# 社团活动报名管理系统

## 项目简介

社团活动报名管理系统是一个基于 Spring Boot 的后端 REST API 服务，用于管理大学社团活动的发布、展示、报名与取消报名等业务流程。系统支持普通用户浏览活动并报名，以及管理员创建和管理活动、分类等操作。

## 技术栈

| 组件       | 技术选型                   
|----------|------------------------|
| 开发框架     | Spring Boot 3.5.5      |
| 开发语言     | Java 25                |
| 持久层框架    | MyBatis-Plus 3.5.9     |
| 数据库      | MySQL 8.0+             |
| 安全框架     | Spring Security        |
| 认证方案     | JWT                    |
| 参数校验     | Spring Bean Validation |
| JSON 序列化 | Jackson                |
| 构建工具     | Maven                  |
| 工具库      | Lombok 1.18.46         |
| 密码加密     | BCrypt                 |

## 项目启动方式

### 方式一：Docker Compose 一键部署

#### 1. 准备工作

确保已安装：
- Docker Desktop（或 Docker + Docker Compose）
- Java 25+ / Maven 3.x

#### 2. 配置环境变量

项目根目录下的 `.env` 文件已包含默认配置，如需请修改：

```bash
# .env 文件
MYSQL_ROOT_PASSWORD=root123456    # MySQL root 密码
DB_PASSWORD=root123456            # 数据库连接密码
JWT_SECRET=YourJwtSecretKeyHere... # JWT 签名密钥
```

> JWT 密钥需要是 Base64 编码的字符串。可以使用以下命令生成：
>
> ```bash
> # PowerShell
> $bytes = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes); [Convert]::ToBase64String($bytes)
> ```

#### 3. 打包 & 启动

```bash
# 1. 先打包后端
mvn clean package -DskipTests

# 2. 启动所有服务（MySQL + 后端）
docker compose up -d
```
首次启动时 MySQL 会自动执行 `schema.sql` 初始化数据库。

服务默认运行在 `http://localhost:8080`。

---

### 方式二：手动部署

#### 1. 环境准备

- JDK 25+
- Maven 3.x
- MySQL 8.0+

#### 2. 配置环境变量

```bash
# Windows PowerShell
$env:DB_PASSWORD = "你的数据库密码"
$env:JWT_SECRET  = "你的JWT密钥（Base64 编码）"
```

#### 3. 初始化数据库

在 MySQL 中执行项目根目录下的 `schema.sql` 脚本：

```bash
mysql -u root -p < schema.sql
```

该脚本会创建数据库、所有表结构，并插入管理员账号、示例分类和示例活动数据。

#### 4. 编译运行

```bash
# 编译
mvn clean package -DskipTests

# 运行
mvn spring-boot:run
```

服务默认运行在 `http://localhost:8080`。

#### 5. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | 自行注册 | — |

## 数据库初始化方式

项目使用 `schema.sql` 进行数据库初始化，包含：

- 数据库及 5 张表的 DDL（`user`、`category`、`activity`、`registration`、`operation_log`）
- 管理员种子数据
- 5 个示例分类
- 12 条示例活动

直接在 MySQL 中执行 `schema.sql` 即可完成初始化。

## 配置文件说明

配置文件位于 `src/main/resources/application.yml`，主要配置项：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `server.port` | 服务端口 | `8080` |
| `spring.datasource.url` | 数据库连接 | `jdbc:mysql://localhost:3306/club_registration` |
| `spring.datasource.username` | 数据库用户 | `root` |
| `spring.datasource.password` | 数据库密码 | `${DB_PASSWORD}`（环境变量） |
| `jwt.secret` | JWT 签名密钥（Base64） | `${JWT_SECRET}`（环境变量） |
| `jwt.expiration` | JWT 过期时间（毫秒） | `86400000`（24 小时） |
| `mybatis-plus.configuration.log-impl` | SQL 日志输出 | `StdOutImpl`|
| `mybatis-plus.global-config.db-config.logic-delete-field` | 逻辑删除字段名 | `isDeleted` |

## Apifox 文档公开链接

> 8p01hzwczq.apifox.cn

## 数据库设计说明

### 表结构

