# Qwen Chat Platform

仿照 chat.qwen.ai 的 AI 聊天平台，包含完整的前后端实现。

## 技术栈

### 后端
- Spring Boot 3.x
- Spring WebFlux (SSE 流式响应)
- Spring Security + JWT
- MyBatis-Plus
- OkHttp (HTTP 客户端)
- PostgreSQL (主数据库 - 用户/对话/消息)
- MongoDB (可选，聊天记录存储)
- Redis (可选，缓存)

### 前端
- Vue 3 + TypeScript
- Vite
- Pinia (状态管理)
- Vue Router
- Element Plus (UI 组件库)
- Markdown-it (Markdown 渲染)
- Highlight.js (代码高亮)

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- Docker & Docker Compose

### 方式一：仅启动基础设施（推荐用于开发）

此方式只启动 PostgreSQL，前后端手动运行以便于开发调试。

```bash
# 1. 复制环境变量文件
cp .env.example .env

# 2. 修改 .env 文件，设置 DASHSCOPE_API_KEY

# 3. 启动基础设施（仅 PostgreSQL）
# Windows
docker-compose -f docker-compose.infra.yml up -d
# 或使用快捷脚本
start-infra.bat

# Linux/Mac
./start-infra.sh

# 4. 启动后端
cd backend
cp .env.example .env
# 编辑 .env 配置 API Key 和数据库密码
mvn clean install
cd qwen-chat-service
mvn spring-boot:run

# 5. 启动前端（新终端）
cd frontend
npm install
npm run dev

# 6. 访问 http://localhost:3000
```

**说明：** 默认配置仅使用 PostgreSQL 存储所有数据（用户/对话/消息），无需 MongoDB 和 Redis。

### 方式二：完整 Docker 部署（生产环境）

```bash
# 1. 复制环境变量文件
cp .env.example .env

# 2. 修改 .env 文件中的配置，特别是 DASHSCOPE_API_KEY

# 3. 启动所有服务
docker-compose up -d

# 4. 访问 http://localhost
```

### 方式三：完全本地开发（无 Docker）

需要手动安装 PostgreSQL 15+

#### 后端启动

```bash
cd backend

# 1. 配置环境变量
cp .env.example .env
# 编辑 .env 文件配置数据库和 API Key

# 2. 构建项目
mvn clean install

# 3. 启动服务
cd qwen-chat-service
mvn spring-boot:run
```

#### 前端启动

```bash
cd frontend

# 1. 安装依赖
npm install

# 2. 启动开发服务器
npm run dev

# 3. 访问 http://localhost:3000
```

## 项目结构

```
.
├── backend/
│   ├── qwen-chat-common/    # 公共模块
│   ├── qwen-chat-auth/      # 认证服务
│   └── qwen-chat-service/   # 核心聊天服务
├── frontend/
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── components/      # 组件
│   │   ├── router/          # 路由
│   │   ├── stores/          # 状态管理
│   │   ├── views/           # 页面
│   │   └── types/           # TypeScript 类型
│   └── package.json
├── docker-compose.yml           # 完整部署配置
├── docker-compose.infra.yml     # 仅 MySQL 配置
├── start-infra.sh               # Linux/Mac 启动脚本
├── start-infra.bat              # Windows 启动脚本
└── README.md
```

## API 文档

### 认证接口

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 聊天接口

- `POST /api/chat/completions` - 流式聊天 (SSE)
- `GET /api/chat/conversations` - 获取对话列表
- `POST /api/chat/conversations` - 创建新对话
- `GET /api/chat/conversations/{id}` - 获取对话详情
- `DELETE /api/chat/conversations/{id}` - 删除对话
- `PUT /api/chat/conversations/{id}` - 重命名对话

## 功能特性

- ✅ 用户注册/登录
- ✅ JWT Token 认证
- ✅ 新建对话
- ✅ 对话历史管理
- ✅ 删除/重命名对话
- ✅ SSE 流式响应
- ✅ Markdown 渲染
- ✅ 代码高亮
- ✅ 响应式布局

## 配置说明

### 后端配置 (application.yml)

```yaml
# Dashscope API Key (必填)
# 未配置时会返回占位回复
ai:
  dashscope:
    enabled: true
    api-key: ${DASHSCOPE_API_KEY:your-api-key-here}
    model: qwen-plus
    api-url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation

# 数据库配置 (PostgreSQL)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/qwen_chat
    username: postgres
    password: postgres
```

**注意：** 由于 Spring AI 依赖无法从公共 Maven 仓库获取，项目使用 OkHttp 直接调用 DashScope API。如需使用真实的 AI 功能，请设置环境变量 `DASHSCOPE_API_KEY`。

### 前端配置 (.env)

```
VITE_API_BASE_URL=http://localhost:8080/api
```

### 环境变量 (.env)

```bash
# PostgreSQL 配置
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Dashscope API Key
DASHSCOPE_API_KEY=your-api-key-here

# JWT 密钥
JWT_SECRET=your-secret-key
```

## 获取 Dashscope API Key

1. 访问 https://dashscope.console.aliyun.com/apiKey
2. 登录阿里云账号
3. 创建或获取 API Key
4. 复制到配置文件中

## 开发计划

- [ ] 模型选择功能
- [ ] 对话搜索功能
- [ ] 消息编辑功能
- [ ] 对话导出功能
- [ ] 主题切换
- [ ] 第三方登录

## License

MIT
