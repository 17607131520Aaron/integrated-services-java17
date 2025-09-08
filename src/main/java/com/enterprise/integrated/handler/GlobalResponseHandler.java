package com.enterprise.integrated.handler;

import com.enterprise.integrated.annotation.IgnoreResponseWrapper;
import com.enterprise.integrated.common.result.Result;
import com.enterprise.integrated.common.result.ResultCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 全局响应处理器
 * 统一包装返回结果
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public GlobalResponseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查是否需要忽略包装
        if (returnType.hasMethodAnnotation(IgnoreResponseWrapper.class)) {
            return false;
        }
        
        // 检查类级别的注解
        if (returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseWrapper.class)) {
            return false;
        }
        
        // 已经是Result类型或ResponseEntity包装的Result不需要再包装
        if (returnType.getParameterType() == Result.class) {
            return false;
        }
        try {
            if (org.springframework.http.ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
        
        // Swagger相关接口不包装
        String className = returnType.getDeclaringClass().getName();
        if (className.contains("springdoc") || className.contains("swagger")) {
            return false;
        }
        
        // 文件/资源下载类不包装
        Class<?> type = returnType.getParameterType();
        if (org.springframework.core.io.Resource.class.isAssignableFrom(type)
                || byte[].class.isAssignableFrom(type)
                || org.springframework.http.ResponseEntity.class.isAssignableFrom(type)
                || org.springframework.core.io.InputStreamSource.class.isAssignableFrom(type)) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request, ServerHttpResponse response) {
        // 非2xx状态码不包装为成功
        int status = 200;
        if (response instanceof ServletServerHttpResponse servletResp) {
            HttpServletResponse raw = servletResp.getServletResponse();
            status = raw.getStatus();
        }

        if (status >= 400) {
            // 根据常见状态码映射统一错误结果
            if (status == HttpStatus.UNAUTHORIZED.value()) {
                return Result.error(ResultCode.UNAUTHORIZED);
            }
            if (status == HttpStatus.FORBIDDEN.value()) {
                return Result.error(ResultCode.FORBIDDEN);
            }
            if (status == HttpStatus.NOT_FOUND.value()) {
                return Result.error(ResultCode.NOT_FOUND);
            }
            if (status >= 500) {
                return Result.error(ResultCode.INTERNAL_ERROR);
            }
            // 其他4xx：尽量从默认错误体中提取 message
            String message = null;
            if (body instanceof java.util.Map<?, ?> map) {
                Object msg = map.get("message");
                Object err = map.get("error");
                message = msg != null ? String.valueOf(msg) : (err != null ? String.valueOf(err) : null);
            }
            if (message == null) {
                message = HttpStatus.resolve(status) != null ? HttpStatus.resolve(status).getReasonPhrase() : "请求错误";
            }
            return Result.error(status, message, body);
        }

        // 如果返回值为null，包装成功响应
        if (body == null) {
            return Result.success();
        }
        
        // 如果是String类型，需要特殊处理，因为StringHttpMessageConverter会直接返回字符串
        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(Result.success(body));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON序列化失败", e);
            }
        }
        
        // 包装成统一响应格式
        return Result.success(body);
    }
}
