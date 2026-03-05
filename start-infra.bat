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

REM 等待 MySQL 就绪
echo ⏳ 等待 MySQL 就绪...
:wait_mysql
docker exec qwen-chat-mysql mysqladmin ping -h localhost --silent >nul 2>&1
if errorlevel 1 (
    timeout /t 2 /nobreak >nul
    goto wait_mysql
)

echo.
echo ✅ 基础设施启动成功！
echo.
echo 📊 服务状态:
echo   - MySQL:    localhost:3306
echo   - MongoDB:  localhost:27017
echo   - Redis:    localhost:6379
echo.
echo 📝 数据库连接信息:
echo   - MySQL:    root / %MYSQL_PASSWORD:-root%
echo   - Database: qwen_chat
echo.
echo 🛑 停止服务：docker-compose -f docker-compose.infra.yml down
echo 📋 查看日志：docker-compose -f docker-compose.infra.yml logs -f
pause
