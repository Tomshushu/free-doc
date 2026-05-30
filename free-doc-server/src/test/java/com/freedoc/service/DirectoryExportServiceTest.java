package com.freedoc.service;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.dto.BatchExportResult;
import com.freedoc.entity.Doc;
import com.freedoc.service.impl.DirectoryExportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DirectoryExportService测试类
 * 
 * @author FreeDoc Team
 */
@ExtendWith(MockitoExtension.class)
class DirectoryExportServiceTest {
    
    @Mock
    private DirectoryService directoryService;
    
    @Mock
    private DocService docService;
    
    @Mock
    private DocumentAccessService documentAccessService;
    
    @Mock
    private com.freedoc.service.converter.ConverterFactory converterFactory;
    
    @Mock
    private com.freedoc.service.converter.FormatConverter formatConverter;
    
    @InjectMocks
    private DirectoryExportServiceImpl directoryExportService;
    
    private Doc testDoc1;
    private Doc testDoc2;
    
    @BeforeEach
    void setUp() {
        testDoc1 = new Doc();
        testDoc1.setDocId("doc1");
        testDoc1.setDocTitle("测试文档1");
        testDoc1.setDirectoryId("dir1");
        testDoc1.setDocContent("# 测试内容1");
        testDoc1.setCreateTime(LocalDateTime.now());
        testDoc1.setUpdateTime(LocalDateTime.now());
        
        testDoc2 = new Doc();
        testDoc2.setDocId("doc2");
        testDoc2.setDocTitle("测试文档2");
        testDoc2.setDirectoryId("dir2");
        testDoc2.setDocContent("# 测试内容2");
        testDoc2.setCreateTime(LocalDateTime.now());
        testDoc2.setUpdateTime(LocalDateTime.now());
    }
    
