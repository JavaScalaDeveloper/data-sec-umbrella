# 数据安全保护伞 (Data Security Umbrella)

数据安全保护伞，专注数据库、API、MQ、日志风险检测及安全防护，提供实时和批量检测能力，支持数据分类分级管理。

## 项目概述

数据安全保护伞是一个全面的数据安全解决方案，旨在帮助企业保护其关键数据资产。系统通过实时监控和批量分析，对数据库、API接口、消息队列和日志系统进行全面的安全检测，并根据数据敏感度进行分类分级，确保数据安全合规。

## 核心功能

### 1. 数据资产安全管理
- **数据库安全管理**
  - 实例监控与管理
  - 数据库安全检测
  - 表级别安全评估
  
- **API安全管理**
  - 域名安全监控
  - API接口风险评估
  - 访问行为分析
  
- **消息队列安全管理**
  - 集群安全监控
  - Topic安全检测
  - 消息内容审计
  
- **日志安全管理**
  - 日志收集与分析
  - 异常行为检测
  - 安全事件告警

### 2. 安全策略管理
- **数据库安全策略**
  - 访问控制策略
  - 数据加密策略
  - 审计日志策略
  
- **API安全策略**
  - 认证授权策略
  - 访问限流策略
  - 数据脱敏策略
  
- **消息队列安全策略**
  - 传输加密策略
  - 访问权限策略
  
- **日志安全策略**
  - 日志保留策略
  - 敏感信息过滤策略

### 3. 检测任务管理
- **实时检测**
  - 实时数据流监控
  - 即时风险告警
  - 动态策略调整
  
- **批量检测**
  - 定期安全扫描
  - 批量数据分析
  - 周期性报告生成

### 4. 数据分类分级
- **自动化分类**
  - 基于内容智能识别
  - 模式匹配分类
  - 机器学习辅助分类
  
- **灵活分级**
  - 敏感度等级定义
  - 自定义分级标准
  - 动态调整机制

## 技术架构

### 前端技术栈
- **框架**: React 19.2.3
- **UI组件**: Ant Design 6.1.1
- **路由**: React Router 7.11.0
- **图标**: Ant Design Icons 6.1.0
- **构建工具**: Create React App

### 后端技术栈
- **框架**: Spring Boot 2.7.0
- **数据库**: MySQL 8.0.33
- **ORM**: MyBatis Plus 3.4.3.1
- **工具**: Lombok 1.18.30

## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                      数据安全保护伞系统                      │
├─────────────────────────────────────────────────────────────┤
│  前端展示层 (React + Ant Design)                           │
│  ┌─────────┬─────────┬─────────┬─────────┬─────────────────┐ │
│  │ 资产管理 │ 策略管理 │ 实时检测 │ 批量检测 │ 系统设置        │ │
│  └─────────┴─────────┴─────────┴─────────┴─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  业务逻辑层 (Spring Boot)                                  │
│  ┌─────────┬─────────┬─────────┬─────────┬─────────────────┐ │
│  │ 资产管理 │ 策略引擎 │ 检测引擎 │ 分类分级 │ 报告生成        │ │
│  └─────────┴─────────┴─────────┴─────────┴─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  数据访问层 (MyBatis Plus)                                 │
│  ┌─────────┬─────────┬─────────┬─────────┬─────────────────┐ │
│  │ 资产数据 │ 策略数据 │ 检测结果 │ 分类结果 │ 日志数据        │ │
│  └─────────┴─────────┴─────────┴─────────┴─────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  数据存储层 (MySQL)                                         │
│  ┌─────────┬─────────┬─────────┬─────────┬─────────────────┐ │
│  │ 资产库   │ 策略库   │ 结果库   │ 分类库   │ 日志库          │ │
│  └─────────┴─────────┴─────────┴─────────┴─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 接口开发规范

### ⚠️ 重要限制

本系统后端接口严格遵循以下规范，所有开发者必须遵守：

1. **禁止使用 RESTful 风格**
   - 所有接口统一使用 `POST` 方法
   - 不得使用 `GET`、`PUT`、`DELETE` 等 RESTful HTTP 方法

2. **禁止将参数写在 URL 路径中**
   - 所有参数必须放在请求体（Request Body）中
   - 不得使用路径参数（如 `/api/resource/{id}`）
   - 不得使用查询参数（如 `/api/resource?id=123`）

3. **统一返回格式**
   - 所有接口返回统一的 `Result` 对象
   - 包含 `code`（状态码）、`message`（消息）、`data`（数据）字段

### 接口示例

#### 正确的接口设计
```java
// ✅ 正确：使用 POST 方法，参数在请求体中
@PostMapping("/get-by-id")
public Result<DataSource> getById(@RequestBody Map<String, Object> params) {
    Long id = Long.parseLong(params.get("id").toString());
    DataSource dataSource = dataSourceService.getById(id);
    return Result.success(dataSource);
}
```

#### 错误的接口设计
```java
// ❌ 错误：使用 GET 方法
@GetMapping("/{id}")
public Result<DataSource> getById(@PathVariable Long id) {
    // ...
}

// ❌ 错误：使用 PUT 方法
@PutMapping
public Result<Boolean> update(@RequestBody DataSource dataSource) {
    // ...
}

// ❌ 错误：使用 DELETE 方法
@DeleteMapping("/{id}")
public Result<Boolean> delete(@PathVariable Long id) {
    // ...
}

// ❌ 错误：使用查询参数
@GetMapping("/page")
public Result<IPage<DataSource>> getPage(
    @RequestParam Integer current,
    @RequestParam Integer size) {
    // ...
}
```

