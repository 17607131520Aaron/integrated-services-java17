package com.enterprise.integrated.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * API版本控制配置
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Bean
    public RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return new ApiVersionRequestMappingHandlerMapping();
    }
}
