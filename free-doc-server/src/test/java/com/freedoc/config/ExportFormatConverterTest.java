package com.freedoc.config;

import com.freedoc.common.enums.ExportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExportFormatConverter单元测试
 * 
 * @author FreeDoc Team
 */
class ExportFormatConverterTest {
    
    private ExportFormatConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new ExportFormatConverter();
    }
    
    @Test
    void testConvert_EnumName_Uppercase() {
        // 测试大写枚举名称
        assertEquals(ExportFormat.MARKDOWN, converter.convert("MARKDOWN"));
        assertEquals(ExportFormat.HTML, converter.convert("HTML"));
        assertEquals(ExportFormat.PDF, converter.convert("PDF"));
        assertEquals(ExportFormat.DOCX, converter.convert("DOCX"));
    }
    
    @Test
    void testConvert_EnumName_Lowercase() {
        // 测试小写枚举名称
        assertEquals(ExportFormat.MARKDOWN, converter.convert("markdown"));
        assertEquals(ExportFormat.HTML, converter.convert("html"));
        assertEquals(ExportFormat.PDF, converter.convert("pdf"));
        assertEquals(ExportFormat.DOCX, converter.convert("docx"));
    }
    
    @Test
    void testConvert_EnumName_MixedCase() {
        // 测试混合大小写枚举名称
        assertEquals(ExportFormat.MARKDOWN, converter.convert("Markdown"));
        assertEquals(ExportFormat.HTML, converter.convert("Html"));
        assertEquals(ExportFormat.PDF, converter.convert("Pdf"));
        assertEquals(ExportFormat.DOCX, converter.convert("Docx"));
    }
    
    @Test
    void testConvert_FileExtension() {
        // 测试文件扩展名
        assertEquals(ExportFormat.MARKDOWN, converter.convert("md"));
        assertEquals(ExportFormat.HTML, converter.convert("html"));
        assertEquals(ExportFormat.PDF, converter.convert("pdf"));
        assertEquals(ExportFormat.DOCX, converter.convert("docx"));
    }
    
    @Test
    void testConvert_FileExtension_Uppercase() {
        // 测试大写文件扩展名
        assertEquals(ExportFormat.MARKDOWN, converter.convert("MD"));
        assertEquals(ExportFormat.HTML, converter.convert("HTML"));
        assertEquals(ExportFormat.PDF, converter.convert("PDF"));
        assertEquals(ExportFormat.DOCX, converter.convert("DOCX"));
    }
    
    @Test
    void testConvert_WithWhitespace() {
        // 测试带空格的输入
        assertEquals(ExportFormat.MARKDOWN, converter.convert(" MARKDOWN "));
        assertEquals(ExportFormat.HTML, converter.convert(" html "));
        assertEquals(ExportFormat.MARKDOWN, converter.convert(" md "));
    }
    
    @Test
    void testConvert_NullInput() {
        // 测试null输入
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(null)
        );
        assertEquals("导出格式不能为空", exception.getMessage());
    }
    
    @Test
    void testConvert_EmptyInput() {
        // 测试空字符串输入
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert("")
        );
        assertEquals("导出格式不能为空", exception.getMessage());
    }
    
    @Test
    void testConvert_WhitespaceOnlyInput() {
        // 测试只有空格的输入
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert("   ")
        );
        assertEquals("导出格式不能为空", exception.getMessage());
    }
    
    @Test
    void testConvert_UnsupportedFormat() {
        // 测试不支持的格式
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert("txt")
        );
        assertTrue(exception.getMessage().contains("不支持的导出格式"));
        assertTrue(exception.getMessage().contains("txt"));
    }
    
    @Test
    void testConvert_InvalidFormat() {
        // 测试无效格式
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert("invalid")
        );
        assertTrue(exception.getMessage().contains("不支持的导出格式"));
    }
}
