package com.enterprise.integrated.config;

import com.enterprise.integrated.annotation.ApiVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * API版本控制处理器映射
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    private static final String VERSION_PREFIX = "/v";

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = super.getMappingForMethod(method, handlerType);
        if (info == null) {
            return null;
        }

        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
        if (methodAnnotation != null) {
            RequestCondition<?> methodCondition = getCustomMethodCondition(method);
            info = createApiVersionInfo(methodAnnotation, methodCondition).combine(info);
        } else {
            ApiVersion typeAnnotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
            if (typeAnnotation != null) {
                RequestCondition<?> typeCondition = getCustomTypeCondition(handlerType);
                info = createApiVersionInfo(typeAnnotation, typeCondition).combine(info);
            }
        }

        return info;
    }

    @Override
    public int getOrder() {
        // 提高优先级，确保版本前缀映射优先生效
        return -10;
    }

    private RequestMappingInfo createApiVersionInfo(ApiVersion annotation, RequestCondition<?> customCondition) {
        String[] patterns = new String[annotation.value().length];
        for (int i = 0; i < annotation.value().length; i++) {
            patterns[i] = VERSION_PREFIX + annotation.value()[i];
        }

        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(patterns).customCondition(customCondition);
        // 保持与当前 HandlerMapping 相同的 PathPattern 解析设置
        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(getPatternParser());
        builder.options(options);
        return builder.build();
    }
}
