package com.enterprise.integrated.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MenuCacheService {

    private final CacheManager cacheManager;

    public MenuCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictAllMenuCaches() {
        evictAll("menuTree");
        evictAll("visibleMenuTree");
    }

    public void evictMenuTree() {
        evictAll("menuTree");
    }

    public void evictVisibleMenuTreeAllUsers() {
        evictAll("visibleMenuTree");
    }

    public void evictVisibleMenuTreeForUser(Long userId) {
        Cache cache = cacheManager.getCache("visibleMenuTree");
        if (cache == null || userId == null) {
            return;
        }
        // 组合参数：menuOnly/pruneEmpty 两个布尔
        boolean[] bools = new boolean[] {false, true};
        for (boolean menuOnly : bools) {
            for (boolean pruneEmpty : bools) {
                String key = userId + ":" + menuOnly + ":" + pruneEmpty;
                cache.evictIfPresent(key);
            }
        }
    }

    public void evictVisibleMenuTreeForUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return;
        for (Long userId : userIds) {
            evictVisibleMenuTreeForUser(userId);
        }
    }

    public void evictAll(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}