### 前端调用示例

```typescript
// ✅ 正确：使用 POST 方法，参数在请求体中
const response = await request('/api/data-source/get-by-id', {
    method: 'POST',
    body: JSON.stringify({ id: 123 }),
});

// ❌ 错误：使用 GET 方法，参数在 URL 中
const response = await request('/api/data-source/123', {
    method: 'GET',
});
```

### 规范原因

1. **安全性**：所有接口使用 POST 方法，减少攻击面
2. **一致性**：统一的接口风格，便于维护和理解
3. **扩展性**：便于接口版本控制和参数扩展
4. **监控**：便于日志记录和请求追踪

## 快速开始

### 环境要求
- Node.js 16+
- Java 8+
- MySQL 8.0+
- Maven 3.6+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-org/data-sec-umbrella.git
   cd data-sec-umbrella
   ```

2. **启动后端服务**
   ```bash
   cd data-sec-umbrella-server
   # 配置数据库连接信息 (application.yml)
   mvn clean install
   mvn spring-boot:run
   ```

3. **启动前端服务**
   ```bash
   cd data-sec-umbrella-front
   npm install
   npm start
   ```

4. **访问系统**
   - 前端地址: http://localhost:3000
   - 后端API: http://localhost:8080

## 使用指南

### 1. 数据资产管理
- 添加数据库、API、MQ、日志等数据资产
- 配置资产连接信息和访问凭证
- 查看资产状态和安全概况

### 2. 安全策略配置
- 根据业务需求制定安全策略
- 设置检测规则和阈值
- 配置告警通知方式

### 3. 检测任务执行
- 创建实时检测任务，持续监控数据安全
- 配置批量检测任务，定期进行全面扫描
- 查看检测结果和风险报告

### 4. 数据分类分级
- 配置分类规则和分级标准
- 执行自动分类分级任务
- 管理和维护分类分级结果

## 项目结构

```
data-sec-umbrella/
├── data-sec-umbrella-front/          # 前端项目
│   ├── public/                       # 静态资源
│   ├── src/
│   │   ├── components/               # 公共组件
│   │   ├── layouts/                  # 布局组件
│   │   ├── pages/                    # 页面组件
│   │   │   ├── asset/                # 资产管理页面
│   │   │   │   ├── database/         # 数据库管理
│   │   │   │   ├── api/              # API管理
│   │   │   │   ├── message/          # 消息队列管理
│   │   │   │   └── log/              # 日志管理
│   │   │   └── task/                 # 任务管理页面
│   │   │       ├── policy/           # 策略管理
│   │   │       ├── real-time/        # 实时检测
│   │   │       └── batch/            # 批量检测
│   │   ├── utils/                    # 工具函数
│   │   └── App.tsx                   # 应用入口
│   └── package.json
├── data-sec-umbrella-server/         # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/                 # Java源码
│   │   │   └── resources/            # 配置文件
│   │   └── test/                     # 测试代码
│   ├── pom.xml                       # Maven配置
│   └── application.yml               # 应用配置
└── README.md                         # 项目说明
```

## 贡献指南

我们欢迎社区贡献！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目采用 [Apache 2.0 许可证](LICENSE)。

## 联系我们

- 项目主页: https://github.com/JavaScalaDeveloper/data-sec-umbrella
- 问题反馈: https://github.com/JavaScalaDeveloperdata-sec-umbrella/issues
- 邮箱: 544789628@qq.com

## 交互界面展示

### 策略管理界面

#### 数据库策略列表
![数据库策略列表](/data-sec-umbrella-front/public/images/交互图/策略管理/数据库策略列表.png)
- 展示所有数据库安全策略的列表
- 支持策略的查询、创建、编辑和删除操作
- 显示策略名称、数据库类型、创建时间等关键信息
- 提供策略启用/禁用功能

#### 数据库策略配置
![数据库策略配置](/data-sec-umbrella-front/public/images/交互图/策略管理/数据库策略配置.png)
- 策略基本信息配置（名称、描述、适用数据库类型）
- 安全配置（加密算法、访问控制、审计日志）
- 检测规则配置（敏感数据识别、SQL注入防护、异常访问检测）
- 响应策略配置（告警级别、通知方式、阻断策略）

## 快速启动脚本

### 前端一键启动

项目提供了便捷的前端启动脚本 `start.sh`，位于 `/data-sec-umbrella-front` 目录下。

#### 使用方法：

1. 进入前端项目目录：
   ```bash
   cd /Users/huang/Documents/Workspaces/data-sec-umbrella/data-sec-umbrella-front
   ```

2. 给脚本添加执行权限（首次使用）：
   ```bash
   chmod +x start.sh
   ```

3. 执行启动脚本：
   ```bash
   ./start.sh
   ```

#### 脚本功能：
- 自动检查 Node.js 和 npm 环境
- 自动安装项目依赖（如果 node_modules 不存在）
- 启动前端开发服务器
- 显示访问地址和停止服务器的方法

## 更新日志

### v0.1.0 (2024-01-01)
- 初始版本发布
- 实现基础的数据资产管理功能
- 支持数据库、API、MQ、日志的安全检测
- 提供实时和批量检测能力
- 实现数据分类分级功能