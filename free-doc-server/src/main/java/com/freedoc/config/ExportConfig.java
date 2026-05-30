package com.freedoc.config;

import com.freedoc.common.constants.Constants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 导出功能配置
 * 
 * @author FreeDoc Team
 */
@Configuration
@ConfigurationProperties(prefix = "freedoc.export")
@Data
public class ExportConfig {
    
    /**
     * 单个文件最大大小（字节）
     */
    private long maxSingleFileSize = Constants.Export.MAX_SINGLE_FILE_SIZE;
    
    /**
     * 批量导出最大总大小（字节）
     */
    private long maxBatchExportSize = Constants.Export.MAX_BATCH_EXPORT_SIZE;
    
    /**
     * 批量导出最大文件数
     */
    private int maxBatchFiles = Constants.Export.MAX_BATCH_FILES;
    
    /**
     * 最大目录深度
     */
    private int maxDirectoryDepth = Constants.Export.MAX_DIRECTORY_DEPTH;
    
    /**
     * 导出超时时间（秒）
     */
    private long exportTimeoutSeconds = Constants.Export.EXPORT_TIMEOUT_SECONDS;
    
    /**
     * 缓存过期时间（分钟）
     */
    private long cacheExpireMinutes = Constants.Export.CACHE_EXPIRE_MINUTES;
    
    /**
     * 线程池大小
     */
    private int threadPoolSize = Constants.Export.EXPORT_THREAD_POOL_SIZE;
    
    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;
    
    /**
     * 是否启用异步导出
     */
    private boolean asyncEnabled = true;
    
    /**
     * PDF导出配置
     */
    private PdfConfig pdf = new PdfConfig();
    
    /**
     * HTML导出配置
     */
    private HtmlConfig html = new HtmlConfig();
    
    /**
     * DOCX导出配置
     */
    private DocxConfig docx = new DocxConfig();
    
    /**
     * PDF导出配置
     */
    @Data
    public static class PdfConfig {
        /**
         * 页面大小（A4, A3, LETTER等）
         */
        private String pageSize = "A4";
        
        /**
         * 页边距（毫米）
         */
        private float marginTop = 20;
        private float marginBottom = 20;
        private float marginLeft = 20;
        private float marginRight = 20;
        
        /**
         * 字体设置
         */
        private String fontFamily = "SimSun";
        private float fontSize = 12;
        
        /**
         * 是否包含页眉页脚
         */
        private boolean includeHeader = false;
        private boolean includeFooter = true;
    }
    
    /**
     * HTML导出配置
     */
    @Data
    public static class HtmlConfig {
        /**
         * CSS样式文件路径
         */
        private String cssPath = "classpath:static/css/export.css";
        
        /**
         * 是否包含内联CSS
         */
        private boolean inlineCss = true;
        
        /**
         * 是否启用语法高亮
         */
        private boolean syntaxHighlight = true;
        
        /**
         * 代码主题
         */
        private String codeTheme = "github";
    }
    
    /**
     * DOCX导出配置
     */
    @Data
    public static class DocxConfig {
        /**
         * 字体设置
         */
        private String fontFamily = "SimSun";
        private int fontSize = 12;
        
        /**
         * 页边距（磅）
         */
        private int marginTop = 1440; // 1英寸
        private int marginBottom = 1440;
        private int marginLeft = 1440;
        private int marginRight = 1440;
        
        /**
         * 是否包含目录
         */
        private boolean includeTableOfContents = false;
    }
    
    /**
     * 导出任务执行器
     */
    @Bean("exportTaskExecutor")
    public Executor exportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolSize);
        executor.setMaxPoolSize(threadPoolSize * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("export-task-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}