#!/usr/bin/env bash
# 家账本：一键打出可安装到手机的 Debug APK（方案一：传 APK 安装）
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

SDK_DIR="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}"
if [[ ! -d "$SDK_DIR" ]]; then
  echo "未找到 Android SDK：$SDK_DIR"
  echo "请先安装 Android Studio，并在首次向导中安装 Android SDK。"
  exit 1
fi

if [[ ! -f "$ROOT_DIR/local.properties" ]]; then
  echo "sdk.dir=$SDK_DIR" > "$ROOT_DIR/local.properties"
  echo "已生成 local.properties -> $SDK_DIR"
fi

echo ">> 开始打包 Debug APK..."
./gradlew :app:assembleDebug --no-daemon

APK_SRC="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
DIST_DIR="$ROOT_DIR/dist"
mkdir -p "$DIST_DIR"

VERSION="$(grep versionName "$ROOT_DIR/app/build.gradle.kts" | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
OUT_NAME="home-ledger-${VERSION}-debug.apk"
cp "$APK_SRC" "$DIST_DIR/$OUT_NAME"

echo ""
echo "打包完成："
echo "  $DIST_DIR/$OUT_NAME"
echo ""
echo "安装到手机："
echo "  1. 把该文件传到手机（微信/数据线/网盘）"
echo "  2. 在手机上打开 APK，允许「安装未知应用」后安装"
echo "  3. 若提示无法安装，请确认手机 Android 版本 >= 8.0（API 26）"
