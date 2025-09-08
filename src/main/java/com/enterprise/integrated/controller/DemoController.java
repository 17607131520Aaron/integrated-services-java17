package com.enterprise.integrated.controller;

import com.enterprise.integrated.annotation.*;
import com.enterprise.integrated.annotation.validation.Phone;
import com.enterprise.integrated.annotation.validation.IdCard;
import com.enterprise.integrated.common.exception.BusinessException;
import com.enterprise.integrated.common.exception.BusinessExceptionCode;
import com.enterprise.integrated.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

/**
 * 功能演示控制器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/demo")
@Tag(name = "功能演示", description = "展示各种企业级功能的使用")
@ApiVersion({"1"})
public class DemoController {

    @GetMapping("/rate-limit")
    @Operation(summary = "限流测试", description = "测试接口限流功能")
    @RateLimit(key = "demo:rate-limit", count = 5, time = 60)
    @OperationLog(value = "限流测试", recordParams = true, recordResult = true)
    public String rateLimitTest() {
        return "限流测试成功，每分钟最多5次请求";
    }

    @PostMapping("/validation")
    @Operation(summary = "参数验证测试", description = "测试自定义参数验证")
    @OperationLog(value = "参数验证测试", recordParams = true)
    public String validationTest(@Valid @RequestBody ValidationRequest request) {
        return "验证通过：" + request.getName();
    }

    @GetMapping("/business-exception")
    @Operation(summary = "业务异常测试", description = "测试业务异常处理")
    @OperationLog(value = "业务异常测试")
    public String businessExceptionTest() {
        throw new BusinessException(BusinessExceptionCode.USER_NOT_FOUND, "测试用户不存在异常");
    }

    @GetMapping("/desensitize")
    @Operation(summary = "数据脱敏测试", description = "测试数据脱敏功能")
    @OperationLog(value = "数据脱敏测试", recordResult = true)
    public UserDTO desensitizeTest() {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("13800138000");
        return user;
    }

    @GetMapping("/async")
    @Operation(summary = "异步处理测试", description = "测试异步任务处理")
    @OperationLog(value = "异步处理测试")
    public String asyncTest() {
        // 这里可以调用异步服务
        return "异步任务已提交";
    }

    /**
     * 验证请求DTO
     */
    public static class ValidationRequest {
        @NotBlank(message = "姓名不能为空")
        private String name;

        @Phone(message = "手机号格式不正确")
        private String phone;

        @IdCard(allowEmpty = true, message = "身份证号格式不正确")
        private String idCard;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }
}
