#!/bin/bash
# ============================================================
#  系统工具箱 一键部署脚本
#  用法: ./deploy.sh
#
#  首次使用前，修改下方 SERVER_* 三个变量为你的云服务器信息。
#  推荐配置 SSH 免密登录，避免每次输入密码:
#    ssh-copy-id user@your-server
#
#  脚本做的事:
#    1. gradle assembleRelease  构建 release APK
#    2. scp 上传到云服务器 nginx 目录
#    3. 打印手机下载 URL
# ============================================================

set -euo pipefail

# ---------- 修改这里 ----------
SERVER_USER="root"
SERVER_HOST="your-server-ip-or-domain"
SERVER_PATH="/var/www/wechat-notifier"          # nginx 静态文件根目录
NGINX_SERVE_NAME="your-domain-or-ip"           # 用于打印 URL
NGINX_PORT="80"                                 # 如果 nginx 监听 443，改成 443
# -----------------------------

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APK_NAME="app-release.apk"
LOCAL_APK="${SCRIPT_DIR}/app/build/outputs/apk/release/${APK_NAME}"
REMOTE_PATH="${SERVER_USER}@${SERVER_HOST}:${SERVER_PATH}/"

echo "==> 构建 Release APK..."
cd "${SCRIPT_DIR}"

# 查找 gradle：优先用 wrapper，其次用系统路径
if [ -f "./gradlew" ]; then
    GRADLE="./gradlew"
elif command -v gradle &> /dev/null; then
    GRADLE="gradle"
else
    echo "错误: 找不到 gradle。请在项目根目录运行 'gradle wrapper' 生成 wrapper。"
    exit 1
fi

${GRADLE} assembleRelease

echo ""
echo "==> 上传到 ${REMOTE_PATH} ..."
scp "${LOCAL_APK}" "${REMOTE_PATH}"

echo ""
echo "==> 完成！"
echo ""
echo "  手机浏览器打开以下地址下载安装:"
echo "  http://${NGINX_SERVE_NAME}:${NGINX_PORT}/${APK_NAME}"
echo ""
echo "  注意: 安装前请先卸载旧版本，或使用 adb install -r 覆盖安装。"
