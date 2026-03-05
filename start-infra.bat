@echo off
chcp 65001 >nul
echo 🚀 启动 Qwen Chat 基础设施...

REM 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker 未运行，请先启动 Docker
    exit /b 1
)

REM 启动服务
docker-compose -f docker-compose.infra.yml up -d

REM 等待 PostgreSQL 就绪
echo ⏳ 等待 PostgreSQL 就绪...
:wait_postgres
docker exec qwen-chat-postgres pg_isready -U postgres >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto wait_postgres
)

echo.
echo ✅ 基础设施启动成功！
echo.
echo 📊 服务状态:
echo   - PostgreSQL:  localhost:5432
echo.
echo 📝 数据库连接信息:
echo   - Host:     localhost:5432
echo   - Database: qwen_chat
echo   - User:     postgres
echo.
echo 🛑 停止服务：docker-compose -f docker-compose.infra.yml down
echo 📋 查看日志：docker-compose -f docker-compose.infra.yml logs -f
pause
