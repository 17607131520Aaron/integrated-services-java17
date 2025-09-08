package com.enterprise.integrated.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 * 
 * @author Enterprise Team
 * @version 1.0.0
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        @Override
        public void insertFill(MetaObject metaObject) {
            LocalDateTime now = LocalDateTime.now();
            
            // 创建时间
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
            // 更新时间
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            
            // TODO: 从当前登录用户获取用户ID
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                // 创建人
                this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
                // 更新人
                this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
            }
        }

        @Override
        public void updateFill(MetaObject metaObject) {
            // 更新时间
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            
            // TODO: 从当前登录用户获取用户ID
            Long currentUserId = getCurrentUserId();
            if (currentUserId != null) {
                // 更新人
                this.strictUpdateFill(metaObject, "updateBy", Long.class, currentUserId);
            }
        }

        /**
         * 获取当前登录用户ID
         * TODO: 集成Spring Security后实现
         */
        private Long getCurrentUserId() {
            // 暂时返回默认值，后续集成Spring Security后从SecurityContext获取
            return 1L;
        }
    }
}
