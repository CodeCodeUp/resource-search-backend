package org.example.task;

import org.example.service.ExcelGameImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Excel游戏数据导入定时任务
 * 每小时执行一次，删除source=1的数据并重新导入
 */
@Component
public class ExcelGameImportTask {

    private static final Logger logger = LoggerFactory.getLogger(ExcelGameImportTask.class);

    @Autowired
    private ExcelGameImportService excelGameImportService;

    /**
     * 定时任务：每周五晚上八点执行一次Excel游戏数据导入
     * cron表达式：0 0 * * * ? 表示每小时的0分0秒执行
     */
    @Scheduled(cron = "0 0 20 ? * FRI")
    public void importExcelGameData() {
        logger.info("=== 开始执行Excel游戏数据导入定时任务 ===");
        
        long startTime = System.currentTimeMillis();
        
        try {
            excelGameImportService.processExcelFiles();
            
            long endTime = System.currentTimeMillis();
            logger.info("=== Excel游戏数据导入定时任务执行完成，耗时: {} ms ===", (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("=== Excel游戏数据导入定时任务执行失败: {} ===", e.getMessage(), e);
        }
    }

    /**
     * 手动触发导入任务（用于测试）
     */
    public void manualImport() {
        logger.info("=== 手动触发Excel游戏数据导入任务 ===");
        importExcelGameData();
    }
}
