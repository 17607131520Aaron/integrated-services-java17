package com.enterprise.integrated.controller;

import com.enterprise.integrated.annotation.IgnoreResponseWrapper;
import com.enterprise.integrated.utils.CacheUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/health")
@IgnoreResponseWrapper
public class HealthController {

    private final DataSource dataSource;
    private final CacheUtils cacheUtils;

    public HealthController(DataSource dataSource, CacheUtils cacheUtils) {
        this.dataSource = dataSource;
        this.cacheUtils = cacheUtils;
    }

    /**
     * 基础健康检查
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("application", "integrated-services-java17");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * 详细健康检查
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        Map<String, Object> components = new HashMap<>();
        
        // 检查数据库连接
        components.put("database", checkDatabase());
        
        // 检查Redis连接
        components.put("redis", checkRedis());
        
        // 检查内存使用情况
        components.put("memory", checkMemory());
        
        // 检查磁盘空间
        components.put("disk", checkDisk());
        
        boolean allHealthy = components.values().stream()
                .allMatch(component -> "UP".equals(((Map<?, ?>) component).get("status")));
        
        health.put("status", allHealthy ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        health.put("application", "integrated-services-java17");
        health.put("version", "1.0.0");
        health.put("components", components);
        
        return ResponseEntity.ok(health);
    }

    /**
     * 检查数据库连接
     */
    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbHealth = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            dbHealth.put("status", isValid ? "UP" : "DOWN");
            dbHealth.put("database", connection.getMetaData().getDatabaseProductName());
            dbHealth.put("validationQuery", "SELECT 1");
        } catch (Exception e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
        }
        return dbHealth;
    }

    /**
     * 检查Redis连接
     */
    private Map<String, Object> checkRedis() {
        Map<String, Object> redisHealth = new HashMap<>();
        try {
            String testKey = "health:check:" + System.currentTimeMillis();
            cacheUtils.set(testKey, "test", 10);
            Object valueObj = cacheUtils.get(testKey);
            String value = valueObj != null ? valueObj.toString() : null;
            cacheUtils.del(testKey);
            
            redisHealth.put("status", "test".equals(value) ? "UP" : "DOWN");
            redisHealth.put("ping", "PONG");
        } catch (Exception e) {
            redisHealth.put("status", "DOWN");
            redisHealth.put("error", e.getMessage());
        }
        return redisHealth;
    }

    /**
     * 检查内存使用情况
     */
    private Map<String, Object> checkMemory() {
        Map<String, Object> memoryHealth = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        memoryHealth.put("status", memoryUsagePercent < 90 ? "UP" : "DOWN");
        memoryHealth.put("max", formatBytes(maxMemory));
        memoryHealth.put("total", formatBytes(totalMemory));
        memoryHealth.put("used", formatBytes(usedMemory));
        memoryHealth.put("free", formatBytes(freeMemory));
        memoryHealth.put("usagePercent", String.format("%.2f%%", memoryUsagePercent));
        
        return memoryHealth;
    }

    /**
     * 检查磁盘空间
     */
    private Map<String, Object> checkDisk() {
        Map<String, Object> diskHealth = new HashMap<>();
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            double diskUsagePercent = (double) usedSpace / totalSpace * 100;
            
            diskHealth.put("status", diskUsagePercent < 90 ? "UP" : "DOWN");
            diskHealth.put("total", formatBytes(totalSpace));
            diskHealth.put("used", formatBytes(usedSpace));
            diskHealth.put("free", formatBytes(freeSpace));
            diskHealth.put("usagePercent", String.format("%.2f%%", diskUsagePercent));
        } catch (Exception e) {
            diskHealth.put("status", "DOWN");
            diskHealth.put("error", e.getMessage());
        }
        return diskHealth;
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
