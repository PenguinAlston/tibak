# Qwen Chat Platform

仿照 chat.qwen.ai 的 AI 聊天平台，包含完整的前后端实现。

## 技术栈

### 后端
- Spring Boot 3.x
- Spring AI Alibaba (通义千问集成)
- Spring WebFlux (SSE 流式响应)
- Spring Security + JWT
- MyBatis-Plus
- MongoDB (聊天记录存储)
- Redis (缓存)
- MySQL (用户数据)

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
- MySQL 8.0+
- MongoDB 7+
- Redis 7+

### 方式一：Docker Compose (推荐)

```bash
# 1. 复制环境变量文件
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env

# 2. 修改 .env 文件中的配置，特别是 DASHSCOPE_API_KEY

# 3. 启动所有服务
docker-compose up -d

# 4. 访问 http://localhost
```

### 方式二：本地开发

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
├── docker-compose.yml
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
ai:
  dashscope:
    api-key: your-api-key
    model: qwen-plus

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qwen_chat
    username: root
    password: your-password
  data:
    mongodb:
      uri: mongodb://localhost:27017/qwen_chat
    redis:
      host: localhost
      port: 6379
```

### 前端配置 (.env)

```
VITE_API_BASE_URL=http://localhost:8080/api
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
