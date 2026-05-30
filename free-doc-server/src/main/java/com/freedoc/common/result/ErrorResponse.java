package com.freedoc.common.result;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 标准化错误响应格式
 * 
 * @author FreeDoc Team
 */
@Data
public class ErrorResponse {
    
    /**
     * 错误码
     */
    private String errorCode;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 详细错误信息
     */
    private String details;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 请求路径
     */
    private String path;
    
    /**
     * 额外的错误信息
     */
    private Map<String, Object> additionalInfo;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String errorCode, String message) {
        this();
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public ErrorResponse(String errorCode, String message, String details) {
        this(errorCode, message);
        this.details = details;
    }
    
    public ErrorResponse(String errorCode, String message, String details, String path) {
        this(errorCode, message, details);
        this.path = path;
    }
    
    /**
     * 创建简单错误响应
     */
    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }
    
    /**
     * 创建详细错误响应
     */
    public static ErrorResponse of(String errorCode, String message, String details) {
        return new ErrorResponse(errorCode, message, details);
    }
    
    /**
     * 创建完整错误响应
     */
    public static ErrorResponse of(String errorCode, String message, String details, String path) {
        return new ErrorResponse(errorCode, message, details, path);
    }
    
    /**
     * 添加额外信息
     */
    public ErrorResponse addInfo(String key, Object value) {
        if (this.additionalInfo == null) {
            this.additionalInfo = new java.util.HashMap<>();
        }
        this.additionalInfo.put(key, value);
        return this;
    }
}