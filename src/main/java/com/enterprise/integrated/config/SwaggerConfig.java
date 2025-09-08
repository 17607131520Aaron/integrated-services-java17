package com.enterprise.integrated.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Swagger/OpenAPI 配置
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(Arrays.asList(
                    new Server().url("http://localhost:8080").description("本地开发环境"),
                    new Server().url("https://api.example.com").description("生产环境")
                ))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("JWT", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT认证令牌")
                    )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("企业级Java17单体服务API")
                .description("基于Spring Boot 3.x的企业级单体服务，包含JWT认证、操作日志、限流、缓存、分布式锁、数据脱敏、异步任务、API版本控制、国际化等功能")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Enterprise Team")
                    .email("team@enterprise.com")
                    .url("https://www.enterprise.com")
                )
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")
                );
    }
}
