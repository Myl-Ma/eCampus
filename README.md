# eCampus（校园二手平台）

## 1. 项目简介

本项目是一个校园二手交易 Mini App，包含：
- 首页浏览商品（支持缓存兜底）
- 商品详情（收藏/取消收藏）
- 发布商品（多图选择 + 裁剪 + 上传 + 创建记录）
- 收藏列表（Room 本地）
- 我的发布（按本机 userId 查询）

技术栈：Jetpack Compose + Navigation Compose + MVVM + Repository + Retrofit/OkHttp + Room + MMKV + Coil。

## 2. 运行前配置（local.properties）

**注意：SUPABASE_URL / SUPABASE_ANON_KEY 不要提交到 Git。**

在项目根目录 `local.properties` 中加入：

```properties
SUPABASE_URL=https://xxxx.supabase.co
SUPABASE_ANON_KEY=xxxx
# 可选：如果你修改过存储桶名称，可自行在代码中替换 DEFAULT_BUCKET
# SUPABASE_BUCKET=item-images
```

另外：为了跨设备展示发布者昵称，本项目会读取/写入 `items.owner_name`。
你需要在 Supabase 执行一次：

```sql
alter table public.items add column if not exists owner_name text;
```

## 3. 运行

- Android Studio 打开项目
- 同步 Gradle
- 选择 `app` 配置，点击 Run

## 4. 打包 APK

### 4.1 Debug 包

```bash
./gradlew :app:assembleDebug
```

产物一般在：
- `app/build/outputs/apk/debug/app-debug.apk`

### 4.2 Release 包

```bash
./gradlew :app:assembleRelease
```

如果你当前网络环境访问 `dl.google.com` 受限，可能会在 `lintVitalAnalyzeRelease` 阶段因 TLS/握手失败而中断。
解决思路：
- 换网络（手机热点通常最稳）
- 或在 `android { lint { ... } }` 中关闭 release lint（仅用于作业交付打包；正式发布建议恢复）

## 5. 启动图标设置（推荐）

Android Studio：
- 右键 `app/src/main/res` -> `New` -> `Image Asset`
- Icon Type 选择 `Launcher Icons (Adaptive and Legacy)`
- Name 保持 `ic_launcher`
- 选择你的 1024x1024 PNG
- Finish

生成/覆盖的资源位置（无需手工放置）：
- `app/src/main/res/mipmap-*/ic_launcher.webp`
- `app/src/main/res/mipmap-*/ic_launcher_round.webp`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

