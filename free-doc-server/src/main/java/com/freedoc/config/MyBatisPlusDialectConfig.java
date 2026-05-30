package com.freedoc.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus Database Dialect Configuration
 * 
 * Configures MyBatis-Plus to use appropriate SQL dialect for each database type.
 * Supports both SQLite and MySQL with automatic dialect selection based on
 * the detected database type.
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Configuration
public class MyBatisPlusDialectConfig {
    
    private final DatabaseTypeDetector databaseTypeDetector;
    
    public MyBatisPlusDialectConfig(DatabaseTypeDetector databaseTypeDetector) {
        this.databaseTypeDetector = databaseTypeDetector;
    }
    
    /**
     * Configures pagination plugin with appropriate dialect
     * 
     * @return MybatisPlusInterceptor configured with the correct database dialect
     */
    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        DatabaseTypeDetector.DatabaseType databaseType = databaseTypeDetector.detectDatabaseType();
        DbType dbType = getDbType(databaseType);
        
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(dbType);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        log.info("MyBatis-Plus pagination interceptor configured with dialect: {}", dbType);
        
        return interceptor;
    }
    
    /**
     * Returns DbType for MyBatis-Plus based on database type
     * 
     * @param type The detected database type
     * @return DbType.SQLITE for SQLite, DbType.MYSQL for MySQL
     */
    private DbType getDbType(DatabaseTypeDetector.DatabaseType type) {
        switch (type) {
            case SQLITE:
                return DbType.SQLITE;
            case MYSQL:
                return DbType.MYSQL;
            default:
                log.warn("Unknown database type: {}. Defaulting to MYSQL", type);
                return DbType.MYSQL;
        }
    }
}
