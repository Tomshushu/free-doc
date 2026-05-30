package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.result.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestControllerAdvice
@Order(1)
public class ExportExceptionHandler {

    @ExceptionHandler(ExportException.class)
    public ResponseEntity<ErrorResponse> handleExportException(ExportException e, WebRequest request) {
        log.error("Export exception: errorCode={}, message={}", e.getErrorCode(), e.getMessage(), e);

        HttpStatus status = determineHttpStatus(e.getErrorCode());
        String userMessage = getUserFriendlyMessage(e.getErrorCode(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), userMessage, e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("httpStatus", status.value());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(ConversionException.class)
    public ResponseEntity<ErrorResponse> handleConversionException(ConversionException e, WebRequest request) {
        log.error("Conversion exception: {}", e.getMessage(), e);

        String messageKey = "error.export.conversionFailed";
        if (e.getMessage().contains("PDF")) {
            messageKey = "error.export.pdfConversionFailed";
        } else if (e.getMessage().contains("DOCX")) {
            messageKey = "error.export.wordConversionFailed";
        } else if (e.getMessage().contains("HTML")) {
            messageKey = "error.export.htmlConversionFailed";
        }

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), I18nUtil.getMessage(messageKey), e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("conversionType", extractConversionType(e.getMessage()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(ExportLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleExportLimitExceededException(ExportLimitExceededException e, WebRequest request) {
        log.error("Export limit exceeded: errorCode={}, message={}", e.getErrorCode(), e.getMessage());

        String messageKey;
        switch (e.getErrorCode()) {
            case Constants.Export.ERROR_FILE_TOO_LARGE:
                messageKey = "error.export.FILE_TOO_LARGE";
                break;
            case Constants.Export.ERROR_TOO_MANY_FILES:
                messageKey = "error.export.TOO_MANY_FILES";
                break;
            default:
                messageKey = "error.export.limitExceeded";
        }

        String userMessage = e.getErrorCode().equals(Constants.Export.ERROR_FILE_TOO_LARGE) ||
                e.getErrorCode().equals(Constants.Export.ERROR_TOO_MANY_FILES)
                ? I18nUtil.getMessage(messageKey)
                : I18nUtil.getMessage(messageKey, e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), userMessage, e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("maxFileSize", Constants.Export.MAX_SINGLE_FILE_SIZE)
                .addInfo("maxBatchFiles", Constants.Export.MAX_BATCH_FILES);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFoundException(DocumentNotFoundException e, WebRequest request) {
        log.error("Document not found exception: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), I18nUtil.getMessage("error.export.DOCUMENT_NOT_FOUND"), e.getMessage())
                .addInfo("requestPath", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDirectoryNotFoundException(DirectoryNotFoundException e, WebRequest request) {
        log.error("Directory not found exception: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), I18nUtil.getMessage("error.export.DIRECTORY_NOT_FOUND"), e.getMessage())
                .addInfo("requestPath", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e, WebRequest request) {
        log.error("Access denied exception: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(Constants.Export.ERROR_ACCESS_DENIED,
                I18nUtil.getMessage("error.export.ACCESS_DENIED"), e.getMessage())
                .addInfo("requestPath", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException e, WebRequest request) {
        log.error("Export timeout exception: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(Constants.Export.ERROR_EXPORT_TIMEOUT,
                I18nUtil.getMessage("error.export.EXPORT_TIMEOUT"), e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("timeoutSeconds", Constants.Export.EXPORT_TIMEOUT_SECONDS);

        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException e, WebRequest request) {
        log.error("IO exception: {}", e.getMessage(), e);

        String messageKey = "error.export.ioFailed";
        String errorCode = "IO_ERROR";

        if (e.getMessage().contains("No space left")) {
            messageKey = "error.export.diskSpaceFull";
            errorCode = "DISK_SPACE_FULL";
        } else if (e.getMessage().contains("Permission denied")) {
            messageKey = "error.export.permissionDenied";
            errorCode = "PERMISSION_DENIED";
        } else if (e.getMessage().contains("File not found")) {
            messageKey = "error.export.fileNotFound";
            errorCode = "FILE_NOT_FOUND";
        }

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, I18nUtil.getMessage(messageKey), e.getMessage())
                .addInfo("requestPath", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e, WebRequest request) {
        log.error("Illegal argument exception: {}", e.getMessage());

        String messageKey = "error.export.paramError";
        String errorCode = Constants.Export.ERROR_INVALID_FORMAT;

        if (e.getMessage().contains("format")) {
            messageKey = "error.export.unsupportedFormat";
        } else if (e.getMessage().contains("type")) {
            messageKey = "error.export.unsupportedType";
        } else {
            messageKey = "error.export.paramErrorWithDetail";
        }

        String userMessage = messageKey.equals("error.export.paramErrorWithDetail")
                ? I18nUtil.getMessage(messageKey, e.getMessage())
                : I18nUtil.getMessage(messageKey);

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, userMessage, e.getMessage())
                .addInfo("requestPath", request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity<ErrorResponse> handleOutOfMemoryError(OutOfMemoryError e, WebRequest request) {
        log.error("Out of memory error: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of("OUT_OF_MEMORY",
                I18nUtil.getMessage("error.export.outOfMemory"), e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("suggestion", I18nUtil.getMessage("error.export.tryBatchExport"));

        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).body(errorResponse);
    }

    @ExceptionHandler(NetworkException.class)
    public ResponseEntity<ErrorResponse> handleNetworkException(NetworkException e, WebRequest request) {
        log.error("Network exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(),
                I18nUtil.getMessage("error.export.networkFailed"), e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("suggestion", I18nUtil.getMessage("error.export.checkNetwork"));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    @ExceptionHandler(ExportTaskException.class)
    public ResponseEntity<ErrorResponse> handleExportTaskException(ExportTaskException e, WebRequest request) {
        log.error("Export task exception: errorCode={}, message={}", e.getErrorCode(), e.getMessage(), e);

        HttpStatus status = determineHttpStatus(e.getErrorCode());
        String userMessage = getUserFriendlyMessage(e.getErrorCode(), e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(e.getErrorCode(), userMessage, e.getMessage())
                .addInfo("requestPath", request.getDescription(false))
                .addInfo("taskType", "export");

        return ResponseEntity.status(status).body(errorResponse);
    }

    private HttpStatus determineHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        switch (errorCode) {
            case Constants.Export.ERROR_DOCUMENT_NOT_FOUND:
            case Constants.Export.ERROR_DIRECTORY_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case Constants.Export.ERROR_ACCESS_DENIED:
                return HttpStatus.FORBIDDEN;
            case Constants.Export.ERROR_INVALID_FORMAT:
            case Constants.Export.ERROR_FILE_TOO_LARGE:
            case Constants.Export.ERROR_TOO_MANY_FILES:
                return HttpStatus.BAD_REQUEST;
            case Constants.Export.ERROR_EXPORT_TIMEOUT:
                return HttpStatus.REQUEST_TIMEOUT;
            case Constants.Export.ERROR_NETWORK_ERROR:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case Constants.Export.ERROR_CONVERSION_FAILED:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String getUserFriendlyMessage(String errorCode, String originalMessage) {
        if (errorCode == null) {
            return I18nUtil.getMessage("error.export.failed");
        }

        String key = "error.export." + errorCode;
        String message = I18nUtil.getMessage(key);
        if (message.equals(key)) {
            return I18nUtil.getMessage("error.export.failedWithDetail", originalMessage);
        }
        return message;
    }

    private String extractConversionType(String message) {
        if (message == null) {
            return "UNKNOWN";
        }

        if (message.contains("PDF")) {
            return "PDF";
        } else if (message.contains("DOCX")) {
            return "DOCX";
        } else if (message.contains("HTML")) {
            return "HTML";
        } else if (message.contains("MARKDOWN")) {
            return "MARKDOWN";
        }

        return "UNKNOWN";
    }
}
