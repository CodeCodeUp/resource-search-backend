package org.example.controller;

import org.example.dto.MenuResponse;
import org.example.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menus")
@CrossOrigin(origins = "*")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    /**
     * 获取所有菜单（层级结构）
     */
    @GetMapping("/hierarchical")
    public ResponseEntity<List<MenuResponse>> getAllMenusHierarchical() {
        logger.info("API调用：获取所有菜单的层级结构");
        
        try {
            List<MenuResponse> menus = menuService.getAllMenusHierarchical();
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            logger.error("获取层级菜单失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据层级获取菜单
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<MenuResponse>> getMenusByLevel(@PathVariable Integer level) {
        logger.info("API调用：获取层级 {} 的菜单", level);
        
        try {
            if (level < 1) {
                return ResponseEntity.badRequest().build();
            }
            
            List<MenuResponse> menus = menuService.getMenusByLevel(level);
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            logger.error("根据层级获取菜单失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取指定层级范围的菜单
     */
    @GetMapping("/level-range")
    public ResponseEntity<List<MenuResponse>> getMenusByLevelRange(
            @RequestParam Integer minLevel,
            @RequestParam Integer maxLevel) {
        logger.info("API调用：获取层级范围 {}-{} 的菜单", minLevel, maxLevel);
        
        try {
            if (minLevel < 1 || maxLevel < minLevel) {
                return ResponseEntity.badRequest().build();
            }
            
            List<MenuResponse> menus = menuService.getMenusByLevelRange(minLevel, maxLevel);
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            logger.error("根据层级范围获取菜单失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据名称搜索菜单
     */
    @GetMapping("/search")
    public ResponseEntity<List<MenuResponse>> searchMenusByName(@RequestParam String name) {
        logger.info("API调用：根据名称搜索菜单，关键词: {}", name);
        
        try {
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<MenuResponse> menus = menuService.searchMenusByName(name.trim());
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            logger.error("根据名称搜索菜单失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据层级和名称搜索菜单
     */
    @GetMapping("/search-by-level")
    public ResponseEntity<List<MenuResponse>> searchMenusByLevelAndName(
            @RequestParam Integer level,
            @RequestParam String name) {
        logger.info("API调用：根据层级 {} 和名称 {} 搜索菜单", level, name);
        
        try {
            if (level < 1 || name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            List<MenuResponse> menus = menuService.searchMenusByLevelAndName(level, name.trim());
            return ResponseEntity.ok(menus);
        } catch (Exception e) {
            logger.error("根据层级和名称搜索菜单失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取最大层级
     */
    @GetMapping("/max-level")
    public ResponseEntity<Integer> getMaxLevel() {
        logger.info("API调用：获取最大层级");
        
        try {
            Integer maxLevel = menuService.getMaxLevel();
            return ResponseEntity.ok(maxLevel);
        } catch (Exception e) {
            logger.error("获取最大层级失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
