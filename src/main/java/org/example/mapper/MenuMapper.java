package org.example.mapper;

import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Menu;

import java.util.List;

@Mapper
public interface MenuMapper {

    /**
     * 根据ID查询菜单
     */
    Menu selectById(@Param("id") Integer id);

    /**
     * 查询所有菜单，按层级和ID排序
     */
    List<Menu> selectAllOrderByLevelAndId();

    /**
     * 根据层级查询菜单
     */
    List<Menu> selectByLevel(@Param("level") Integer level);

    /**
     * 根据层级范围查询菜单
     */
    List<Menu> selectByLevelRange(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    /**
     * 根据菜单标识查询
     */
    Menu selectByMenu(@Param("menu") String menu);

    /**
     * 根据菜单名称模糊查询
     */
    List<Menu> selectByNameLike(@Param("name") String name);

    /**
     * 查询最大层级
     */
    Integer selectMaxLevel();

    /**
     * 查询指定层级的菜单数量
     */
    Long countByLevel(@Param("level") Integer level);

    /**
     * 根据层级和名称查询
     */
    List<Menu> selectByLevelAndNameLike(@Param("level") Integer level, @Param("name") String name);

    /**
     * 插入菜单
     */
    int insert(Menu menu);

    /**
     * 更新菜单
     */
    int updateById(Menu menu);

    /**
     * 删除菜单
     */
    int deleteById(@Param("id") Integer id);
}
