package com.freedoc.common.exception;

/**
 * 导出任务异常
 * 
 * @author FreeDoc Team
 */
public class ExportTaskException extends ExportException {
    
    public ExportTaskException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public ExportTaskException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}