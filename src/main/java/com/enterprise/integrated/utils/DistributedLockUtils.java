package com.enterprise.integrated.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁工具类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Component
public class DistributedLockUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "distributed_lock:";
    private static final String UNLOCK_LUA_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    public DistributedLockUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @return 锁的标识，获取失败返回null
     */
    public String tryLock(String lockKey, long expireTime) {
        String lockValue = UUID.randomUUID().toString();
        String key = LOCK_PREFIX + lockKey;
        
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, lockValue, expireTime, TimeUnit.SECONDS);
        
        return Boolean.TRUE.equals(success) ? lockValue : null;
    }

    /**
     * 尝试获取分布式锁（默认30秒过期）
     * @param lockKey 锁的key
     * @return 锁的标识，获取失败返回null
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, 30);
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁的key
     * @param lockValue 锁的标识
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        String key = LOCK_PREFIX + lockKey;
        
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(UNLOCK_LUA_SCRIPT);
        redisScript.setResultType(Long.class);
        
        Long result = redisTemplate.execute(redisScript, 
            Collections.singletonList(key), lockValue);
        
        return Long.valueOf(1).equals(result);
    }

    /**
     * 带重试的获取锁
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @param retryTimes 重试次数
     * @param retryInterval 重试间隔（毫秒）
     * @return 锁的标识，获取失败返回null
     */
    public String tryLockWithRetry(String lockKey, long expireTime, int retryTimes, long retryInterval) {
        for (int i = 0; i <= retryTimes; i++) {
            String lockValue = tryLock(lockKey, expireTime);
            if (lockValue != null) {
                return lockValue;
            }
            
            if (i < retryTimes) {
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 执行带锁的操作
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @param task 要执行的任务
     * @return 是否执行成功
     */
    public boolean executeWithLock(String lockKey, long expireTime, Runnable task) {
        String lockValue = tryLock(lockKey, expireTime);
        if (lockValue == null) {
            return false;
        }
        
        try {
            task.run();
            return true;
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 执行带锁的操作（带返回值）
     * @param lockKey 锁的key
     * @param expireTime 锁的过期时间（秒）
     * @param task 要执行的任务
     * @return 任务执行结果，获取锁失败返回null
     */
    public <T> T executeWithLock(String lockKey, long expireTime, java.util.function.Supplier<T> task) {
        String lockValue = tryLock(lockKey, expireTime);
        if (lockValue == null) {
            return null;
        }
        
        try {
            return task.get();
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * 检查锁是否存在
     * @param lockKey 锁的key
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取锁的剩余过期时间
     * @param lockKey 锁的key
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示key不存在
     */
    public long getLockExpireTime(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
