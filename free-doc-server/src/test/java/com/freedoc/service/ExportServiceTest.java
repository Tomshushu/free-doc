package com.freedoc.service;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.dto.ExportResult;
import com.freedoc.entity.Doc;
import com.freedoc.service.impl.ExportServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ExportService测试类
 * 
 * @author FreeDoc Team
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExportServiceTest {
    
    @SpyBean
    private ExportServiceImpl exportService;
    
    @MockBean
    private DocService docService;
    
    @MockBean
    private DocumentAccessService documentAccessService;
    
    @MockBean
    private ExportCacheService exportCacheService;
    
    @Test
    public void testExportDocumentBasic() {
        // 准备测试数据
        String docId = "test-doc-id";
        String userId = "test-user-id";
        ExportFormat format = ExportFormat.MARKDOWN;
        
        Doc testDoc = new Doc();
        testDoc.setDocId(docId);
        testDoc.setDocTitle("测试文档");
        testDoc.setDocContent("# 测试标题\n\n这是测试内容。");
        testDoc.setCreateUser(userId);
        testDoc.setCreateTime(LocalDateTime.now());
        
        // 模拟依赖服务
        when(documentAccessService.hasReadPermission(userId, docId)).thenReturn(true);
        when(exportCacheService.getCachedExport(anyString(), any(), anyString())).thenReturn(null);
        when(docService.getById(docId)).thenReturn(testDoc);
        
        // 模拟DocService.getDocById方法
        when(docService.getDocById(docId, userId)).thenReturn(testDoc);
        
        // 执行测试
        try {
            ExportResult result = exportService.exportDocument(docId, format, userId);
            
            // 验证结果
            assertNotNull(result);
            assertEquals(format, result.getFormat());
            assertNotNull(result.getContent());
            assertTrue(result.getContent().length > 0);
            assertFalse(result.isBatch());
            
            // 验证文件名
            String expectedFilename = "测试文档.md";
            assertEquals(expectedFilename, result.getFilename());
            
            System.out.println("导出测试成功:");
            System.out.println("- 文件名: " + result.getFilename());
            System.out.println("- 格式: " + result.getFormat());
            System.out.println("- 大小: " + result.getSize() + " 字节");
            System.out.println("- 内容预览: " + new String(result.getContent()).substring(0, Math.min(100, result.getContent().length)));
            
        } catch (Exception e) {
            fail("导出测试失败: " + e.getMessage());
        }
    }
    
    @Test
    public void testExportDocumentPermissionDenied() {
        // 准备测试数据
        String docId = "test-doc-id";
        String userId = "test-user-id";
        ExportFormat format = ExportFormat.MARKDOWN;
        
        // 模拟权限被拒绝
        when(documentAccessService.hasReadPermission(userId, docId)).thenReturn(false);
        
        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            exportService.exportDocument(docId, format, userId);
        });
    }
}