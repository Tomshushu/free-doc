package com.freedoc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Environment Configuration Manager
 * 
 * Loads and validates environment variables for application configuration.
 * Provides centralized access to all environment-based configuration values.
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
public class EnvironmentConfigManager {
    
    // MySQL Database Configuration
    @Value("${MYSQL_HOST:#{null}}")
    private String mysqlHost;
    
    @Value("${MYSQL_PORT:#{null}}")
    private String mysqlPortStr;
    
    private int mysqlPort = 3306;
    
    @Value("${MYSQL_DATABASE:#{null}}")
    private String mysqlDatabase;
    
    @Value("${MYSQL_USER:#{null}}")
    private String mysqlUser;
    
    @Value("${MYSQL_PASSWORD:#{null}}")
    private String mysqlPassword;
    
    @Value("${MYSQL_USE_SSL:true}")
    private boolean mysqlUseSsl;
    
    // Application Configuration
    @Value("${SERVER_PORT:9200}")
    private int serverPort;
    
    @Value("${jwt.secret:#{null}}")
    private String jwtSecret;
    
    @Value("${upload.path:./uploads}")
    private String uploadPath;
    
    @Value("${upload.max-size:52428800}")
    private long uploadMaxSize;
    
    /**
     * Gets MySQL server hostname
     * 
     * @return MySQL host or null if not configured
     */
    public String getMySQLHost() {
        return mysqlHost;
    }
    
    /**
     * Gets MySQL server port
     * 
     * @return MySQL port (default: 3306)
     */
    public int getMySQLPort() {
        if (mysqlPortStr != null) {
            try {
                return Integer.parseInt(mysqlPortStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid MYSQL_PORT: {}. Using default: 3306", mysqlPortStr);
            }
        }
        return mysqlPort;
    }
    
    /**
     * Gets MySQL database name
     * 
     * @return MySQL database name
     */
    public String getMySQLDatabase() {
        return mysqlDatabase;
    }
    
    /**
     * Gets MySQL username
     * 
     * @return MySQL username
     */
    public String getMySQLUser() {
        return mysqlUser;
    }
    
    /**
     * Gets MySQL password
     * 
     * @return MySQL password or null if not configured
     */
    public String getMySQLPassword() {
        return mysqlPassword;
    }
    
    /**
     * Gets whether MySQL SSL is enabled
     * 
     * @return true if SSL is enabled (default: true)
     */
    public boolean getMySQLUseSsl() {
        return mysqlUseSsl;
    }
    
    /**
     * Gets HTTP server port
     * 
     * @return Server port (default: 9200)
     */
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * Gets JWT secret key
     * 
     * @return JWT secret key
     */
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    /**
     * Gets file upload directory path
     * 
     * @return Upload path (default: ./uploads)
     */
    public String getUploadPath() {
        return uploadPath;
    }
    
    /**
     * Gets maximum file upload size in bytes
     * 
     * @return Max upload size (default: 52428800 bytes = 50MB)
     */
    public long getUploadMaxSize() {
        return uploadMaxSize;
    }
    
    /**
     * Validates configuration and logs warnings for invalid values
     * Called automatically after bean construction
     */
    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating environment configuration...");
        
        // Validate JWT secret - required
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            log.error("JWT_SECRET is not configured! Please set JWT_SECRET environment variable.");
            throw new IllegalStateException("JWT_SECRET environment variable is required but not set. "
                + "Please set a secure JWT_SECRET (minimum 32 characters) and restart the application.");
        }
        if (jwtSecret.length() < 32) {
            log.error("JWT_SECRET is too short ({} characters). Minimum required: 32 characters.", jwtSecret.length());
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long for security.");
        }
        
        // Validate server port
        if (serverPort < 1 || serverPort > 65535) {
            log.warn("Invalid SERVER_PORT value: {}. Must be between 1 and 65535. Using default: 9200", serverPort);
            serverPort = 9200;
        }
        
        // Parse and validate MySQL port
        if (mysqlPortStr != null) {
            try {
                mysqlPort = Integer.parseInt(mysqlPortStr);
                if (mysqlPort < 1 || mysqlPort > 65535) {
                    log.warn("Invalid MYSQL_PORT value: {}. Must be between 1 and 65535. Using default: 3306", mysqlPort);
                    mysqlPort = 3306;
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid MYSQL_PORT value: {}. Using default: 3306", mysqlPortStr);
                mysqlPort = 3306;
            }
        }
        
        // Validate upload max size
        if (uploadMaxSize < 0) {
            log.warn("Invalid UPLOAD_MAX_SIZE value: {}. Must be non-negative. Using default: 52428800", uploadMaxSize);
            uploadMaxSize = 52428800L;
        }
        
        // Validate upload path
        if (uploadPath == null || uploadPath.trim().isEmpty()) {
            log.warn("UPLOAD_PATH is empty. Using default: ./uploads");
            uploadPath = "./uploads";
        }
        
        // Validate MySQL configuration if MySQL host is set
        if (mysqlHost != null && !mysqlHost.trim().isEmpty()) {
            if (mysqlDatabase == null || mysqlDatabase.trim().isEmpty()) {
                log.error("MYSQL_HOST is set but MYSQL_DATABASE is not configured.");
                throw new IllegalStateException("MYSQL_DATABASE environment variable is required when MYSQL_HOST is set.");
            }
            if (mysqlUser == null || mysqlUser.trim().isEmpty()) {
                log.error("MYSQL_HOST is set but MYSQL_USER is not configured.");
                throw new IllegalStateException("MYSQL_USER environment variable is required when MYSQL_HOST is set.");
            }
            if (mysqlPassword == null || mysqlPassword.trim().isEmpty()) {
                log.error("MYSQL_HOST is set but MYSQL_PASSWORD is not configured.");
                throw new IllegalStateException("MYSQL_PASSWORD environment variable is required when MYSQL_HOST is set.");
            }
        }
        
        log.info("Environment configuration validation completed");
        log.info("Configuration summary:");
        log.info("  - Server Port: {}", serverPort);
        log.info("  - Upload Path: {}", uploadPath);
        log.info("  - Upload Max Size: {} bytes ({} MB)", uploadMaxSize, uploadMaxSize / 1024 / 1024);
        log.info("  - JWT Secret Length: {} characters", jwtSecret.length());
        log.info("  - MySQL SSL Enabled: {}", mysqlUseSsl);
        
        if (mysqlHost != null && !mysqlHost.trim().isEmpty()) {
            log.info("  - MySQL Host: {}", mysqlHost);
            log.info("  - MySQL Port: {}", mysqlPort);
            log.info("  - MySQL Database: {}", mysqlDatabase);
            log.info("  - MySQL User: {}", mysqlUser);
        }
    }
}
