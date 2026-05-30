package com.freedoc.common.exception;

import com.freedoc.common.constants.Constants;
import com.freedoc.common.result.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * ExportExceptionHandler 测试类
 * 
 * @author FreeDoc Team
 */
@ExtendWith(MockitoExtension.class)
class ExportExceptionHandlerTest {

    @InjectMocks
    private ExportExceptionHandler exportExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/export/document/123");
    }

    @Test
    void testHandleExportException_DocumentNotFound() {
        // Given
        DocumentNotFoundException exception = new DocumentNotFoundException("doc123");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleDocumentNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_DOCUMENT_NOT_FOUND, response.getBody().getErrorCode());
        assertEquals("文档不存在或已被删除", response.getBody().getMessage());
    }

    @Test
    void testHandleExportException_DirectoryNotFound() {
        // Given
        DirectoryNotFoundException exception = new DirectoryNotFoundException("dir123");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleDirectoryNotFoundException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_DIRECTORY_NOT_FOUND, response.getBody().getErrorCode());
        assertEquals("目录不存在或已被删除", response.getBody().getMessage());
    }

    @Test
    void testHandleConversionException_PDF() {
        // Given
        ConversionException exception = new ConversionException("PDF conversion failed");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleConversionException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_CONVERSION_FAILED, response.getBody().getErrorCode());
        assertEquals("PDF转换失败，文档可能包含不支持的格式", response.getBody().getMessage());
        assertEquals("PDF", response.getBody().getAdditionalInfo().get("conversionType"));
    }

    @Test
    void testHandleConversionException_DOCX() {
        // Given
        ConversionException exception = new ConversionException("DOCX conversion failed");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleConversionException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Word文档转换失败，请稍后重试", response.getBody().getMessage());
        assertEquals("DOCX", response.getBody().getAdditionalInfo().get("conversionType"));
    }

    @Test
    void testHandleExportLimitExceededException_FileTooLarge() {
        // Given
        ExportLimitExceededException exception = new ExportLimitExceededException("File too large");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleExportLimitExceededException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_FILE_TOO_LARGE, response.getBody().getErrorCode());
        assertEquals("文件过大，单个文件不能超过50MB", response.getBody().getMessage());
        assertEquals(Constants.Export.MAX_SINGLE_FILE_SIZE, 
                response.getBody().getAdditionalInfo().get("maxFileSize"));
    }

    @Test
    void testHandleExportLimitExceededException_TooManyFiles() {
        // Given
        ExportLimitExceededException exception = new ExportLimitExceededException(
                Constants.Export.ERROR_TOO_MANY_FILES, "Too many files");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleExportLimitExceededException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_TOO_MANY_FILES, response.getBody().getErrorCode());
        assertEquals("文件数量过多，批量导出不能超过1000个文件", response.getBody().getMessage());
        assertEquals(Constants.Export.MAX_BATCH_FILES, 
                response.getBody().getAdditionalInfo().get("maxBatchFiles"));
    }

    @Test
    void testHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleAccessDeniedException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_ACCESS_DENIED, response.getBody().getErrorCode());
        assertEquals("您没有权限导出该资源", response.getBody().getMessage());
    }

    @Test
    void testHandleTimeoutException() {
        // Given
        TimeoutException exception = new TimeoutException("Export timeout");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleTimeoutException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_EXPORT_TIMEOUT, response.getBody().getErrorCode());
        assertEquals("导出超时，请稍后重试或减少导出文件数量", response.getBody().getMessage());
        assertEquals(Constants.Export.EXPORT_TIMEOUT_SECONDS, 
                response.getBody().getAdditionalInfo().get("timeoutSeconds"));
    }

    @Test
    void testHandleIOException_DiskSpaceFull() {
        // Given
        IOException exception = new IOException("No space left on device");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleIOException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DISK_SPACE_FULL", response.getBody().getErrorCode());
        assertEquals("服务器存储空间不足，请稍后重试", response.getBody().getMessage());
    }

    @Test
    void testHandleIOException_PermissionDenied() {
        // Given
        IOException exception = new IOException("Permission denied");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleIOException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PERMISSION_DENIED", response.getBody().getErrorCode());
        assertEquals("文件权限不足，请联系管理员", response.getBody().getMessage());
    }

    @Test
    void testHandleIOException_FileNotFound() {
        // Given
        IOException exception = new IOException("File not found");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleIOException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FILE_NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("文件不存在或已被删除", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgumentException_InvalidFormat() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid format specified");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleIllegalArgumentException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_INVALID_FORMAT, response.getBody().getErrorCode());
        assertEquals("不支持的导出格式", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgumentException_InvalidType() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid type specified");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleIllegalArgumentException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_INVALID_FORMAT, response.getBody().getErrorCode());
        assertEquals("不支持的导出类型", response.getBody().getMessage());
    }

    @Test
    void testHandleOutOfMemoryError() {
        // Given
        OutOfMemoryError error = new OutOfMemoryError("Java heap space");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleOutOfMemoryError(error, webRequest);

        // Then
        assertEquals(HttpStatus.INSUFFICIENT_STORAGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OUT_OF_MEMORY", response.getBody().getErrorCode());
        assertEquals("文件过大，服务器内存不足，请减少导出文件数量", response.getBody().getMessage());
        assertEquals("请尝试分批导出或选择较小的文件", 
                response.getBody().getAdditionalInfo().get("suggestion"));
    }

    @Test
    void testHandleNetworkException() {
        // Given
        NetworkException exception = new NetworkException("Network connection failed");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleNetworkException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_NETWORK_ERROR, response.getBody().getErrorCode());
        assertEquals("网络连接失败，请检查网络设置", response.getBody().getMessage());
        assertEquals("请检查网络连接或稍后重试", 
                response.getBody().getAdditionalInfo().get("suggestion"));
    }

    @Test
    void testHandleExportTaskException() {
        // Given
        ExportTaskException exception = new ExportTaskException(
                Constants.Export.ERROR_EXPORT_TIMEOUT, "Task timeout");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleExportTaskException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.REQUEST_TIMEOUT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_EXPORT_TIMEOUT, response.getBody().getErrorCode());
        assertEquals("导出超时，请稍后重试或减少导出文件数量", response.getBody().getMessage());
        assertEquals("export", response.getBody().getAdditionalInfo().get("taskType"));
    }

    @Test
    void testHandleExportException_GenericExportException() {
        // Given
        ExportException exception = new ExportException(
                Constants.Export.ERROR_CONVERSION_FAILED, "Generic export error");

        // When
        ResponseEntity<ErrorResponse> response = exportExceptionHandler
                .handleExportException(exception, webRequest);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Constants.Export.ERROR_CONVERSION_FAILED, response.getBody().getErrorCode());
        assertEquals("文档转换失败，请检查文档格式是否正确", response.getBody().getMessage());
        assertEquals("Generic export error", response.getBody().getDetails());
    }
}