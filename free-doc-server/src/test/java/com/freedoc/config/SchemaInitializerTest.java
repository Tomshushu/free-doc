package com.freedoc.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SchemaInitializer
 * Tests the MySQL to SQLite schema adaptation logic
 */
class SchemaInitializerTest {

    private SchemaInitializer schemaInitializer;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to create instance without dependencies
        Constructor<SchemaInitializer> constructor = SchemaInitializer.class.getDeclaredConstructor(
            javax.sql.DataSource.class, DatabaseTypeDetector.class);
        constructor.setAccessible(true);
        schemaInitializer = constructor.newInstance(null, null);
    }
    /**
     * Test that adaptSchemaForSQLite removes ENGINE clause
     */
    @Test
    void testRemoveEngineClause() throws Exception {
        String input = "CREATE TABLE test (id INT) ENGINE = InnoDB;";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("ENGINE"), "ENGINE clause should be removed");
        assertFalse(result.contains("InnoDB"), "InnoDB should be removed");
    }

    /**
     * Test that adaptSchemaForSQLite removes CHARACTER SET and COLLATE
     */
    @Test
    void testRemoveCharacterSetAndCollate() throws Exception {
        String input = "CREATE TABLE test (name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci);";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("CHARACTER SET"), "CHARACTER SET should be removed");
        assertFalse(result.contains("COLLATE"), "COLLATE should be removed");
        assertFalse(result.contains("utf8mb4"), "utf8mb4 should be removed");
    }

    /**
     * Test that adaptSchemaForSQLite removes FULLTEXT INDEX
     */
    @Test
    void testRemoveFulltextIndex() throws Exception {
        String input = "CREATE TABLE test (title TEXT, content TEXT, FULLTEXT INDEX idx_title_content(title, content) WITH PARSER ngram);";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("FULLTEXT"), "FULLTEXT INDEX should be removed");
        assertFalse(result.contains("WITH PARSER"), "WITH PARSER should be removed");
    }

    /**
     * Test that adaptSchemaForSQLite converts AUTO_INCREMENT to AUTOINCREMENT
     */
    @Test
    void testConvertAutoIncrement() throws Exception {
        String input = "CREATE TABLE test (id INT AUTO_INCREMENT PRIMARY KEY);";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("AUTO_INCREMENT"), "AUTO_INCREMENT should be converted");
        assertTrue(result.contains("AUTOINCREMENT"), "Should contain AUTOINCREMENT");
    }

    /**
     * Test that adaptSchemaForSQLite replaces datetime with TEXT
     */
    @Test
    void testReplaceDatetime() throws Exception {
        String input = "CREATE TABLE test (created datetime, updated timestamp);";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("datetime"), "datetime should be replaced");
        assertFalse(result.contains("timestamp"), "timestamp should be replaced");
        assertTrue(result.contains("TEXT"), "Should contain TEXT");
    }

    /**
     * Test that adaptSchemaForSQLite replaces longtext with TEXT
     */
    @Test
    void testReplaceLongtext() throws Exception {
        String input = "CREATE TABLE test (content longtext);";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("longtext"), "longtext should be replaced");
        assertTrue(result.contains("TEXT"), "Should contain TEXT");
    }

    /**
     * Test that adaptSchemaForSQLite replaces tinyint(1) with INTEGER
     */
    @Test
    void testReplaceTinyint() throws Exception {
        String input = "CREATE TABLE test (is_active tinyint(1) DEFAULT 0);";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("tinyint"), "tinyint should be replaced");
        assertTrue(result.contains("INTEGER"), "Should contain INTEGER");
    }

    /**
     * Test that adaptSchemaForSQLite simplifies foreign key constraints
     */
    @Test
    void testSimplifyForeignKey() throws Exception {
        String input = "CONSTRAINT fk_test FOREIGN KEY (parent_id) REFERENCES parent(id) ON DELETE CASCADE ON UPDATE RESTRICT;";
        String result = invokeAdaptSchemaForSQLite(input);
        assertFalse(result.contains("ON UPDATE RESTRICT"), "ON UPDATE RESTRICT should be removed");
        assertTrue(result.contains("ON DELETE CASCADE"), "ON DELETE CASCADE should be preserved");
    }

    /**
     * Test complete schema adaptation with real schema file
     */
    @Test
    void testCompleteSchemaAdaptation() throws Exception {
        // Sample MySQL schema with all features that need adaptation
        String mysqlSchema = """
            SET NAMES utf8mb4;
            SET FOREIGN_KEY_CHECKS = 0;
            
            DROP TABLE IF EXISTS `doc`;
            CREATE TABLE `doc` (
              `doc_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档ID',
              `doc_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
              `doc_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
              `create_time` datetime NULL DEFAULT NULL,
              `is_active` tinyint(1) NULL DEFAULT 0,
              PRIMARY KEY (`doc_id`) USING BTREE,
              FULLTEXT INDEX `idx_doc_title_content`(`doc_title`, `doc_content`) WITH PARSER `ngram`
            ) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;
            
            CREATE TABLE `doc_comments` (
              `comment_id` varchar(64) NOT NULL,
              `parent_comment_id` varchar(64) NULL DEFAULT NULL,
              `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
              `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              PRIMARY KEY (`comment_id`),
              CONSTRAINT `doc_comments_ibfk_1` FOREIGN KEY (`parent_comment_id`) REFERENCES `doc_comments` (`comment_id`) ON DELETE CASCADE ON UPDATE RESTRICT
            ) ENGINE = InnoDB;
            
            SET FOREIGN_KEY_CHECKS = 1;
            """;
        
        String sqliteSchema = invokeAdaptSchemaForSQLite(mysqlSchema);
        
        // Verify all transformations
        assertFalse(sqliteSchema.contains("ENGINE"), "ENGINE should be removed");
        assertFalse(sqliteSchema.contains("CHARACTER SET"), "CHARACTER SET should be removed");
        assertFalse(sqliteSchema.contains("COLLATE"), "COLLATE should be removed");
        assertFalse(sqliteSchema.contains("FULLTEXT"), "FULLTEXT should be removed");
        assertFalse(sqliteSchema.contains("AUTO_INCREMENT"), "AUTO_INCREMENT should be converted");
        assertFalse(sqliteSchema.contains("ON UPDATE RESTRICT"), "ON UPDATE RESTRICT should be removed");
        assertFalse(sqliteSchema.contains("longtext"), "longtext should be replaced with TEXT");
        assertFalse(sqliteSchema.contains("datetime"), "datetime should be replaced with TEXT");
        assertFalse(sqliteSchema.contains("timestamp"), "timestamp should be replaced with TEXT");
        assertFalse(sqliteSchema.contains("tinyint"), "tinyint should be replaced with INTEGER");
        assertFalse(sqliteSchema.contains("DEFAULT CURRENT_TIMESTAMP"), "DEFAULT CURRENT_TIMESTAMP should be removed");
        
        // Verify the schema is not empty
        assertFalse(sqliteSchema.trim().isEmpty(), "Adapted schema should not be empty");
        
        // Verify CREATE TABLE statements are preserved
        assertTrue(sqliteSchema.contains("CREATE TABLE"), "CREATE TABLE statements should be preserved");
        
        // Verify ON DELETE CASCADE is preserved
        assertTrue(sqliteSchema.contains("ON DELETE CASCADE"), "ON DELETE CASCADE should be preserved");
    }

    /**
     * Helper method to invoke private adaptSchemaForSQLite method using reflection
     */
    private String invokeAdaptSchemaForSQLite(String mysqlSchema) throws Exception {
        Method method = SchemaInitializer.class.getDeclaredMethod("adaptSchemaForSQLite", String.class);
        method.setAccessible(true);
        return (String) method.invoke(schemaInitializer, mysqlSchema);
    }
}
