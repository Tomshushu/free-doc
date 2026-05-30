package com.freedoc.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EnvironmentConfigManager单元测试
 * 
 * @author FreeDoc Team
 */
class EnvironmentConfigManagerTest {
    
    private EnvironmentConfigManager configManager;
    
    @BeforeEach
    void setUp() {
        configManager = new EnvironmentConfigManager();
    }
    
    @Test
    void testGetMySQLHost_WhenSet() {
        // 测试MySQL主机名获取
        ReflectionTestUtils.setField(configManager, "mysqlHost", "localhost");
        assertEquals("localhost", configManager.getMySQLHost());
    }
    
    @Test
    void testGetMySQLHost_WhenNull() {
        // 测试MySQL主机名为null
        ReflectionTestUtils.setField(configManager, "mysqlHost", null);
        assertNull(configManager.getMySQLHost());
    }
    
    @Test
    void testGetMySQLPort_Default() {
        // 测试MySQL端口默认值
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3306);
        assertEquals(3306, configManager.getMySQLPort());
    }
    
    @Test
    void testGetMySQLPort_Custom() {
        // 测试自定义MySQL端口
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3307);
        assertEquals(3307, configManager.getMySQLPort());
    }
    
    @Test
    void testGetMySQLDatabase_Default() {
        // 测试MySQL数据库名默认值
        ReflectionTestUtils.setField(configManager, "mysqlDatabase", "free_doc");
        assertEquals("free_doc", configManager.getMySQLDatabase());
    }
    
    @Test
    void testGetMySQLDatabase_Custom() {
        // 测试自定义MySQL数据库名
        ReflectionTestUtils.setField(configManager, "mysqlDatabase", "custom_db");
        assertEquals("custom_db", configManager.getMySQLDatabase());
    }
    
    @Test
    void testGetMySQLUser_Default() {
        // 测试MySQL用户名默认值
        ReflectionTestUtils.setField(configManager, "mysqlUser", "root");
        assertEquals("root", configManager.getMySQLUser());
    }
    
    @Test
    void testGetMySQLUser_Custom() {
        // 测试自定义MySQL用户名
        ReflectionTestUtils.setField(configManager, "mysqlUser", "admin");
        assertEquals("admin", configManager.getMySQLUser());
    }
    
    @Test
    void testGetMySQLPassword_WhenSet() {
        // 测试MySQL密码获取
        ReflectionTestUtils.setField(configManager, "mysqlPassword", "password123");
        assertEquals("password123", configManager.getMySQLPassword());
    }
    
    @Test
    void testGetMySQLPassword_WhenNull() {
        // 测试MySQL密码为null
        ReflectionTestUtils.setField(configManager, "mysqlPassword", null);
        assertNull(configManager.getMySQLPassword());
    }
    
    @Test
    void testGetServerPort_Default() {
        // 测试服务器端口默认值
        ReflectionTestUtils.setField(configManager, "serverPort", 9200);
        assertEquals(9200, configManager.getServerPort());
    }
    
    @Test
    void testGetServerPort_Custom() {
        // 测试自定义服务器端口
        ReflectionTestUtils.setField(configManager, "serverPort", 8080);
        assertEquals(8080, configManager.getServerPort());
    }
    
    @Test
    void testGetJwtSecret() {
        // 测试JWT密钥获取
        String secret = "TestSecretKey123456789012345678901234567890";
        ReflectionTestUtils.setField(configManager, "jwtSecret", secret);
        assertEquals(secret, configManager.getJwtSecret());
    }
    
    @Test
    void testGetUploadPath_Default() {
        // 测试上传路径默认值
        ReflectionTestUtils.setField(configManager, "uploadPath", "./uploads");
        assertEquals("./uploads", configManager.getUploadPath());
    }
    
    @Test
    void testGetUploadPath_Custom() {
        // 测试自定义上传路径
        ReflectionTestUtils.setField(configManager, "uploadPath", "/app/uploads");
        assertEquals("/app/uploads", configManager.getUploadPath());
    }
    
    @Test
    void testGetUploadMaxSize_Default() {
        // 测试上传文件大小限制默认值
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 52428800L);
        assertEquals(52428800L, configManager.getUploadMaxSize());
    }
    
    @Test
    void testGetUploadMaxSize_Custom() {
        // 测试自定义上传文件大小限制
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 104857600L);
        assertEquals(104857600L, configManager.getUploadMaxSize());
    }
    
    @Test
    void testValidateConfiguration_InvalidServerPort() {
        // 测试无效的服务器端口验证
        ReflectionTestUtils.setField(configManager, "serverPort", -1);
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3306);
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 52428800L);
        ReflectionTestUtils.setField(configManager, "jwtSecret", "ValidSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(configManager, "uploadPath", "./uploads");
        
        configManager.validateConfiguration();
        
        // 验证端口被重置为默认值
        assertEquals(9200, configManager.getServerPort());
    }
    
    @Test
    void testValidateConfiguration_InvalidMySQLPort() {
        // 测试无效的MySQL端口验证
        ReflectionTestUtils.setField(configManager, "serverPort", 9200);
        ReflectionTestUtils.setField(configManager, "mysqlPort", 70000);
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 52428800L);
        ReflectionTestUtils.setField(configManager, "jwtSecret", "ValidSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(configManager, "uploadPath", "./uploads");
        
        configManager.validateConfiguration();
        
        // 验证端口被重置为默认值
        assertEquals(3306, configManager.getMySQLPort());
    }
    
    @Test
    void testValidateConfiguration_NegativeUploadMaxSize() {
        // 测试负数上传文件大小限制验证
        ReflectionTestUtils.setField(configManager, "serverPort", 9200);
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3306);
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", -1L);
        ReflectionTestUtils.setField(configManager, "jwtSecret", "ValidSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(configManager, "uploadPath", "./uploads");
        
        configManager.validateConfiguration();
        
        // 验证大小被重置为默认值
        assertEquals(52428800L, configManager.getUploadMaxSize());
    }
    
    @Test
    void testValidateConfiguration_EmptyJwtSecret() {
        // 测试空JWT密钥验证
        ReflectionTestUtils.setField(configManager, "serverPort", 9200);
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3306);
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 52428800L);
        ReflectionTestUtils.setField(configManager, "jwtSecret", "");
        ReflectionTestUtils.setField(configManager, "uploadPath", "./uploads");
        
        configManager.validateConfiguration();
        
        // 验证密钥被重置为默认值
        assertEquals("FreeDocSecretKey2026ForJWTTokenGenerationAndValidation", configManager.getJwtSecret());
    }
    
    @Test
    void testValidateConfiguration_EmptyUploadPath() {
        // 测试空上传路径验证
        ReflectionTestUtils.setField(configManager, "serverPort", 9200);
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3306);
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 52428800L);
        ReflectionTestUtils.setField(configManager, "jwtSecret", "ValidSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(configManager, "uploadPath", "");
        
        configManager.validateConfiguration();
        
        // 验证路径被重置为默认值
        assertEquals("./uploads", configManager.getUploadPath());
    }
    
    @Test
    void testValidateConfiguration_ValidConfiguration() {
        // 测试有效配置验证
        ReflectionTestUtils.setField(configManager, "serverPort", 9200);
        ReflectionTestUtils.setField(configManager, "mysqlPort", 3306);
        ReflectionTestUtils.setField(configManager, "uploadMaxSize", 52428800L);
        ReflectionTestUtils.setField(configManager, "jwtSecret", "ValidSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(configManager, "uploadPath", "./uploads");
        ReflectionTestUtils.setField(configManager, "mysqlHost", "localhost");
        ReflectionTestUtils.setField(configManager, "mysqlDatabase", "free_doc");
        ReflectionTestUtils.setField(configManager, "mysqlUser", "root");
        ReflectionTestUtils.setField(configManager, "mysqlPassword", "password");
        
        // 不应抛出异常
        assertDoesNotThrow(() -> configManager.validateConfiguration());
    }
}
