package org.example.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.builder.ExcelReaderSheetBuilder;
import org.example.dto.GameExcelData;
import org.example.entity.Resource;
import org.example.mapper.ResourceMapper;
import org.example.util.StringCleanupUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Excel游戏数据导入服务
 */
@Service
public class ExcelGameImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelGameImportService.class);

    @Autowired
    private ResourceMapper resourceMapper;

    /**
     * 处理files目录下的所有Excel文件
     */
    @Transactional
    public void processExcelFiles() {
        logger.info("开始处理Excel文件导入游戏数据");

        try {
            // 1. 删除所有source=1的数据
            int deletedCount = resourceMapper.deleteBySource(1);
            logger.info("删除了 {} 条source=1的旧数据", deletedCount);

            // 2. 读取resources/files目录下的所有Excel文件
            File filesDir = getFilesDirectory();
            if (!filesDir.exists()) {
                logger.warn("resources/files目录不存在，创建目录");
                filesDir.mkdirs();
                return;
            }

            File[] excelFiles = filesDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".xls") || name.toLowerCase().endsWith(".xlsx"));

            if (excelFiles == null || excelFiles.length == 0) {
                logger.info("files目录下没有找到Excel文件");
                return;
            }

            List<Resource> allGameResources = new ArrayList<>();

            // 3. 处理每个Excel文件
            for (File excelFile : excelFiles) {
                logger.info("处理Excel文件: {}", excelFile.getName());
                List<Resource> gameResources = processExcelFile(excelFile);
                allGameResources.addAll(gameResources);
            }

            // 4. 批量插入数据
            if (!allGameResources.isEmpty()) {
                // 分批插入，每批1000条
                int batchSize = 1000;
                for (int i = 0; i < allGameResources.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, allGameResources.size());
                    List<Resource> batch = allGameResources.subList(i, endIndex);
                    resourceMapper.insertBatch(batch);
                    logger.info("批量插入第 {} 批，共 {} 条数据", (i / batchSize) + 1, batch.size());
                }
                logger.info("成功导入 {} 条游戏数据", allGameResources.size());
            } else {
                logger.info("没有有效的游戏数据需要导入");
            }

        } catch (Exception e) {
            logger.error("处理Excel文件时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("Excel文件处理失败", e);
        }
    }

    /**
     * 处理单个Excel文件
     */
    private List<Resource> processExcelFile(File excelFile) {
        List<Resource> gameResources = new ArrayList<>();

        try {
            // 读取Excel数据，跳过前5行说明，从第6行开始读取（第6行是标题行）
            List<GameExcelData> excelDataList = EasyExcel.read(excelFile)
                    .head(GameExcelData.class)
                    .sheet()
                    .headRowNumber(5) // 设置标题行为第6行（从0开始计数，所以是5）
                    .doReadSync();

            logger.info("从文件 {} 读取到 {} 条数据（跳过前5行说明）", excelFile.getName(), excelDataList.size());

            // 转换为Resource对象
            for (GameExcelData excelData : excelDataList) {
                if (isValidGameData(excelData)) {
                    Resource resource = convertToResource(excelData);
                    gameResources.add(resource);
                }
            }

            logger.info("文件 {} 转换后有效数据 {} 条", excelFile.getName(), gameResources.size());

        } catch (Exception e) {
            logger.error("处理Excel文件 {} 时发生错误: {}", excelFile.getName(), e.getMessage(), e);
        }

        return gameResources;
    }

    /**
     * 验证游戏数据是否有效
     */
    private boolean isValidGameData(GameExcelData excelData) {
        // 检查游戏名是否为空
        if (excelData.getGameName() == null || excelData.getGameName().trim().isEmpty()) {
            return false;
        }

        // 过滤包含"迅雷"的游戏名
        if (excelData.getGameName().contains("迅雷")) {
            logger.debug("过滤包含迅雷的游戏: {}", excelData.getGameName());
            return false;
        }

        return true;
    }

    /**
     * 将Excel数据转换为Resource对象
     */
    private Resource convertToResource(GameExcelData excelData) {
        Resource resource = new Resource();

        // 在游戏名前加上密码提示
        String gameName = "（密码：amuyouxi）" + excelData.getGameName().trim();
        String remark = excelData.getRemark() != null ? excelData.getRemark().trim() : "";
        String gameUrl = excelData.getGameUrl() != null ? excelData.getGameUrl().trim() : "";

        // 清理字段中的反斜杠字符
        String[] cleanedFields = StringCleanupUtil.cleanResourceFields(gameName, remark, gameUrl);

        // 记录清理信息
        if (StringCleanupUtil.containsBackslashes(gameName) ||
            StringCleanupUtil.containsBackslashes(remark) ||
            StringCleanupUtil.containsBackslashes(gameUrl)) {
            logger.info("清理Excel游戏资源字段中的反斜杠字符: {}", gameName);
        }

        resource.setName(cleanedFields[0]);
        resource.setContent(cleanedFields[1]);
        resource.setUrl(cleanedFields[2]);

        // 设置固定值
        resource.setType("game");
        resource.setLevel(1);
        resource.setSource(1); // Excel导入

        // 设置时间戳
        resource.setResourceTime((int) (System.currentTimeMillis() / 1000));

        return resource;
    }

    /**
     * 获取resources/files目录
     */
    private File getFilesDirectory() {
        try {
            // 尝试从classpath获取files目录
            ClassPathResource resource = new ClassPathResource("files");
            if (resource.exists()) {
                return resource.getFile();
            }
        } catch (IOException e) {
            logger.debug("无法从classpath获取files目录: {}", e.getMessage());
        }

        // 如果classpath中不存在，则使用项目根目录下的src/main/resources/files
        String projectRoot = System.getProperty("user.dir");
        File filesDir = new File(projectRoot, "src/main/resources/files");

        logger.info("使用files目录路径: {}", filesDir.getAbsolutePath());
        return filesDir;
    }
}
