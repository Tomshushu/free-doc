package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;

/**
 * 网络异常
 * 
 * @author FreeDoc Team
 */
public class NetworkException extends ExportException {
    
    public NetworkException(String message) {
        super(Constants.Export.ERROR_NETWORK_ERROR, message);
    }
    
    public NetworkException(String message, Throwable cause) {
        super(Constants.Export.ERROR_NETWORK_ERROR, message, cause);
    }
}