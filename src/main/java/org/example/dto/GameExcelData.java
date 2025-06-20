package org.example.dto;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * Excel游戏数据模型
 */
public class GameExcelData {

    @ExcelProperty(index = 1)
    private String gameName;

    @ExcelProperty(index = 2)
    private String gameUrl;

    @ExcelProperty(index = 5)
    private String remark;

    // 默认构造函数
    public GameExcelData() {}

    // 带参构造函数
    public GameExcelData(String gameName, String gameUrl, String remark) {
        this.gameName = gameName;
        this.gameUrl = gameUrl;
        this.remark = remark;
    }

    // Getters and Setters
    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameUrl() {
        return gameUrl;
    }

    public void setGameUrl(String gameUrl) {
        this.gameUrl = gameUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "GameExcelData{" +
                "gameName='" + gameName + '\'' +
                ", gameUrl='" + gameUrl + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
