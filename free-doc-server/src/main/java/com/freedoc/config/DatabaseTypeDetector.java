package com.freedoc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseTypeDetector {

    public enum DatabaseType {
        SQLITE, MYSQL
    }

    private volatile DatabaseType cachedType;
    private volatile boolean detected = false;

    public DatabaseType detectDatabaseType() {
        if (detected) {
            return cachedType;
        }

        String dbType = System.getenv("DB_TYPE");
        if (dbType != null && !dbType.trim().isEmpty()) {
            String type = dbType.trim().toUpperCase();
            if ("MYSQL".equals(type)) {
                cachedType = DatabaseType.MYSQL;
                log.info("Database type detected: MYSQL (from DB_TYPE config)");
            } else {
                cachedType = DatabaseType.SQLITE;
                log.info("Database type detected: SQLITE (from DB_TYPE config)");
            }
        } else {
            String mysqlHost = System.getenv("MYSQL_HOST");
            if (mysqlHost != null && !mysqlHost.trim().isEmpty()) {
                cachedType = DatabaseType.MYSQL;
                log.info("Database type detected: MYSQL (MYSQL_HOST={})", mysqlHost);
            } else {
                cachedType = DatabaseType.SQLITE;
                log.info("Database type detected: SQLITE (default - no MYSQL_HOST or DB_TYPE configured)");
            }
        }

        detected = true;
        return cachedType;
    }

    public void validateMySQLConfig() {
        String mysqlHost = System.getenv("MYSQL_HOST");
        String mysqlDatabase = System.getenv("MYSQL_DATABASE");
        String mysqlUser = System.getenv("MYSQL_USER");
        String mysqlPassword = System.getenv("MYSQL_PASSWORD");

        StringBuilder missingParams = new StringBuilder();

        if (mysqlHost == null || mysqlHost.trim().isEmpty()) {
            missingParams.append("MYSQL_HOST ");
        }
        if (mysqlDatabase == null || mysqlDatabase.trim().isEmpty()) {
            missingParams.append("MYSQL_DATABASE ");
        }
        if (mysqlUser == null || mysqlUser.trim().isEmpty()) {
            missingParams.append("MYSQL_USER ");
        }
        if (mysqlPassword == null || mysqlPassword.trim().isEmpty()) {
            missingParams.append("MYSQL_PASSWORD ");
        }

        if (missingParams.length() > 0) {
            String errorMessage = "Missing required MySQL configuration parameters: " + missingParams.toString().trim();
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.info("MySQL configuration validated successfully");
    }
}
