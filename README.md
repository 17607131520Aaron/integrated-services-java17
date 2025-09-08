# 企业级集成服务 (Java17)

基于Spring Boot 3.x + Java17构建的企业级单体服务架构，采用阿里技术栈，提供完整的用户管理、认证授权、监控运维等功能。

## 🚀 技术栈

### 核心框架
- **Java 17** - 最新LTS版本
- **Spring Boot 3.1.5** - 企业级应用框架
- **Spring Security** - 安全认证框架
- **Spring Cloud Alibaba** - 微服务治理

### 数据存储
- **MySQL 8.0+** - 关系型数据库
- **MyBatis-Plus 3.5+** - 数据访问层框架
- **Druid 1.2+** - 数据库连接池
- **Redis** - 缓存和会话存储
- **Redisson** - Redis客户端

### 监控运维
- **Spring Boot Actuator** - 应用监控
- **Micrometer + Prometheus** - 指标监控
- **Knife4j** - API文档
- **Logback** - 日志框架

### 工具库
- **Hutool** - Java工具类库
- **FastJSON2** - JSON处理
- **JWT** - 令牌认证

## 📁 项目结构

```
integrated-services-java17/
├── src/main/java/com/enterprise/integrated/
│   ├── common/                 # 通用组件
│   │   ├── base/              # 基础类
│   │   ├── exception/         # 异常处理
│   │   └── result/            # 统一返回结果
│   ├── config/                # 配置类
│   ├── controller/            # 控制器层
│   ├── dto/                   # 数据传输对象
│   ├── entity/                # 实体类
│   ├── mapper/                # 数据访问层
│   ├── service/               # 业务逻辑层
│   ├── utils/                 # 工具类
│   └── IntegratedServicesApplication.java
├── src/main/resources/
│   ├── sql/                   # 数据库脚本
│   ├── application.yml        # 主配置文件
│   ├── application-dev.yml    # 开发环境配置
│   ├── application-prod.yml   # 生产环境配置
│   └── logback-spring.xml     # 日志配置
└── src/test/                  # 测试代码
```

## 🛠️ 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 1. 克隆项目
```bash
git clone <repository-url>
cd integrated-services-java17
```

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE integrated_services CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
source src/main/resources/sql/schema.sql
```

### 3. 配置修改
修改 `application-dev.yml` 中的数据库和Redis连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/integrated_services_dev
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 启动应用
```bash
mvn spring-boot:run
```

### 5. 访问应用
- **应用地址**: http://localhost:8080/api
- **API文档**: http://localhost:8080/api/doc.html
- **监控面板**: http://localhost:8080/api/actuator
- **数据库监控**: http://localhost:8080/api/druid

## 🔐 默认账户

- **用户名**: admin
- **密码**: admin123

## 📋 主要功能

### 用户管理
- ✅ 用户注册/登录
- ✅ JWT令牌认证
- ✅ 用户信息管理
- ✅ 角色权限控制

### 系统功能
- ✅ 统一异常处理
- ✅ 统一返回格式
- ✅ 参数校验
- ✅ 操作日志记录
- ✅ 接口文档生成

### 监控运维
- ✅ 应用健康检查
- ✅ 性能指标监控
- ✅ 数据库连接池监控
- ✅ 日志管理

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/integrated_services
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
```

### Redis配置
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
```

### JWT配置
```yaml
jwt:
  secret: ${JWT_SECRET:mySecretKey}
  expiration: 86400000 # 24小时
  refresh-expiration: 604800000 # 7天
```

## 🚀 部署指南

### Docker部署
```bash
# 构建镜像
docker build -t integrated-services:1.0.0 .

# 运行容器
docker run -d \
  --name integrated-services \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-username \
  -e DB_PASSWORD=your-password \
  integrated-services:1.0.0
```

### 生产环境部署
1. 修改 `application-prod.yml` 配置
2. 打包应用：`mvn clean package -Pprod`
3. 运行：`java -jar target/integrated-services-1.0.0.jar --spring.profiles.active=prod`

## 📊 性能优化

### JVM参数建议
```bash
java -Xms2g -Xmx4g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar integrated-services-1.0.0.jar
```

### 数据库优化
- 合理设置连接池参数
- 启用SQL慢查询日志
- 定期分析执行计划
- 建立合适的索引

## 🔍 监控告警

### Prometheus指标
- JVM内存使用情况
- 数据库连接池状态
- HTTP请求响应时间
- 业务指标统计

### 日志监控
- 错误日志告警
- 性能日志分析
- 操作审计日志

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 Apache 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- **项目维护者**: Enterprise Team
- **邮箱**: developer@enterprise.com
- **文档**: [项目文档](https://docs.enterprise.com)

---

**注意**: 这是一个企业级项目模板，请根据实际需求进行定制和扩展。