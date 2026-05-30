package com.freedoc.common.util;

import com.freedoc.common.constants.Constants;
import com.freedoc.common.enums.ExportFormat;
import cn.hutool.core.util.StrUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 导出工具类
 * 
 * @author FreeDoc Team
 */
public class ExportUtil {
    
    private static final Pattern INVALID_FILENAME_CHARS = Pattern.compile("[\\\\/:*?\"<>|]");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    /**
     * 生成安全的文件名
     * 
     * @param title 文档标题
     * @param format 导出格式
     * @return 安全的文件名
     */
    public static String generateSafeFilename(String title, ExportFormat format) {
        if (StrUtil.isBlank(title)) {
            title = Constants.Export.DEFAULT_FILENAME;
        }
        
        // 移除或替换非法字符
        String safeTitle = INVALID_FILENAME_CHARS.matcher(title.trim()).replaceAll("_");
        
        // 限制文件名长度
        if (safeTitle.length() > 100) {
            safeTitle = safeTitle.substring(0, 100);
        }
        
        return safeTitle + "." + format.getExtension();
    }
    
    /**
     * 生成批量导出的ZIP文件名
     * 
     * @param directoryName 目录名称
     * @param format 导出格式
     * @return ZIP文件名
     */
    public static String generateBatchFilename(String directoryName, ExportFormat format) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String safeName = generateSafeBasename(directoryName);
        
        return Constants.Export.BATCH_EXPORT_PREFIX + safeName + "_" + 
               format.name().toLowerCase() + "_" + timestamp + Constants.Export.ZIP_EXTENSION;
    }
    
    /**
     * 生成安全的基础文件名（不包含扩展名）
     * 
     * @param name 原始名称
     * @return 安全的基础文件名
     */
    public static String generateSafeBasename(String name) {
        if (StrUtil.isBlank(name)) {
            return Constants.Export.DEFAULT_FILENAME;
        }
        
        String safeName = INVALID_FILENAME_CHARS.matcher(name.trim()).replaceAll("_");
        
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }
        
        return safeName;
    }
    
    /**
     * 格式化文件大小
     * 
     * @param bytes 字节数
     * @return 格式化后的大小字符串
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 验证文件大小是否超出限制
     * 
     * @param size 文件大小
     * @param maxSize 最大允许大小
     * @return 是否超出限制
     */
    public static boolean isFileSizeExceeded(long size, long maxSize) {
        return size > maxSize;
    }
    
    /**
     * 生成缓存键
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 缓存键
     */
    public static String generateCacheKey(String docId, ExportFormat format, String userId) {
        return Constants.Export.CACHE_KEY_PREFIX + docId + ":" + format.name() + ":" + userId;
    }
    
    /**
     * 清理临时文件路径
     * 
     * @param path 文件路径
     * @return 清理后的路径
     */
    public static String sanitizePath(String path) {
        if (StrUtil.isBlank(path)) {
            return "";
        }
        
        // 移除路径遍历攻击字符
        return path.replaceAll("\\.\\./", "").replaceAll("\\\\", "/");
    }
    
    /**
     * 验证导出格式是否支持
     * 
     * @param format 格式字符串
     * @return 是否支持
     */
    public static boolean isSupportedFormat(String format) {
        return ExportFormat.isSupported(format);
    }
}