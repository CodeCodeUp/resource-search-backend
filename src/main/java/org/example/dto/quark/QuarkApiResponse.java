package org.example.dto.quark;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 夸克网盘API响应基础类
 */
public class QuarkApiResponse<T> {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("data")
    private T data;

    // Constructors
    public QuarkApiResponse() {}

    public QuarkApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }

    @Override
    public String toString() {
        return "QuarkApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", data=" + data +
                '}';
    }
}
