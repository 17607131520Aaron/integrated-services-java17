package com.enterprise.integrated.controller;

import com.enterprise.integrated.annotation.IgnoreResponseWrapper;
import com.enterprise.integrated.common.exception.ServiceException;
import com.enterprise.integrated.common.result.Result;
import com.enterprise.integrated.common.result.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局响应处理测试控制器
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Tag(name = "响应处理测试", description = "测试全局响应处理功能")
@RestController
@RequestMapping("/api/test/response")
public class ResponseTestController {

    @Operation(summary = "测试自动包装 - 返回字符串")
    @GetMapping("/string")
    public String testString() {
        return "这是一个字符串，会被自动包装成Result";
    }

    @Operation(summary = "测试自动包装 - 返回对象")
    @GetMapping("/object")
    public Map<String, Object> testObject() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1);
        data.put("name", "测试用户");
        data.put("email", "test@example.com");
        return data;
    }

    @Operation(summary = "测试自动包装 - 返回null")
    @GetMapping("/null")
    public Object testNull() {
        return null;
    }

    @Operation(summary = "测试手动返回Result")
    @GetMapping("/manual-result")
    public Result<String> testManualResult() {
        return Result.success("手动返回的Result，不会被重复包装");
    }

    @Operation(summary = "测试忽略包装注解")
    @GetMapping("/ignore")
    @IgnoreResponseWrapper
    public Map<String, Object> testIgnoreWrapper() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这个响应不会被包装");
        data.put("timestamp", System.currentTimeMillis());
        return data;
    }

    @Operation(summary = "测试默认异常 - 9000")
    @GetMapping("/error/default")
    public String testDefaultError() {
        throw new ServiceException("这是默认的业务异常，错误码为9000");
    }

    @Operation(summary = "测试自定义异常码")
    @GetMapping("/error/custom")
    public String testCustomError() {
        throw new ServiceException(9001, "这是自定义的业务异常，错误码为9001");
    }

    @Operation(summary = "测试带数据的异常")
    @GetMapping("/error/with-data")
    public String testErrorWithData() {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("field", "username");
        errorData.put("value", "invalid_user");
        throw new ServiceException(9002, "用户名格式不正确", errorData);
    }

    @Operation(summary = "测试使用ResultCode的异常")
    @GetMapping("/error/result-code")
    public String testErrorWithResultCode() {
        throw ServiceException.of(ResultCode.PARAM_ERROR);
    }

    @Operation(summary = "测试运行时异常 - 会被转为9000")
    @GetMapping("/error/runtime")
    public String testRuntimeError() {
        throw new RuntimeException("这是运行时异常，会被全局异常处理器捕获");
    }

    @Operation(summary = "测试成功响应 - 自定义消息")
    @GetMapping("/success/custom-message")
    public String testSuccessWithCustomMessage() {
        // 这里演示如何在Controller中返回自定义成功消息
        // 实际使用中可以通过其他方式实现
        return "操作完成";
    }

    @Operation(summary = "测试参数验证异常")
    @PostMapping("/error/validation")
    public String testValidationError(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ServiceException(ResultCode.PARAM_MISSING.getCode(), "name参数不能为空");
        }
        return "参数验证通过: " + name;
    }
}
