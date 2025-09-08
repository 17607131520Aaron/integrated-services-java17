package com.enterprise.integrated;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用程序测试类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
class IntegratedServicesApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否正常加载
    }
}
