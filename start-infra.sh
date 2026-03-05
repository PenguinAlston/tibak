# 快速启动基础设施
# 使用此脚本启动 MySQL、MongoDB 和 Redis

#!/bin/bash

echo "🚀 启动 Qwen Chat 基础设施..."

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 启动服务
docker-compose -f docker-compose.infra.yml up -d

# 等待 MySQL 就绪
echo "⏳ 等待 MySQL 就绪..."
while ! docker exec qwen-chat-mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
    sleep 2
done

echo ""
echo "✅ 基础设施启动成功！"
echo ""
echo "📊 服务状态:"
echo "  - MySQL:    localhost:3306"
echo "  - MongoDB:  localhost:27017"
echo "  - Redis:    localhost:6379"
echo ""
echo "📝 数据库连接信息:"
echo "  - MySQL:    root / ${MYSQL_PASSWORD:-root}"
echo "  - Database: qwen_chat"
echo ""
echo "🛑 停止服务：docker-compose -f docker-compose.infra.yml down"
echo "📋 查看日志：docker-compose -f docker-compose.infra.yml logs -f"
