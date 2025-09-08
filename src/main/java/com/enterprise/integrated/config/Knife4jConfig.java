package com.enterprise.integrated.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API文档配置类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Configuration
public class Knife4jConfig {

    @Bean(name = "knife4jOpenAPI")
    @Primary
    public OpenAPI knife4jOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("企业级集成服务API")
                        .version("1.0.0")
                        .description("基于Spring Boot 3.x + Java17的企业级单体服务API文档")
                        .contact(new Contact()
                                .name("Enterprise Team")
                                .email("developer@enterprise.com")
                                .url("https://enterprise.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