    @Test
    void testGetDocumentsRecursively() {
        // 准备测试数据
        String directoryId = "test-dir";
        String userId = "test-user";
        List<Doc> expectedDocs = Arrays.asList(testDoc1, testDoc2);
        
        // 模拟依赖
        when(documentAccessService.hasDirectoryAccess(userId, directoryId)).thenReturn(true);
        when(directoryService.getAllDocumentsRecursively(directoryId, userId)).thenReturn(expectedDocs);
        
        // 执行测试
        List<Doc> result = directoryExportService.getDocumentsRecursively(directoryId, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("测试文档1", result.get(0).getDocTitle());
        assertEquals("测试文档2", result.get(1).getDocTitle());
        
        // 验证方法调用
        verify(documentAccessService).hasDirectoryAccess(userId, directoryId);
        verify(directoryService).getAllDocumentsRecursively(directoryId, userId);
    }
    
    @Test
    void testGetDocumentsInDirectory() {
        // 准备测试数据
        String directoryId = "test-dir";
        String userId = "test-user";
        List<Doc> expectedDocs = Arrays.asList(testDoc1);
        
        // 模拟依赖
        when(documentAccessService.hasDirectoryAccess(userId, directoryId)).thenReturn(true);
        when(docService.getDirectoryDocs(directoryId, userId)).thenReturn(expectedDocs);
        
        // 执行测试
        List<Doc> result = directoryExportService.getDocumentsInDirectory(directoryId, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试文档1", result.get(0).getDocTitle());
        
        // 验证方法调用
        verify(documentAccessService).hasDirectoryAccess(userId, directoryId);
        verify(docService).getDirectoryDocs(directoryId, userId);
    }
    
    @Test
    void testBuildZipEntryPath() {
        // 准备测试数据
        String rootDirectoryId = "root-dir";
        
        // 模拟依赖
        when(directoryService.getDocumentRelativePath(testDoc1.getDocId(), rootDirectoryId))
                .thenReturn("subdir");
        
        // 执行测试
        String result = directoryExportService.buildZipEntryPath(testDoc1, ExportFormat.MARKDOWN, rootDirectoryId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.contains("测试文档1"));
        assertTrue(result.contains(".md"));
        assertTrue(result.startsWith("subdir/"));
        
        // 验证方法调用
        verify(directoryService).getDocumentRelativePath(testDoc1.getDocId(), rootDirectoryId);
    }
    
    @Test
    void testValidateDirectoryExportPermission() {
        // 准备测试数据
        String directoryId = "test-dir";
        String userId = "test-user";
        
        // 测试有权限的情况
        when(documentAccessService.hasDirectoryAccess(userId, directoryId)).thenReturn(true);
        assertTrue(directoryExportService.validateDirectoryExportPermission(directoryId, userId));
        
        // 测试无权限的情况
        when(documentAccessService.hasDirectoryAccess(userId, directoryId)).thenReturn(false);
        assertFalse(directoryExportService.validateDirectoryExportPermission(directoryId, userId));
        
        // 验证方法调用
        verify(documentAccessService, times(2)).hasDirectoryAccess(userId, directoryId);
    }
    
    @Test
    void testCreateExportZip() {
        // 准备测试数据
        List<Doc> docs = Arrays.asList(testDoc1, testDoc2);
        String rootDirectoryId = "root-dir";
        byte[] mockContent = "test content".getBytes();
        
        // 模拟依赖
        when(converterFactory.getConverter(ExportFormat.MARKDOWN)).thenReturn(formatConverter);
        when(formatConverter.convert(any(Doc.class), any())).thenReturn(mockContent);
        when(directoryService.getDocumentRelativePath(anyString(), eq(rootDirectoryId)))
                .thenReturn("subdir");
        
        // 执行测试
        byte[] result = directoryExportService.createExportZip(docs, ExportFormat.MARKDOWN, rootDirectoryId);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // 验证方法调用
        verify(converterFactory).getConverter(ExportFormat.MARKDOWN);
        verify(formatConverter, times(2)).convert(any(Doc.class), any());
    }
    
    @Test
    void testExportDirectory() {
        // 准备测试数据
        String directoryId = "test-dir";
        String userId = "test-user";
        List<Doc> docs = Arrays.asList(testDoc1, testDoc2);
        byte[] mockContent = "test content".getBytes();
        
        // 模拟依赖
        when(documentAccessService.hasDirectoryAccess(userId, directoryId)).thenReturn(true);
        when(docService.getDirectoryDocs(directoryId, userId)).thenReturn(docs);
        when(converterFactory.getConverter(ExportFormat.MARKDOWN)).thenReturn(formatConverter);
        when(formatConverter.convert(any(Doc.class), any())).thenReturn(mockContent);
        when(directoryService.getDocumentRelativePath(anyString(), eq(directoryId)))
                .thenReturn("subdir");
        when(directoryService.getDirectoryName(directoryId)).thenReturn("测试目录");
        
        // 执行测试
        BatchExportResult result = directoryExportService.exportDirectory(
                directoryId, ExportFormat.MARKDOWN, false, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertTrue(result.getZipFilename().contains("测试目录"));
        assertTrue(result.getZipFilename().endsWith(".zip"));
        assertNotNull(result.getZipContent());
        assertTrue(result.getZipContent().length > 0);
        assertEquals(ExportFormat.MARKDOWN, result.getFormat());
        assertFalse(result.isRecursive());
        
        // 验证方法调用
        verify(documentAccessService).hasDirectoryAccess(userId, directoryId);
        verify(docService).getDirectoryDocs(directoryId, userId);
        verify(directoryService).getDirectoryName(directoryId);
    }
    
    @Test
    void testExportDirectoryRecursive() {
        // 准备测试数据
        String directoryId = "test-dir";
        String userId = "test-user";
        List<Doc> docs = Arrays.asList(testDoc1, testDoc2);
        byte[] mockContent = "test content".getBytes();
        
        // 模拟依赖
        when(documentAccessService.hasDirectoryAccess(userId, directoryId)).thenReturn(true);
        when(directoryService.getAllDocumentsRecursively(directoryId, userId)).thenReturn(docs);
        when(converterFactory.getConverter(ExportFormat.MARKDOWN)).thenReturn(formatConverter);
        when(formatConverter.convert(any(Doc.class), any())).thenReturn(mockContent);
        when(directoryService.getDocumentRelativePath(anyString(), eq(directoryId)))
                .thenReturn("subdir");
        when(directoryService.getDirectoryName(directoryId)).thenReturn("测试目录");
        
        // 执行测试
        BatchExportResult result = directoryExportService.exportDirectory(
                directoryId, ExportFormat.MARKDOWN, true, userId);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.getTotalFiles());
        assertTrue(result.isRecursive());
        
        // 验证方法调用
        verify(documentAccessService).hasDirectoryAccess(userId, directoryId);
        verify(directoryService).getAllDocumentsRecursively(directoryId, userId);
        verify(directoryService).getDirectoryName(directoryId);
    }
}