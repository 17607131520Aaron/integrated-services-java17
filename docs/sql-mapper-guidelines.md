## SQL/Mapper 编写规约（企业级）

### 目录与命名
- XML 统一放在 `src/main/resources/mapper/`，命名与接口同名：`XxxMapper.xml`。
- 接口包：`com.enterprise.integrated.mapper`；`namespace` 必须与接口全限定名一致。

### 查询规范
- 禁止 `SELECT *`，必须列出显式列清单，建议抽取 `<sql id="Base_Column_List">` 复用。
- 结果映射使用 `resultMap`，显式列到字段的映射，避免隐式风险。
- 单条语义查询应：
  - DB 层具备唯一键约束；
  - SQL 末尾加 `LIMIT 1`；
  - 方法名用 `findBy...`，返回单对象。
- 列表/集合查询方法名用 `list...` 或 `page...`，分页使用 MyBatis-Plus 分页插件。

### 条件与动态 SQL
- 统一软删/状态条件：`deleted = 0`、`status = 1`；可通过 XML 片段或 Wrapper 复用。
- 模糊匹配使用 `LIKE CONCAT('%', #{kw}, '%')`；避免拼接 `%${}`。
- 时间范围筛选：`create_time >= #{startTime} AND create_time <= #{endTime}`。

### 关联与去重
- JOIN 时确保关联键有索引；必要时使用 `EXISTS` 避免大表 DISTINCT。
- 确需去重才使用 `SELECT DISTINCT`，优先从模型上避免重复。

### 计数与存在性
- 仅判断存在性使用：`SELECT EXISTS(SELECT 1 ... LIMIT 1)`，方法命名 `existsXxx`，返回 `boolean`。
- 统计行数使用 `COUNT(*)`，方法命名 `countXxx`，返回 `int/long`。

### 批量操作
- 使用 `<foreach>` 批量插入/删除，必要时配合联合唯一键 + `ON DUPLICATE KEY UPDATE` 保证幂等。
- 批量参数命名：集合 `xxxIds`，元素 `id`/`item`；避免过大批次（建议 <= 1000）。

### 性能与索引建议
- 高频过滤条件、JOIN 键建立合适索引（覆盖索引优先）。
- 只取必需列，避免大字段频繁查询。
- 大表分页尽量走条件列（索引列）+ 排序列（覆盖索引）。

### 日志与审计
- 复杂/关键 SQL 放 XML，利于变更审计与代码评审。
- 生产关闭控制台 SQL 日志，必要时按模块/采样输出。

### 示例片段
```xml
<sql id="Base_Column_List">
    id, username, status, create_time, update_time
    <!-- 仅举例，按表结构维护 -->
    
</sql>

<resultMap id="UserResultMap" type="com.enterprise.integrated.entity.User">
    <id property="id" column="id" />
    <result property="username" column="username" />
    <result property="status" column="status" />
    <result property="createTime" column="create_time" />
    <result property="updateTime" column="update_time" />
</resultMap>

<select id="findByUsername" resultMap="UserResultMap">
    SELECT <include refid="Base_Column_List" />
    FROM sys_user
    WHERE username = #{username}
      AND deleted = 0
    LIMIT 1
</select>

<select id="pageUsers" resultMap="UserResultMap">
    SELECT <include refid="Base_Column_List" />
    FROM sys_user
    <where>
        <if test="status != null">AND status = #{status}</if>
        <if test="kw != null and kw != ''">
            AND (username LIKE CONCAT('%', #{kw}, '%'))
        </if>
        AND deleted = 0
    </where>
    ORDER BY update_time DESC
</select>
```

### 代码风格
- Mapper 接口以领域语义命名：`findBy...`、`list...`、`page...`、`exists...`、`count...`、`bind/unbind...`。
- 参数使用 `@Param` 明确命名；XML 中统一引用相同命名。

---
本规约随项目演进更新，新增场景请补充对应规范与示例。


