package com.freedoc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freedoc.common.enums.ExportFormat;
import com.freedoc.common.util.JwtUtil;
import com.freedoc.dto.ExportRequest;
import com.freedoc.dto.ExportResult;
import com.freedoc.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ExportController集成测试
 * 
 * @author FreeDoc Team
 */
@WebMvcTest(ExportController.class)
class ExportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ExportService exportService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ExportResult mockExportResult;
    
    @BeforeEach
    void setUp() {
        mockExportResult = ExportResult.builder()
                .filename("test-document.md")
                .content("# Test Document\n\nThis is a test document.".getBytes())
                .mimeType("text/markdown")
                .size(42L)
                .format(ExportFormat.MARKDOWN)
                .createdAt(LocalDateTime.now())
                .batch(false)
                .build();
    }
    
    @Test
    void testExportDocument_Success() throws Exception {
        // Given
        String docId = "test-doc-id";
        ExportFormat format = ExportFormat.MARKDOWN;
        
        when(exportService.exportDocument(eq(docId), eq(format), anyString()))
                .thenReturn(mockExportResult);
        
        // When & Then
        mockMvc.perform(post("/api/export/document/{docId}", docId)
                        .param("format", format.name())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/markdown"))
                .andExpect(header().string("Content-Length", "42"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(mockExportResult.getContent()));
    }
    
    @Test
    void testExportDocument_GetMethod_Success() throws Exception {
        // Given
        String docId = "test-doc-id";
        ExportFormat format = ExportFormat.MARKDOWN;
        
        when(exportService.exportDocument(eq(docId), eq(format), anyString()))
                .thenReturn(mockExportResult);
        
        // When & Then - 测试GET方法
        mockMvc.perform(get("/api/export/document/{docId}", docId)
                        .param("format", format.name())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/markdown"))
                .andExpect(header().string("Content-Length", "42"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(mockExportResult.getContent()));
    }
    
    @Test
    void testExportDirectory_Success() throws Exception {
        // Given
        String directoryId = "test-dir-id";
        ExportFormat format = ExportFormat.MARKDOWN;
        boolean recursive = true;
        
        ExportResult batchResult = ExportResult.builder()
                .filename("export_directory.zip")
                .content("ZIP_CONTENT".getBytes())
                .mimeType("application/zip")
                .size(11L)
                .format(format)
                .createdAt(LocalDateTime.now())
                .batch(true)
                .fileCount(5)
                .build();
        
        when(exportService.exportDirectory(eq(directoryId), eq(format), eq(recursive), anyString()))
                .thenReturn(batchResult);
        
        // When & Then
        mockMvc.perform(post("/api/export/directory/{directoryId}", directoryId)
                        .param("format", format.name())
                        .param("recursive", "true")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Length", "11"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().bytes(batchResult.getContent()));
    }
    
    @Test
    void testExportWithRequestBody_DocumentType() throws Exception {
        // Given
        ExportRequest request = new ExportRequest();
        request.setTargetId("test-doc-id");
        request.setFormat(ExportFormat.MARKDOWN);
        request.setExportType(ExportRequest.ExportType.DOCUMENT);
        request.setRecursive(false);
        
        when(exportService.exportDocument(eq(request.getTargetId()), eq(request.getFormat()), anyString()))
                .thenReturn(mockExportResult);
        
        // When & Then
        mockMvc.perform(post("/api/export/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/markdown"))
                .andExpect(content().bytes(mockExportResult.getContent()));
    }
    
    @Test
    void testExportDocumentAsync_Success() throws Exception {
        // Given
        String docId = "test-doc-id";
        ExportFormat format = ExportFormat.MARKDOWN;
        String taskId = "export_12345";
        
        when(exportService.exportDocumentAsync(eq(docId), eq(format), anyString()))
                .thenReturn(taskId);
        
        // When & Then
        mockMvc.perform(post("/api/export/document/{docId}/async", docId)
                        .param("format", format.name())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(taskId));
    }
    
    @Test
    void testGetExportTaskStatus_Success() throws Exception {
        // Given
        String taskId = "export_12345";
        String status = "COMPLETED";
        
        when(exportService.getExportTaskStatus(eq(taskId)))
                .thenReturn(status);
        
        // When & Then
        mockMvc.perform(get("/api/export/task/{taskId}/status", taskId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(status));
    }
    
    @Test
    void testGetExportTaskResult_Success() throws Exception {
        // Given
        String taskId = "export_12345";
        
        when(exportService.getExportTaskResult(eq(taskId)))
                .thenReturn(mockExportResult);
        
        // When & Then
        mockMvc.perform(get("/api/export/task/{taskId}/result", taskId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/markdown"))
                .andExpect(content().bytes(mockExportResult.getContent()));
    }
    
    @Test
    void testGetExportTaskResult_NotFound() throws Exception {
        // Given
        String taskId = "export_12345";
        
        when(exportService.getExportTaskResult(eq(taskId)))
                .thenReturn(null);
        
        // When & Then
        mockMvc.perform(get("/api/export/task/{taskId}/result", taskId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testExportDocument_InvalidFormat() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/export/document/{docId}", "test-doc-id")
                        .param("format", "INVALID_FORMAT")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testExportDocument_MissingFormat() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/export/document/{docId}", "test-doc-id")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testExportWithRequestBody_InvalidRequest() throws Exception {
        // Given - 缺少必要字段的请求
        ExportRequest request = new ExportRequest();
        // 不设置必要字段
        
        // When & Then
        mockMvc.perform(post("/api/export/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
    }
}