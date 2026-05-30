package com.freedoc.config;

import com.freedoc.common.util.PasswordUtil;
import com.freedoc.common.util.SnowflakeIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Schema Initializer
 * 
 * Automatically creates database schema and seeds initial data on first startup.
 * Supports both SQLite and MySQL databases with appropriate schema adaptation.
 * 
 * @author FreeDoc Team
 */
@Slf4j
@Component
public class SchemaInitializer implements ApplicationRunner {
    
    private final DataSource dataSource;
    private final DatabaseTypeDetector detector;
    
    @Value("${DEFAULT_ADMIN_PASSWORD:admin123}")
    private String defaultAdminPassword;
    
    public SchemaInitializer(DataSource dataSource, DatabaseTypeDetector detector) {
        this.dataSource = dataSource;
        this.detector = detector;
    }
    
    /**
     * Executes after Spring context initialization
     * Checks if schema exists and creates it if necessary
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting schema initialization check...");
        
        DatabaseTypeDetector.DatabaseType dbType = detector.detectDatabaseType();
        
        if (schemaExists()) {
            log.info("Database schema already exists. Skipping initialization.");
            return;
        }
        
        log.info("Database schema does not exist. Creating schema...");
        createSchema(dbType);
        
        log.info("Seeding initial data...");
        seedInitialData();
        
        log.info("Schema initialization completed successfully");
    }
    
    /**
     * Checks if schema exists by querying for doc_user table
     * 
     * @return true if schema exists, false otherwise
     */
    private boolean schemaExists() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            DatabaseTypeDetector.DatabaseType dbType = detector.detectDatabaseType();
            
            // Try to query the doc_user table
            String query;
            if (dbType == DatabaseTypeDetector.DatabaseType.SQLITE) {
                // SQLite: Check sqlite_master table
                query = "SELECT name FROM sqlite_master WHERE type='table' AND name='doc_user'";
            } else {
                // MySQL: Check information_schema
                query = "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_NAME = 'doc_user'";
            }
            
            try (ResultSet rs = stmt.executeQuery(query)) {
                boolean exists = rs.next();
                log.info("Schema existence check: {}", exists ? "EXISTS" : "NOT EXISTS");
                return exists;
            }
            
        } catch (Exception e) {
            log.warn("Error checking schema existence: {}. Assuming schema does not exist.", e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates all tables from schema definition
     * Loads appropriate schema file based on database type
     * 
     * @param dbType The database type (SQLITE or MYSQL)
     */
    private void createSchema(DatabaseTypeDetector.DatabaseType dbType) throws Exception {
        // Choose appropriate schema file based on database type
        String schemaFile;
        if (dbType == DatabaseTypeDetector.DatabaseType.SQLITE) {
            schemaFile = "db/free_doc_sqlite.sql";
            log.info("Loading SQLite schema from {}...", schemaFile);
        } else {
            schemaFile = "db/free_doc_mysql.sql";
            log.info("Loading MySQL schema from {}...", schemaFile);
        }
        
        // Read SQL schema file from classpath
        ClassPathResource resource = new ClassPathResource(schemaFile);
        StringBuilder sqlContent = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sqlContent.append(line).append("\n");
            }
        }
        
        String schema = sqlContent.toString();
        
        // Execute schema creation
        log.info("Executing schema creation statements...");
        executeSchemaStatements(schema);
        
        log.info("Schema created successfully");
    }
    
    /**
     * Executes SQL statements from schema string
     * 
     * @param schema SQL schema string
     */
    private void executeSchemaStatements(String schema) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Split by semicolon and execute each statement
            String[] statements = schema.split(";");
            
            int executedCount = 0;
            for (String sql : statements) {
                String trimmed = sql.trim();
                
                // Skip empty statements and comments
                if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("/*")) {
                    continue;
                }
                
                // Skip DROP TABLE statements (we want to preserve existing data if any)
                if (trimmed.toUpperCase().startsWith("DROP TABLE")) {
                    log.debug("Skipping DROP TABLE statement");
                    continue;
                }
                
                try {
                    stmt.execute(trimmed);
                    executedCount++;
                    
                    // Log table creation
                    if (trimmed.toUpperCase().contains("CREATE TABLE")) {
                        String tableName = extractTableName(trimmed);
                        log.info("Created table: {}", tableName);
                    }
                } catch (Exception e) {
                    log.error("Error executing SQL statement: {}", trimmed.substring(0, Math.min(100, trimmed.length())));
                    log.error("Error details: {}", e.getMessage());
                    throw e;
                }
            }
            
            log.info("Executed {} SQL statements successfully", executedCount);
        }
    }
    
    /**
     * Extracts table name from CREATE TABLE statement
     * 
     * @param createTableSql CREATE TABLE SQL statement
     * @return Table name
     */
    private String extractTableName(String createTableSql) {
        try {
            String upper = createTableSql.toUpperCase();
            int startIdx = upper.indexOf("CREATE TABLE");
            if (startIdx == -1) {
                return "unknown";
            }
            
            String afterCreate = createTableSql.substring(startIdx + "CREATE TABLE".length()).trim();
            
            // Remove IF NOT EXISTS if present
            if (afterCreate.toUpperCase().startsWith("IF NOT EXISTS")) {
                afterCreate = afterCreate.substring("IF NOT EXISTS".length()).trim();
            }
            
            // Extract table name (until space or opening parenthesis)
            int endIdx = afterCreate.indexOf(' ');
            int parenIdx = afterCreate.indexOf('(');
            
            if (endIdx == -1) {
                endIdx = parenIdx;
            } else if (parenIdx != -1 && parenIdx < endIdx) {
                endIdx = parenIdx;
            }
            
            if (endIdx == -1) {
                return afterCreate.trim();
            }
            
            String tableName = afterCreate.substring(0, endIdx).trim();
            
            // Remove backticks if present
            tableName = tableName.replace("`", "");
            
            return tableName;
        } catch (Exception e) {
            log.warn("Failed to extract table name: {}", e.getMessage());
            return "unknown";
        }
    }
    
    /**
     * Inserts default admin user
     * 
     * Creates admin user with:
     * - user_id: Generated using SnowflakeIdUtil
     * - account: "admin"
     * - password: MD5 hash of "admin123" with salt
     * - user_name: "Administrator"
     * - create_time: Current timestamp
     */
    private void seedInitialData() throws Exception {
        log.info("Creating default admin user...");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Generate user ID and salt
            String userId = SnowflakeIdUtil.nextId();
            String salt = UUID.randomUUID().toString().replace("-", "");
            String password = PasswordUtil.encrypt(defaultAdminPassword, salt);
            
            // Get current timestamp
            String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Insert admin user
            String insertSql = String.format(
                "INSERT INTO doc_user (user_id, user_name, user_icon, account, password, salt, create_user, create_time) " +
                "VALUES ('%s', 'Administrator', NULL, 'admin', '%s', '%s', '%s', '%s')",
                userId, password, salt, userId, createTime
            );
            
            stmt.execute(insertSql);
            
            log.info("Default admin user created successfully");
            log.info("  - Account: admin");
            log.info("  - User ID: {}", userId);
            log.warn("SECURITY WARNING: Please change the default admin password after first login!");
            
        } catch (Exception e) {
            log.error("Error creating default admin user: {}", e.getMessage());
            log.warn("You may need to create an admin user manually");
            // Don't throw exception - this is non-critical
        }
    }
}
