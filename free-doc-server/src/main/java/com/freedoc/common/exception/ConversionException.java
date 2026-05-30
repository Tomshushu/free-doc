package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;

/**
 * 格式转换异常
 * 
 * @author FreeDoc Team
 */
public class ConversionException extends ExportException {
    
    public ConversionException(String message) {
        super(Constants.Export.ERROR_CONVERSION_FAILED, message);
    }
    
    public ConversionException(String message, Throwable cause) {
        super(Constants.Export.ERROR_CONVERSION_FAILED, message, cause);
    }
}