package com.freedoc.common.constants;

public interface Constants {

    String OWNER = "OWNER";
    String PARTICIPANT = "PARTICIPANT";

    String PERMISSION_READ = "r";
    String PERMISSION_READ_WRITE = "rw";

    String SHARE_TYPE_PROJECT = "PROJECT";
    String SHARE_TYPE_DOC = "DOC";

    // 导出相关常量
    interface Export {
        // 文件大小限制
        long MAX_SINGLE_FILE_SIZE = 50 * 1024 * 1024; // 50MB
        long MAX_BATCH_EXPORT_SIZE = 100 * 1024 * 1024; // 100MB
        
        // 批量导出限制
        int MAX_BATCH_FILES = 1000;
        int MAX_DIRECTORY_DEPTH = 10;
        
        // 文件名相关
        String DEFAULT_FILENAME = "document";
        String BATCH_EXPORT_PREFIX = "export_";
        String ZIP_EXTENSION = ".zip";
        
        // 缓存相关
        String CACHE_KEY_PREFIX = "export:";
        long CACHE_EXPIRE_MINUTES = 30;
        
        // 导出任务相关
        int EXPORT_THREAD_POOL_SIZE = 5;
        long EXPORT_TIMEOUT_SECONDS = 300; // 5分钟
        
        // 错误码
        String ERROR_DOCUMENT_NOT_FOUND = "DOCUMENT_NOT_FOUND";
        String ERROR_DIRECTORY_NOT_FOUND = "DIRECTORY_NOT_FOUND";
        String ERROR_ACCESS_DENIED = "ACCESS_DENIED";
        String ERROR_CONVERSION_FAILED = "CONVERSION_FAILED";
        String ERROR_FILE_TOO_LARGE = "FILE_TOO_LARGE";
        String ERROR_TOO_MANY_FILES = "TOO_MANY_FILES";
        String ERROR_EXPORT_TIMEOUT = "EXPORT_TIMEOUT";
        String ERROR_INVALID_FORMAT = "INVALID_FORMAT";
        String ERROR_NETWORK_ERROR = "NETWORK_ERROR";
    }

}
