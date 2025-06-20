package org.example.controller;

import org.example.task.ExcelGameImportTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Excel导入控制器
 */
@RestController
@RequestMapping("/excel")
@CrossOrigin(origins = "*")
public class ExcelImportController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportController.class);

    @Autowired
    private ExcelGameImportTask excelGameImportTask;

    /**
     * 手动触发Excel游戏数据导入
     */
    @PostMapping("/import/games")
    public ResponseEntity<String> manualImportGames() {
        logger.info("API调用：手动触发Excel游戏数据导入");

        try {
            excelGameImportTask.manualImport();
            return ResponseEntity.ok("Excel游戏数据导入任务已触发");
        } catch (Exception e) {
            logger.error("手动触发Excel导入失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("导入失败: " + e.getMessage());
        }
    }

    /**
     * 获取导入状态信息
     */
    @GetMapping("/import/status")
    public ResponseEntity<String> getImportStatus() {
        return ResponseEntity.ok("Excel导入功能正常运行，定时任务每小时执行一次");
    }
}
