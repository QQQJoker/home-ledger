# 家账本（Home Ledger）

一个面向个人/小家庭的离线记账 App，专注「快速记账、清晰统计、本地安全」。

## 核心功能

- 记一笔：支持收入/支出、分类、账户、备注、发生时间（默认当前时间可修改）
- 流水：按时间倒序展示，支持时间/类型/账户/分类筛选
- 账户：支持新增账户与初始余额，流水会联动账户余额
- 统计：
  - 周期支持月度 / 年度 / 全部
  - 汇总卡展示收入、支出、结余
  - 支出分类占比图
  - 年度与全部视图支持收支趋势折线图（年度按月、全部按年）
- 备份恢复：AES-GCM 加密备份，导出/恢复均通过密码弹窗校验
- 应用锁：支持 PIN 解锁与后台超时锁定

## 技术栈

- Kotlin + Jetpack Compose
- Room（本地数据库）
- Hilt（依赖注入）
- MVVM + Repository

## 项目结构

- `app/src/main/java/com/joker/homeledger/feature/entry`：记一笔
- `app/src/main/java/com/joker/homeledger/feature/ledger`：流水
- `app/src/main/java/com/joker/homeledger/feature/stats`：统计
- `app/src/main/java/com/joker/homeledger/feature/account`：账户管理
- `app/src/main/java/com/joker/homeledger/feature/backup`：备份恢复
- `app/src/main/java/com/joker/homeledger/core`：数据库、仓储、公共能力
- `docs`：需求/设计/开发计划等文档

## 本地运行与打包

### 环境要求

- JDK 17+
- Android SDK（compileSdk 34）

### 打包 Debug APK

```bash
./scripts/build-apk.sh
```

打包输出路径：

`dist/home-ledger-<version>-debug.apk`

## 当前版本

- 版本号：`1.0.4`
- 主要能力：统计周期重构（月/年/全部）、备份密码弹窗流程、账户余额联动

---

如需查看详细规划与变更记录，请阅读：

- `docs/需求说明书.md`
- `docs/设计文档-v1.0.md`
- `docs/开发计划-v1.0.md`
