package org.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

public class Menu {

    private Integer id;

    @NotBlank(message = "菜单标识不能为空")
    private String menu;

    @NotBlank(message = "菜单名称不能为空")
    private String name;

    @NotNull(message = "菜单层级不能为空")
    private Integer level = 1;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 默认构造函数
    public Menu() {}

    // 带参构造函数
    public Menu(String menu, String name, Integer level) {
        this.menu = menu;
        this.name = name;
        this.level = level;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu1 = (Menu) o;
        return Objects.equals(id, menu1.id) &&
                Objects.equals(menu, menu1.menu) &&
                Objects.equals(name, menu1.name) &&
                Objects.equals(level, menu1.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, menu, name, level);
    }

    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", menu='" + menu + '\'' +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
