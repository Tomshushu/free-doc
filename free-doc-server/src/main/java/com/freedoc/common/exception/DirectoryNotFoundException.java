package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;

public class DirectoryNotFoundException extends ExportException {

    public DirectoryNotFoundException(String directoryId) {
        super(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, "Directory not found: " + directoryId);
    }

    public DirectoryNotFoundException(String directoryId, String message) {
        super(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, message);
    }
}
