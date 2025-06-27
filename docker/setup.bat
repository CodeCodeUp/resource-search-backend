@echo off
echo 设置脚本执行权限...

REM 在Windows上，我们需要确保脚本可以在Git Bash或WSL中执行
REM 这个批处理文件主要用于Windows环境的初始化

echo.
echo Docker部署文件已创建完成！
echo.
echo 使用方法：
echo 1. 在Git Bash或WSL中执行：
echo    cd docker
echo    chmod +x *.sh
echo    ./quick-deploy.sh
echo.
echo 2. 或者使用完整部署：
echo    ./deploy.sh deploy
echo.
echo 3. 查看帮助：
echo    ./deploy.sh help
echo.
pause