| 表名 | 说明 | 核心字段                                                                                                                                | 软删除 |
|------|------|-------------------------------------------------------------------------------------------------------------------------------------|-----|
| `user` | 用户表 | id, username(UK), password(BCrypt), email(UK), role                                                                                 | 支持  |
| `category` | 活动分类表 | id, name(UK), description, sort_order                                                                                               | 支持  |
| `activity` | 活动表 | id, title(UK), start_time, end_time, registration_deadline, max_participants, current_participants, category_id, status, created_by | 支持  |
| `registration` | 报名记录表 | id, user_id, activity_id(联合UK), status, registered_at, cancelled_at                                                                 | 支持  |
| `operation_log` | 操作日志表 | id, user_id, username, action, target_type, target_id, detail                                                                       | 无需  |

### 关键索引

- `user` 表：`uk_username`、`uk_email` 唯一索引
- `activity` 表：`uk_title` 唯一索引，`idx_category_id`、`idx_status`、`idx_start_time` 等普通索引
- `registration` 表：`uk_user_activity` 唯一索引，防止重复报名

### 软删除

`user`、`category`、`activity`、`registration` 四张表均采用软删除机制（`is_deleted = 0/1`），通过 MyBatis-Plus `@TableLogic` 注解自动过滤已删除记录。`operation_log` 表不设软删除。

## 主要接口设计说明

### 公开接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录，返回 JWT Token |
| GET | `/api/activities` | 活动列表（支持关键词、分类、状态筛选，分页） |
| GET | `/api/activities/{id}` | 活动详情 |
| GET | `/api/categories` | 分类列表 |

### 需认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/activities/{id}/register` | 报名活动 |
| DELETE | `/api/activities/{id}/register` | 取消报名 |
| GET | `/api/user/registrations` | 我的报名记录（分页） |

### 管理员接口（需 ROLE_ADMIN 角色）

| 方法 | 路径 | 说明               |
|------|------|------------------|
| POST | `/api/admin/activities` | 创建活动             |
| PUT | `/api/admin/activities/{id}` | 更新活动             |
| DELETE | `/api/admin/activities/{id}` | 删除活动             |
| GET | `/api/admin/activities/{id}/registrations` | 根据活动 id 查看活动报名人员 |
| POST | `/api/admin/categories` | 创建分类             |
| PUT | `/api/admin/categories/{id}` | 更新分类             |
| DELETE | `/api/admin/categories/{id}` | 删除分类             |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "records": []
  }
}
```

## Spring Security + JWT 鉴权流程说明

### 整体流程

```
客户端                         服务端
  │                              │
  │  POST /api/auth/login        │
  │ ──────────────────────────>  │  验证用户名密码
  │                              │
  │  { token, userId, ... }      │  生成 JWT Token
  │ <──────────────────────────  │
  │                              │
  │  GET /api/user/registrations │
  │  Authorization: Bearer token │
  │ ──────────────────────────>  │  JwtAuthenticationFilter 拦截
  │                              │  ├─ 从 Header 提取 Token
  │                              │  ├─ 解析/验证 JWT
  │                              │  ├─ 构建 Authentication
  │                              │  └─ 写入 SecurityContext
  │                              │
  │  200 { data: [...] }         │  业务处理
  │ <──────────────────────────  │
