package org.example.dto.quark;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 夸克网盘文件列表数据DTO
 */
public class QuarkFileListData {
    
    @JsonProperty("list")
    private List<QuarkFileInfo> list;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("_page")
    private Integer page;
    
    @JsonProperty("_size")
    private Integer size;

    // Constructors
    public QuarkFileListData() {}

    public QuarkFileListData(List<QuarkFileInfo> list, Integer total) {
        this.list = list;
        this.total = total;
    }

    // Getters and Setters
    public List<QuarkFileInfo> getList() {
        return list;
    }

    public void setList(List<QuarkFileInfo> list) {
        this.list = list;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "QuarkFileListData{" +
                "list=" + list +
                ", total=" + total +
                ", page=" + page +
                ", size=" + size +
                '}';
    }
}
