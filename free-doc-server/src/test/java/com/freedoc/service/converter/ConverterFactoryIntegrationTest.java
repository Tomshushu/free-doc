package com.freedoc.service.converter;

import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 转换器工厂集成测试
 * 验证PdfConverter是否正确集成到系统中
 * 
 * @author FreeDoc Team
 */
@SpringBootTest(classes = {
    ConverterFactory.class,
    MarkdownConverter.class,
    HtmlConverter.class,
    PdfConverter.class,
    DocxConverter.class,
    ExportConfig.class,
    com.freedoc.config.ConverterConfig.class
})
@TestPropertySource(properties = {
    "freedoc.export.pdf.pageSize=A4",
    "freedoc.export.pdf.fontFamily=SimSun",
    "freedoc.export.html.cssPath=classpath:static/css/export.css"
})
class ConverterFactoryIntegrationTest {
    
    @Autowired
    private ConverterFactory converterFactory;
    
    @Test
    void testPdfConverterIsRegistered() {
        // 验证PDF格式被支持
        assertTrue(converterFactory.isSupported(ExportFormat.PDF));
        
        // 验证可以获取PDF转换器
        FormatConverter pdfConverter = converterFactory.getConverter(ExportFormat.PDF);
        assertNotNull(pdfConverter);
        assertInstanceOf(PdfConverter.class, pdfConverter);
        
        // 验证转换器返回正确的格式
        assertEquals(ExportFormat.PDF, pdfConverter.getSupportedFormat());
    }
    
    @Test
    void testAllExpectedConvertersAreRegistered() {
        List<ExportFormat> supportedFormats = converterFactory.getSupportedFormats();
        
        // 验证所有预期的格式都被支持
        assertTrue(supportedFormats.contains(ExportFormat.MARKDOWN));
        assertTrue(supportedFormats.contains(ExportFormat.HTML));
        assertTrue(supportedFormats.contains(ExportFormat.PDF));
        assertTrue(supportedFormats.contains(ExportFormat.DOCX));
        
        // 验证至少有4个转换器（MD, HTML, PDF, DOCX）
        assertTrue(supportedFormats.size() >= 4);
    }
    
    @Test
    void testGetConverterForEachFormat() {
        List<ExportFormat> supportedFormats = converterFactory.getSupportedFormats();
        
        // 验证每个支持的格式都能获取到对应的转换器
        for (ExportFormat format : supportedFormats) {
            FormatConverter converter = converterFactory.getConverter(format);
            assertNotNull(converter, "转换器不应为空: " + format);
            assertEquals(format, converter.getSupportedFormat(), "转换器格式不匹配: " + format);
        }
    }
    
    @Test
    void testDocxConverterIsRegistered() {
        // 验证DOCX格式被支持
        assertTrue(converterFactory.isSupported(ExportFormat.DOCX));
        
        // 验证可以获取DOCX转换器
        FormatConverter docxConverter = converterFactory.getConverter(ExportFormat.DOCX);
        assertNotNull(docxConverter);
        assertInstanceOf(DocxConverter.class, docxConverter);
        
        // 验证转换器返回正确的格式
        assertEquals(ExportFormat.DOCX, docxConverter.getSupportedFormat());
    }
    
    @Test
    void testDocxConverterProperties() {
        FormatConverter docxConverter = converterFactory.getConverter(ExportFormat.DOCX);
        
        // 验证MIME类型和文件扩展名
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxConverter.getMimeType());
        assertEquals("docx", docxConverter.getFileExtension());
    }
    
    @Test
    void testPdfConverterProperties() {
        FormatConverter pdfConverter = converterFactory.getConverter(ExportFormat.PDF);
        
        // 验证MIME类型和文件扩展名
        assertEquals("application/pdf", pdfConverter.getMimeType());
        assertEquals("pdf", pdfConverter.getFileExtension());
    }
}