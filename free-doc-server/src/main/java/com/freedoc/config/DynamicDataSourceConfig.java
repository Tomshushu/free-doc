package com.freedoc.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.File;

/**
 * Dynamic DataSource Configuration
 * 
 * Configures Spring DataSource based on detected database type.
 * Supports both SQLite (default) and MySQL databases with appropriate
 * connection pool settings and database-specific configurations.
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Configuration
public class DynamicDataSourceConfig {
    
    private final DatabaseTypeDetector detector;
    private final EnvironmentConfigManager envConfig;
    
    public DynamicDataSourceConfig(DatabaseTypeDetector detector, EnvironmentConfigManager envConfig) {
        this.detector = detector;
        this.envConfig = envConfig;
    }
    
    /**
     * Creates DataSource bean based on detected database type
     * 
     * @return Configured DataSource (SQLite or MySQL)
     */
    @Bean
    public DataSource dataSource() {
        DatabaseTypeDetector.DatabaseType dbType = detector.detectDatabaseType();
        
        DataSource dataSource;
        if (dbType == DatabaseTypeDetector.DatabaseType.MYSQL) {
            // Validate MySQL configuration before creating datasource
            detector.validateMySQLConfig();
            dataSource = createMySQLDataSource();
            log.info("MySQL DataSource configured successfully");
        } else {
            dataSource = createSQLiteDataSource();
            log.info("SQLite DataSource configured successfully");
        }
        
        return dataSource;
    }
    
    /**
     * Creates SQLite DataSource with HikariCP connection pool
     * 
     * Configuration:
     * - URL: jdbc:sqlite:/app/data/freedoc.db
     * - Max pool size: 10 connections
     * - SQLite pragmas: journal_mode=WAL, synchronous=NORMAL, foreign_keys=ON
     * 
     * @return Configured SQLite DataSource
     */
    private DataSource createSQLiteDataSource() {
        log.info("Creating SQLite DataSource...");
        
        // Ensure data directory exists
        File dataDir = new File("/app/data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                log.info("Created data directory: /app/data");
            } else {
                log.warn("Failed to create data directory: /app/data (may already exist or permission issue)");
            }
        }
        
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:/app/data/freedoc.db");
        
        // Connection pool settings for SQLite
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        // SQLite-specific pragmas for better performance and data integrity
        config.addDataSourceProperty("journal_mode", "WAL"); // Write-Ahead Logging for better concurrency
        config.addDataSourceProperty("synchronous", "NORMAL"); // Balance between safety and performance
        config.addDataSourceProperty("foreign_keys", "ON"); // Enable foreign key constraints
        
        // Connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        // Pool name for logging
        config.setPoolName("SQLiteHikariPool");
        
        log.info("SQLite DataSource configuration:");
        log.info("  - JDBC URL: {}", config.getJdbcUrl());
        log.info("  - Max Pool Size: {}", config.getMaximumPoolSize());
        log.info("  - Journal Mode: WAL");
        log.info("  - Foreign Keys: ON");
        
        return new HikariDataSource(config);
    }
    
    /**
     * Creates MySQL DataSource with HikariCP connection pool
     * 
     * Configuration:
     * - URL: Built from environment variables
     * - Max pool size: 20 connections
     * - MySQL-specific connection parameters
     * 
     * @return Configured MySQL DataSource
     */
    private DataSource createMySQLDataSource() {
        log.info("Creating MySQL DataSource...");
        
        String host = envConfig.getMySQLHost();
        int port = envConfig.getMySQLPort();
        String database = envConfig.getMySQLDatabase();
        String user = envConfig.getMySQLUser();
        String password = envConfig.getMySQLPassword();
        
        // Build JDBC URL with MySQL-specific parameters
        String useSsl = envConfig.getMySQLUseSsl() ? "true" : "false";
        String jdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=%s&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
            host, port, database, useSsl
        );
        
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        
        // Connection pool settings for MySQL
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        
        // MySQL-specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        // Pool name for logging
        config.setPoolName("MySQLHikariPool");
        
        log.info("MySQL DataSource configuration:");
        log.info("  - JDBC URL: jdbc:mysql://{}:{}/{}", host, port, database);
        log.info("  - Username: {}", user);
        log.info("  - Max Pool Size: {}", config.getMaximumPoolSize());
        
        return new HikariDataSource(config);
    }
}
