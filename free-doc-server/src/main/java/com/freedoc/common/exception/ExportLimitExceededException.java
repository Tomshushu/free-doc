package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;

/**
 * 导出限制超出异常
 * 
 * @author FreeDoc Team
 */
public class ExportLimitExceededException extends ExportException {
    
    public ExportLimitExceededException(String message) {
        super(Constants.Export.ERROR_FILE_TOO_LARGE, message);
    }
    
    public ExportLimitExceededException(String errorCode, String message) {
        super(errorCode, message);
    }
}