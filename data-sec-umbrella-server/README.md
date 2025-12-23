# 数据安全伞后端项目

这是数据安全伞系统的后端服务，基于Spring Boot框架构建。

## 技术栈

- Java 21
- Spring Boot 2.7.0
- MyBatis Plus
- MySQL
- Maven

## 项目结构

- `src/main/java/com/arelore/data/sec/umbrella/server/` - 主要源代码
  - `controller/` - 控制器层，处理HTTP请求
  - `service/` - 服务层，实现业务逻辑
  - `entity/` - 实体类，映射数据库表
  - `dto/` - 数据传输对象
  - `mapper/` - MyBatis映射接口
- `src/main/resources/` - 资源文件
  - `application.yml` - 应用配置文件
  - `mapper/` - MyBatis XML映射文件

## 功能模块

- 数据库策略管理
- API策略管理
- 消息策略管理
- 日志策略管理

## 启动项目

使用以下命令启动项目：

```bash
./start.sh
```

或者手动启动：

```bash
mvn clean compile
mvn spring-boot:run
```

## API接口

服务启动后，API接口将在以下地址可用：

- 基础URL: http://localhost:8081
- 数据库策略API: http://localhost:8081/api/database-policy
- API策略API: http://localhost:8081/api/api-policy
- 消息策略API: http://localhost:8081/api/message-policy
- 日志策略API: http://localhost:8081/api/log-policy

## 配置说明

### 数据库配置

在 `src/main/resources/application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/data_sec_umbrella?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 端口配置

默认端口为8081，可在 `application.yml` 中修改：

```yaml
server:
  port: 8081
```

## 开发指南

### 添加新的策略类型

1. 创建对应的实体类（Entity）
2. 创建对应的DTO类（Request和Response）
3. 创建对应的Mapper接口
4. 创建对应的Service接口和实现类
5. 创建对应的Controller类

### 数据库表创建

参考 `src/main/resources/database_policy.sql` 文件中的SQL语句创建数据库表。

## 注意事项

- 本项目禁用了RESTful风格的HTTP方法，所有接口均使用POST请求
- 所有请求参数通过请求体（RequestBody）传递，不使用URL参数
- 项目使用MyBatis Plus作为ORM框架，简化数据库操作