```

### 核心组件

| 组件 | 职责 |
|------|------|
| `JwtUtils` | Token 生成、解析、验证。|
| `JwtAuthenticationFilter` | `OncePerRequestFilter`，从请求头提取 Bearer Token，解析后设置安全上下文 |
| `JwtAuthenticationEntryPoint` | 认证失败时返回 JSON 格式 401 |
| `JwtAccessDeniedHandler` | 权限不足时返回 JSON 格式 403 |
| `SecurityUtils` | 静态工具类，从 SecurityContext 获取当前用户 ID 和用户名 |
| `JwtUserDetails` | 简单主体对象，持有 userId 和 username |

### 权限配置

```java
.requestMatchers("/api/auth/**").permitAll()        // 注册/登录公开
.requestMatchers("/api/activities", "/api/activities/*").permitAll()  // 活动浏览公开
.requestMatchers("/api/categories/**").permitAll()   // 分类浏览公开
.requestMatchers("/api/admin/**").hasRole("ADMIN")   // 管理接口仅管理员
.anyRequest().authenticated()                        // 其余需认证
```

- 会话策略：`SessionCreationPolicy.STATELESS`（无状态）
- CSRF：已禁用

## 持久层实现说明

使用 **MyBatis-Plus 3.5.9** 作为持久层框架。

### 关键特性

| 特性 | 说明 |
|------|------|
| BaseMapper | 所有 Mapper 继承 `BaseMapper<T>`，自动获得 CRUD 方法 |
| LambdaQueryWrapper | 使用 Lambda 表达式构建查询条件 |
| 分页插件 | `PaginationInnerInterceptor` 自动处理分页 |
| 逻辑删除 | `@TableLogic` 注解，自动拼接 `is_deleted = 0` 条件 |
| 自动填充 | `MyMetaObjectHandler` 自动填充 `createdAt` / `updatedAt` |
| 自定义 SQL | `ActivityMapper.updateCurrentParticipants()` 使用原子 UPDATE 防止并发超量报名 |

### 无 XML Mapper

所有数据操作均通过 MyBatis-Plus 内置方法和注解 SQL 实现，无 XML Mapper 文件。

## 权限控制说明

| 角色 | 常量 | 权限 |
|------|------|------|
| 普通用户 | `ROLE_USER` | 浏览活动/分类，报名/取消报名，查看自己的报名记录 |
| 管理员 | `ROLE_ADMIN` | 普通用户权限 + 活动 CRUD、分类 CRUD、查看活动报名人员 |

- **用户注册**时默认赋予 `ROLE_USER` 角色
- **管理员**通过数据库直接设置（种子数据中 admin 用户为 `ROLE_ADMIN`）
- 权限校验在 Spring Security 配置层和业务代码层（通过 `SecurityUtils` 获取当前用户）双重保障

## 核心业务规则说明

### 报名

1. 活动必须存在且未被删除
2. 当前时间不能超过报名截止时间
3. 当前时间不能超过活动开始时间
4. 如果用户已报名，不允许重复报名
5. 如果用户之前取消过报名，重新报名会复用原有记录
6. 报名人数不能超过 `max_participants`

### 取消报名

1. 活动必须存在且未被删除
2. 活动开始后不允许取消
3. 只能取消状态为 REGISTERED 的报名记录
4. 取消后名额自动释放

## 特殊设计说明

### 管理员是否可以报名活动

**可以。** 管理员也可以作为普通用户报名活动。

### 活动开始后是否可以取消报名

**不可以。** 取消报名时会检查 `LocalDateTime.now().isAfter(activity.getStartTime())`，活动开始后取消将抛出 `CANNOT_CANCEL_STARTED` 异常。

### 删除已有报名的活动时如何处理

**不允许删除。** 删除活动前会检查是否存在状态为 REGISTERED 的报名记录，如果存在则抛出 `ACTIVITY_HAS_REGISTRATIONS` 异常，防止误删有参与者的活动。

### 分类下存在活动时是否允许删除分类

**不允许删除。** 删除分类前会检查该分类下是否存在活动，如果存在则抛出 `CATEGORY_HAS_ACTIVITIES` 异常。


## Git 提交说明

```
fix some bugs and add exception handler
fix some issue and add docker-compose dockerfile
fix some issue about business boundary logic
fix some issue about exception
add admin category management api
add admin activities management api
add register activity and cancel activity api
add query category api
add query activity api
add security module
add register and login api
add entity, mapper and schema.sql
Initial commit 
```

## 使用 AI 辅助的情况说明

本项目在开发过程中使用了 Claude Code 进行以下 AI 辅助工作：

1. **Bug 修复**：在 AI 的帮助下，修复了如取消报名后无法重复报名等bug
2. **检查代码**：AI 检查我的代码，给我提出了一些修改意见
3. **数据库初始化数据**：AI 帮我生成了数据库的一些初始化分类和活动数据

## 遇到的问题与解决方案

### 1. 通过原子更新防止超量报名

`ActivityMapper.updateCurrentParticipants()` 使用数据库级别的原子 UPDATE：

```sql
UPDATE activity
SET current_participants = current_participants + #{delta}
WHERE id = #{activityId}
  AND current_participants + #{delta} >= 0
  AND (current_participants + #{delta}) <= max_participants
```

通过 `updated == 0` 判断更新失败，从而在高并发场景下防止超量报名。

### 2. 重复报名解决方式

`registration` 表通过 `UNIQUE KEY uk_user_activity (user_id, activity_id)` 保证同一用户对同一活动最多一条记录。用户先报名再取消再报名时，会更新原记录的状态而非插入新记录，避免了唯一键冲突。
