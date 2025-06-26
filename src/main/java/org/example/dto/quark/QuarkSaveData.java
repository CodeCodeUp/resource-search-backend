package org.example.dto.quark;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 夸克网盘保存任务数据DTO
 */
public class QuarkSaveData {
    
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("save_as")
    private SaveAsInfo saveAs;

    // Constructors
    public QuarkSaveData() {}

    public QuarkSaveData(String taskId, SaveAsInfo saveAs) {
        this.taskId = taskId;
        this.saveAs = saveAs;
    }

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public SaveAsInfo getSaveAs() {
        return saveAs;
    }

    public void setSaveAs(SaveAsInfo saveAs) {
        this.saveAs = saveAs;
    }

    /**
     * 保存结果信息
     */
    public static class SaveAsInfo {
        
        @JsonProperty("save_as_top_fids")
        private List<Object> saveAsTopFids;

        public SaveAsInfo() {}

        public SaveAsInfo(List<Object> saveAsTopFids) {
            this.saveAsTopFids = saveAsTopFids;
        }

        public List<Object> getSaveAsTopFids() {
            return saveAsTopFids;
        }

        public void setSaveAsTopFids(List<Object> saveAsTopFids) {
            this.saveAsTopFids = saveAsTopFids;
        }

        @Override
        public String toString() {
            return "SaveAsInfo{" +
                    "saveAsTopFids=" + saveAsTopFids +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "QuarkSaveData{" +
                "taskId='" + taskId + '\'' +
                ", saveAs=" + saveAs +
                '}';
    }
}
