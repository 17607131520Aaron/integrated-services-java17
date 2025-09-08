package com.enterprise.integrated;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 企业级集成服务启动类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 * @since 2025-09-08
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@EnableScheduling
@MapperScan("com.enterprise.integrated.mapper")
public class IntegratedServicesApplication {

    public static void main(String[] args) {
        // 设置系统属性
        System.setProperty("spring.application.admin.enabled", "true");
        
        SpringApplication.run(IntegratedServicesApplication.class, args);
        
        System.out.println("""
            
            ========================================
            🚀 企业级集成服务启动成功！
            📖 API文档地址: http://localhost:9090/api/doc.html
            📊 监控面板: http://localhost:9090/api/actuator
            💾 数据库监控: http://localhost:9090/api/druid
            ========================================
            
            """);
    }
}
