package com.freedoc.service.impl;

import com.freedoc.common.constants.Constants;
import com.freedoc.common.enums.ExportFormat;
import com.freedoc.config.ExportConfig;
import com.freedoc.service.ExportCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 导出缓存服务实现类
 * 基于内存的简单缓存实现
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportCacheServiceImpl implements ExportCacheService {
    
    private final ExportConfig exportConfig;
    
    // 缓存存储
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    // 定时清理器
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, "export-cache-cleanup"));
    
    /**
     * 缓存条目
     */
    private static class CacheEntry {
        private final byte[] content;
        private final long createTime;
        private final long expireTime;
        
        public CacheEntry(byte[] content, long expireMinutes) {
            this.content = content;
            this.createTime = System.currentTimeMillis();
            this.expireTime = createTime + TimeUnit.MINUTES.toMillis(expireMinutes);
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
        
        public byte[] getContent() {
            return content;
        }
    }
    
    // 启动定时清理任务
    {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredEntries, 
                5, 5, TimeUnit.MINUTES);
    }
    
    @Override
    public byte[] getCachedExport(String docId, ExportFormat format, String userId) {
        if (!exportConfig.isCacheEnabled()) {
            return null;
        }
        
        String cacheKey = buildCacheKey(docId, format, userId);
        CacheEntry entry = cache.get(cacheKey);
        
        if (entry == null) {
            log.debug("缓存未命中: {}", cacheKey);
            return null;
        }
        
        if (entry.isExpired()) {
            log.debug("缓存已过期: {}", cacheKey);
            cache.remove(cacheKey);
            return null;
        }
        
        log.debug("缓存命中: {}", cacheKey);
        return entry.getContent();
    }
    
    @Override
    public void cacheExport(String docId, ExportFormat format, String userId, byte[] content) {
        if (!exportConfig.isCacheEnabled() || content == null) {
            return;
        }
        
        String cacheKey = buildCacheKey(docId, format, userId);
        CacheEntry entry = new CacheEntry(content, exportConfig.getCacheExpireMinutes());
        
        cache.put(cacheKey, entry);
        log.debug("缓存导出结果: key={}, size={}", cacheKey, content.length);
    }
    
    @Override
    public void clearDocumentCache(String docId) {
        String prefix = Constants.Export.CACHE_KEY_PREFIX + docId + ":";
        
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix));
        log.debug("清除文档缓存: docId={}", docId);
    }
    
    @Override
    public void clearUserCache(String userId) {
        cache.entrySet().removeIf(entry -> entry.getKey().contains(":" + userId));
        log.debug("清除用户缓存: userId={}", userId);
    }
    
    @Override
    public void clearAllCache() {
        cache.clear();
        log.info("清除所有导出缓存");
    }
    
    @Override
    public boolean isCached(String docId, ExportFormat format, String userId) {
        if (!exportConfig.isCacheEnabled()) {
            return false;
        }
        
        String cacheKey = buildCacheKey(docId, format, userId);
        CacheEntry entry = cache.get(cacheKey);
        
        return entry != null && !entry.isExpired();
    }
    
    /**
     * 构建缓存键
     * 
     * @param docId 文档ID
     * @param format 导出格式
     * @param userId 用户ID
     * @return 缓存键
     */
    private String buildCacheKey(String docId, ExportFormat format, String userId) {
        return Constants.Export.CACHE_KEY_PREFIX + docId + ":" + format.name() + ":" + userId;
    }
    
    /**
     * 清理过期的缓存条目
     */
    private void cleanupExpiredEntries() {
        try {
            int removedCount = 0;
            for (var iterator = cache.entrySet().iterator(); iterator.hasNext(); ) {
                var entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    iterator.remove();
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                log.debug("清理过期缓存条目: {} 个", removedCount);
            }
        } catch (Exception e) {
            log.error("清理缓存时发生错误", e);
        }
    }
}