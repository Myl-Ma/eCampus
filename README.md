# eCampus - 校园二手交易 Mini App

eCampus 是一款面向校园场景的 **二手物品交易 Mini App**，旨在为在校学生提供一个便捷、安全、轻量的二手物品发布与浏览平台。  
本项目完整地实现了从产品设计、功能开发到打包发布的全流程。

---

## ✨ 项目背景

在校园场景中，学生之间经常存在教材、电子产品、生活用品等物品的闲置与流转需求。  
eCampus 以“校园内部流转”为核心理念，聚焦 **轻量化、低学习成本、真实使用场景**，打造一款适合校园用户的二手交易应用。

---

## 🧩 功能介绍

当前版本已实现以下核心功能模块：

### 1️⃣ 首页浏览
- 展示校园二手物品列表
- 支持图文卡片式展示
- 显示物品名称、价格、发布时间等关键信息

### 2️⃣ 物品详情页
- 查看单个物品的详细信息
- 展示完整描述与发布时间
- 支持收藏操作

### 3️⃣ 发布物品
- 发布二手物品信息（标题 / 描述 / 价格）
- 模拟真实发布流程
- 为后续接入后端接口预留结构

### 4️⃣ 我的收藏
- 查看已收藏的物品
- 基于本地数据库持久化存储

### 5️⃣ 个人页面
- 展示个人相关入口
- 预留账号体系与个人物品管理扩展空间

---

## 🏗️ 技术架构

### 技术选型
- **开发语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **架构模式**：MVVM
- **本地存储**：Room 数据库
- **网络层（预留）**：Supabase（BaaS）
- **依赖管理**：Gradle Version Catalog
- **构建工具**：Android Studio

### 架构分层
UI Layer → Jetpack Compose Screens
ViewModel → 状态管理 & 业务逻辑
Repository → 数据统一入口
Data Layer → Room / Remote（Supabase 预留）

yaml
复制代码

---

## 📂 项目结构说明

app/
├── data/ # 数据层（entity / dao / repository）
├── ui/
│ ├── screens/ # 页面（Home / Detail / Publish / Profile 等）
│ ├── components/# 可复用 UI 组件
│ └── navigation/# 导航与路由
├── utils/ # 工具类与状态封装
└── MainActivity # 应用入口

yaml
复制代码

---

## 🚀 运行方式

1. 使用 Android Studio 打开项目
2. 等待 Gradle Sync 完成
3.在项目根目录下新建local.properties 文件，并添加以下内容：SUPABASE_URL=xxxxx
SUPABASE_ANON_KEY=xxxxxxx
4. 连接 Android 真机或启动模拟器
5. 点击 **Run** 即可运行

---

## 📦 APK 安装说明

项目已生成 release APK，可直接安装到 Android 设备：

- 在手机中允许「安装未知来源应用」
- 点击 APK 文件完成安装

---

## 📹 演示说明

本项目已完成完整功能演示录屏，用于腾讯训练营结营提交。

---

## 📌 说明

- 本项目为个人学习与训练营作业使用
- 所有功能均由本人独立完成
- 后端接口已预留，具备继续扩展为完整产品的能力

---