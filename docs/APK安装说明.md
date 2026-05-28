# 家账本 — APK 安装说明（方案一）

自用版本采用 **「Mac 上打安装包 → 传到手机安装」**，不需要 USB 调试，也不需要一直连着电脑。

---

## 一、Mac 上准备（只需做一次）

1. 安装 [Android Studio](https://developer.android.com/studio)，按向导安装 **Android SDK**。
2. 用 Android Studio 打开本项目目录 `home-ledger`，等待 **Gradle Sync** 成功（首次会下载依赖，需联网）。
3. 之后每次出新版本，在 Mac 终端执行：

```bash
cd /Users/joker/code/home-ledger
chmod +x scripts/build-apk.sh   # 首次需要
./scripts/build-apk.sh
```

4. 安装包输出在：

```
dist/home-ledger-1.0.0-debug.apk
```

（版本号会随 `app/build.gradle.kts` 里的 `versionName` 变化。）

> 也可在 Android Studio 菜单：**Build → Build APK(s)**，产物在  
> `app/build/outputs/apk/debug/app-debug.apk`。

---

## 二、传到手机并安装

1. 将 `.apk` 传到手机（微信文件助手、数据线、网盘、AirDrop 到安卓设备等）。
2. 在手机上点击 APK 文件。
3. 若提示「不允许安装未知应用」，到系统设置里对该来源（如文件管理器/微信）**允许安装未知应用**。
4. 安装完成后桌面会出现 **家账本**。

---

## 三、更新版本

1. Mac 上重新执行 `./scripts/build-apk.sh` 得到新 APK。
2. 在手机上**覆盖安装**（直接装新 APK 即可，本地数据一般保留；若包名或签名变化可能需先卸载旧版）。

---

## 四、常见问题

| 问题 | 处理 |
|------|------|
| Mac 打包失败，提示找不到 SDK | 先打开 Android Studio 完成 SDK 安装；或确认 `~/Library/Android/sdk` 存在 |
| 手机提示「应用未安装」 | 检查 Android 是否 ≥ 8.0；是否下载完整；是否允许未知来源 |
| 和商店版冲突 | 本应用为自用包名 `com.joker.homeledger`，与商店应用无关 |

---

## 五、与「真机调试」的区别

| 方式 | 是否需要 USB / 调试模式 | 适合场景 |
|------|-------------------------|----------|
| **本方案（APK 安装）** | 否 | 自用、偶尔更新 |
| Studio 真机调试 | 是 | 开发中频繁改代码、看日志 |

当前项目按 **本方案** 交付即可满足自用需求。
