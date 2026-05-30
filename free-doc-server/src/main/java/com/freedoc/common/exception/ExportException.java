package com.freedoc.common.exception;

/**
 * 导出异常基类
 * 
 * @author FreeDoc Team
 */
public class ExportException extends RuntimeException {
    
    private final String errorCode;
    
    public ExportException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ExportException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}