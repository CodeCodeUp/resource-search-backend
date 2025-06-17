package org.example.service;

import org.example.dto.MenuResponse;
import org.example.entity.Menu;
import org.example.mapper.MenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuService.class);

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 获取所有菜单（层级结构）
     */
    public List<MenuResponse> getAllMenusHierarchical() {
        logger.info("获取所有菜单的层级结构");

        List<Menu> allMenus = menuMapper.selectAllOrderByLevelAndId();
        return buildHierarchicalStructure(allMenus);
    }

    /**
     * 根据层级获取菜单
     */
    public List<MenuResponse> getMenusByLevel(Integer level) {
        logger.info("获取层级 {} 的菜单", level);

        List<Menu> menus = menuMapper.selectByLevel(level);
        return menus.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定层级范围的菜单
     */
    public List<MenuResponse> getMenusByLevelRange(Integer minLevel, Integer maxLevel) {
        logger.info("获取层级范围 {}-{} 的菜单", minLevel, maxLevel);

        List<Menu> menus = menuMapper.selectByLevelRange(minLevel, maxLevel);
        return buildHierarchicalStructure(menus);
    }

    /**
     * 根据名称搜索菜单
     */
    public List<MenuResponse> searchMenusByName(String name) {
        logger.info("根据名称搜索菜单: {}", name);

        List<Menu> menus = menuMapper.selectByNameLike(name);
        return menus.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取最大层级
     */
    public Integer getMaxLevel() {
        Integer maxLevel = menuMapper.selectMaxLevel();
        return maxLevel != null ? maxLevel : 0;
    }

    /**
     * 根据层级和名称搜索菜单
     */
    public List<MenuResponse> searchMenusByLevelAndName(Integer level, String name) {
        logger.info("根据层级 {} 和名称 {} 搜索菜单", level, name);

        List<Menu> menus = menuMapper.selectByLevelAndNameLike(level, name);
        return menus.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 构建层级结构
     */
    private List<MenuResponse> buildHierarchicalStructure(List<Menu> allMenus) {
        // 按层级分组
        Map<Integer, List<Menu>> menusByLevel = allMenus.stream()
                .collect(Collectors.groupingBy(Menu::getLevel));

        List<MenuResponse> result = new ArrayList<>();
        
        // 获取第一层菜单
        List<Menu> level1Menus = menusByLevel.get(1);
        if (level1Menus != null) {
            for (Menu menu : level1Menus) {
                MenuResponse menuResponse = convertToResponse(menu);
                
                // 递归构建子菜单
                buildChildren(menuResponse, menusByLevel, 2);
                result.add(menuResponse);
            }
        }
        
        return result;
    }

    /**
     * 递归构建子菜单
     */
    private void buildChildren(MenuResponse parent, Map<Integer, List<Menu>> menusByLevel, int currentLevel) {
        List<Menu> currentLevelMenus = menusByLevel.get(currentLevel);
        if (currentLevelMenus == null || currentLevelMenus.isEmpty()) {
            return;
        }

        List<MenuResponse> children = new ArrayList<>();
        for (Menu menu : currentLevelMenus) {
            MenuResponse child = convertToResponse(menu);
            
            // 继续递归构建下一层
            buildChildren(child, menusByLevel, currentLevel + 1);
            children.add(child);
        }
        
        if (!children.isEmpty()) {
            parent.setChildren(children);
        }
    }

    /**
     * 转换为响应对象
     */
    private MenuResponse convertToResponse(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getMenu(),
                menu.getName(),
                menu.getLevel(),
                menu.getCreateTime(),
                menu.getUpdateTime()
        );
    }
}
