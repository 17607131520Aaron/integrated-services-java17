package com.enterprise.integrated.config;

import com.enterprise.integrated.interceptor.RequestResponseLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestResponseLoggingInterceptor loggingInterceptor;

    public WebMvcConfig(RequestResponseLoggingInterceptor loggingInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/actuator/**",
                    "/api/swagger-ui/**",
                    "/api/v3/api-docs/**",
                    "/api/webjars/**",
                    "/api/favicon.ico"
                );
    }
}
