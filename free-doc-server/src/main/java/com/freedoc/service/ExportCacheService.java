package com.freedoc.service;

import com.freedoc.common.enums.ExportFormat;

/**
 * 导出缓存服务接口
 * 
 * @author FreeDoc Team
 */
public interface ExportCacheService {
    
    /**
     * 获取缓存的导出结果
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 缓存的内容，如果不存在则返回null
     */
    byte[] getCachedExport(String docId, ExportFormat format, String userId);
    
    /**
     * 缓存导出结果
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @param content 导出内容
     */
    void cacheExport(String docId, ExportFormat format, String userId, byte[] content);
    
    /**
     * 清除指定文档的缓存
     * 
     * @param docId 文档ID
     */
    void clearDocumentCache(String docId);
    
    /**
     * 清除指定用户的所有缓存
     * 
     * @param userId 用户ID
     */
    void clearUserCache(String userId);
    
    /**
     * 清除所有缓存
     */
    void clearAllCache();
    
    /**
     * 检查缓存是否存在
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 是否存在缓存
     */
    boolean isCached(String docId, ExportFormat format, String userId);
}