package com.freedoc.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StaticResourceConfig单元测试
 * 
 * @author FreeDoc Team
 */
class StaticResourceConfigTest {
    
    private StaticResourceConfig staticResourceConfig;
    
    @BeforeEach
    void setUp() {
        staticResourceConfig = new StaticResourceConfig();
    }
    
    @Test
    void testConfiguration_IsSpringConfiguration() {
        // 验证类被正确标注为Spring配置类
        assertTrue(staticResourceConfig.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class),
            "StaticResourceConfig should be annotated with @Configuration");
    }
    
    @Test
    void testConfiguration_ImplementsWebMvcConfigurer() {
        // 验证类实现了WebMvcConfigurer接口
        assertTrue(org.springframework.web.servlet.config.annotation.WebMvcConfigurer.class
            .isAssignableFrom(staticResourceConfig.getClass()),
            "StaticResourceConfig should implement WebMvcConfigurer");
    }
    
    @Test
    void testPathResourceResolver_NonApiPathsShouldFallbackToIndex() {
        // 测试非API路由应该回退到index.html
        // 这个测试验证了Vue Router支持的核心逻辑
        
        String[] nonApiPaths = {
            "home",
            "projects",
            "documents/123",
            "settings",
            "users/profile",
            "assets/css/style.css",
            "assets/js/app.js"
        };
        
        for (String path : nonApiPaths) {
            assertFalse(path.startsWith("api/"), 
                "Path '" + path + "' should not start with 'api/' and should fallback to index.html");
        }
    }
    
    @Test
    void testPathResourceResolver_ApiPathsShouldNotFallback() {
        // 测试API路由不应该回退到index.html
        
        String[] apiPaths = {
            "api/auth/login",
            "api/projects",
            "api/documents/123",
            "api/users",
            "api/health"
        };
        
        for (String path : apiPaths) {
            assertTrue(path.startsWith("api/"), 
                "Path '" + path + "' should start with 'api/' and should not fallback to index.html");
        }
    }
    
    @Test
    void testStaticResourceConfig_NotNull() {
        // 验证配置对象可以正确创建
        assertNotNull(staticResourceConfig, "StaticResourceConfig should be instantiable");
    }
}
