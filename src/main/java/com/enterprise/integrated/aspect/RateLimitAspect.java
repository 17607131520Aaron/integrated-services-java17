package com.enterprise.integrated.aspect;

import com.enterprise.integrated.annotation.RateLimit;
import com.enterprise.integrated.common.exception.BusinessException;
import com.enterprise.integrated.common.result.ResultCode;
import com.enterprise.integrated.security.UserDetailsServiceImpl;
import com.enterprise.integrated.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 限流AOP切面
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_LUA_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local count = tonumber(ARGV[1])\n" +
        "local time = tonumber(ARGV[2])\n" +
        "local current = redis.call('get', key)\n" +
        "if current == false then\n" +
        "    redis.call('set', key, 1)\n" +
        "    redis.call('expire', key, time)\n" +
        "    return 1\n" +
        "else\n" +
        "    if tonumber(current) < count then\n" +
        "        return redis.call('incr', key)\n" +
        "    else\n" +
        "        return tonumber(current) + 1\n" +
        "    end\n" +
        "end";

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.enterprise.integrated.annotation.RateLimit)")
    public void rateLimitPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 构建限流key
        String key = buildRateLimitKey(rateLimit, method);
        
        // 执行限流检查
        if (!checkRateLimit(key, rateLimit)) {
            log.warn("接口限流触发: {}", key);
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS, rateLimit.message());
        }

        return joinPoint.proceed();
    }

    /**
     * 构建限流key
     */
    private String buildRateLimitKey(RateLimit rateLimit, Method method) {
        StringBuilder keyBuilder = new StringBuilder("rate_limit:");
        
        // 添加自定义key前缀
        if (!rateLimit.key().isEmpty()) {
            keyBuilder.append(rateLimit.key()).append(":");
        } else {
            // 默认使用类名和方法名
            keyBuilder.append(method.getDeclaringClass().getSimpleName())
                     .append(":")
                     .append(method.getName())
                     .append(":");
        }

        // 根据限流类型添加标识
        switch (rateLimit.limitType()) {
            case IP:
                keyBuilder.append(getClientIpAddress());
                break;
            case USER:
                keyBuilder.append(getCurrentUserId());
                break;
            case DEFAULT:
            default:
                keyBuilder.append("global");
                break;
        }

        return keyBuilder.toString();
    }

    /**
     * 检查限流
     */
    private boolean checkRateLimit(String key, RateLimit rateLimit) {
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(RATE_LIMIT_LUA_SCRIPT);
            redisScript.setResultType(Long.class);

            long timeWindow = rateLimit.timeUnit().toSeconds(rateLimit.time());
            Long result = redisTemplate.execute(redisScript, 
                Collections.singletonList(key), 
                rateLimit.count(), 
                timeWindow);

            return result != null && result <= rateLimit.count();
        } catch (Exception e) {
            log.error("限流检查异常", e);
            // 异常情况下允许通过
            return true;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            return IpUtils.getClientIpAddress(request);
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsServiceImpl.CustomUserDetails) {
                UserDetailsServiceImpl.CustomUserDetails userDetails = 
                    (UserDetailsServiceImpl.CustomUserDetails) authentication.getPrincipal();
                return userDetails.getUserId().toString();
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败", e);
        }
        return "anonymous";
    }
}
