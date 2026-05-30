package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;

public class DocumentNotFoundException extends ExportException {

    public DocumentNotFoundException(String docId) {
        super(Constants.Export.ERROR_DOCUMENT_NOT_FOUND, "Document not found: " + docId);
    }

    public DocumentNotFoundException(String docId, String message) {
        super(Constants.Export.ERROR_DOCUMENT_NOT_FOUND, message);
    }
}
