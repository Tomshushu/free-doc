package com.freedoc.controller;

import com.freedoc.config.DatabaseTypeDetector;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Health Check Controller
 * 
 * Provides container health status for Docker HEALTHCHECK.
 * Returns 200 if application is healthy, 503 if database is unreachable.
 * 
 * @author FreeDoc Team
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private final DataSource dataSource;
    private final DatabaseTypeDetector detector;
    
    public HealthController(DataSource dataSource, DatabaseTypeDetector detector) {
        this.dataSource = dataSource;
        this.detector = detector;
    }
    
    /**
     * Health check endpoint
     * 
     * @return 200 if application is healthy, 503 if database is unreachable
     */
    @GetMapping
    public ResponseEntity<HealthStatus> health() {
        HealthStatus status = new HealthStatus();
        status.setTimestamp(ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        
        DatabaseTypeDetector.DatabaseType dbType = detector.detectDatabaseType();
        status.setDatabase(dbType.name());
        
        if (isDatabaseHealthy()) {
            status.setStatus("UP");
            log.debug("Health check: UP (database: {})", dbType);
            return ResponseEntity.ok(status);
        } else {
            status.setStatus("DOWN");
            log.warn("Health check: DOWN (database: {} is unreachable)", dbType);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }
    
    /**
     * Tests database connectivity
     * 
     * @return true if database is accessible, false otherwise
     */
    private boolean isDatabaseHealthy() {
        try (Connection conn = dataSource.getConnection()) {
            // Try to execute a simple query
            return conn.isValid(3); // 3 seconds timeout
        } catch (Exception e) {
            log.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Health status response model
     */
    @Data
    public static class HealthStatus {
        private String status;
        private String database;
        private String timestamp;
    }
}
