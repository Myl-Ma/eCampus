# eCampus— PROJECT GUIDE (for Windsurf)

本文件是项目根目录总规划与协作指南。Windsurf / AI coder 在实现时必须遵守本指南。

---

## A. 项目目标与作业要求对齐

**项目名称**：Campus Second-hand (Mini App)  
**目标**：完成一个校园二手平台 Mini App，可演示“浏览-详情-收藏-发布-我的发布”，满足：
- 功能：至少 1 个功能点（本项目包含多个）
- 技术：至少使用到网络/存储之一（本项目使用：网络 + 本地存储）
- 性能：不卡顿、无明显资源泄露
- 交付：产品报告 + 源码仓库 + 演示录屏

**最终可演示闭环**（录屏按这个走）：
1) 首页滑动浏览商品（流畅）
2) 打开详情并收藏
3) 收藏页看到该商品
4) 发布一个带图商品
5) 回首页刷新看到新商品
6) 我的发布列表看到自己发布的商品

---

## B. 技术选型（固定，不要随意更换）

### UI
- Jetpack Compose + Material3
- Navigation Compose（底部 Tab + 页面跳转）

### 架构
- MVVM（每个 Screen 一个 ViewModel）
- Repository（统一封装远端 + 本地）

### 异步与状态
- Kotlin Coroutines + Flow/StateFlow
- 所有网络/DB 都必须在 Dispatchers.IO

### 网络
- Retrofit + OkHttp
- Supabase：使用 REST 接口（避免引入过多 SDK 复杂度）

### 存储
- Room：收藏、列表缓存
- MMKV（腾讯开源）：保存 userId（模拟登录）、筛选偏好、发布草稿（可选）

### 图片
- Coil（加载图片 + 缓存）
- 发布图片：从系统相册多选；上传前做轻量压缩（可选实现，但推荐）

### 工程化（腾讯训练营契合）
- MMKV 必做
- 性能保障：基础做法 = 列表优化 + Profiler 检查（不额外接入复杂监控）

> 禁止：引入复杂 DI（Hilt/Koin）除非必要；禁止大改 Gradle/AGP/Compose 版本。

---

## C. 功能范围（MVP）

### P0 必做
1) Home：商品列表（封面图、标题、价格、校区、时间）
2) Detail：商品详情（多图、描述、联系按钮、收藏按钮）
3) Publish：发布商品（表单 + 多图选择 + 上传 + 创建记录）
4) Favorites：收藏列表（本地 Room）
5) My：我的发布列表（用 MMKV 的 userId 查询）

### P1 可选（做完 P0 再做）
- 搜索、筛选、下拉刷新、分页
- 删除/下架自己的商品

### 不做
- 在线聊天、支付、复杂鉴权（避免拖期）

---

## D. 数据模型（统一定义）

### 1) 业务模型 Item
- id: String
- title: String
- price: Double
- category: String
- campus: String
- description: String
- imageUrls: List<String>
- contact: String
- ownerId: String
- createdAt: Long

### 2) Room 表
A. favorites
- itemId: String (PK)
- createdAt: Long

B. cached_items（列表缓存）
- itemId: String (PK)
- title: String
- price: Double
- coverUrl: String
- campus: String
- createdAt: Long
- cachedAt: Long

### 3) MMKV Key
- key_user_id：首次启动生成 UUID，后续复用
- key_selected_campus：筛选偏好（可选）
- key_publish_draft：发布草稿（可选）

---

## E. Supabase 对接约定（安全规则）

**重要：Supabase URL/KEY 绝不提交到 Git。**

- 在 `local.properties` 写：
  - SUPABASE_URL=...
  - SUPABASE_ANON_KEY=...
  - SUPABASE_BUCKET=... (如 images)

- 代码从 local.properties 注入到 BuildConfig（或 gradle 生成常量）
- 提交前检查：repo 不出现 key 字符串

### 远端表建议
- items 表包含上面的字段
- storage bucket 存图片，返回 public url

---

## F. 页面结构与导航

底部 Tab：
- Home
- Publish
- Favorites
- Profile

导航：
- Home -> Detail(itemId)
- Favorites -> Detail(itemId)
- Profile -> MyItems -> Detail(itemId)

---

## G. 性能与“不卡顿/无泄露”硬性规则

1) 列表：Compose LazyColumn 必须使用 key(itemId)
2) 图片：Coil 加载，禁止在主线程做 bitmap 重压缩
3) 网络/DB：禁止主线程调用
4) ViewModel：禁止持有 Activity/Context（必要时用 Application）
5) 上传图片时：InputStream 用完必须关闭
6) 出错必须有 UI 状态（Loading/Error/Empty/Content）
7) 每个阶段完成后都要可运行可验证

---

## H. AI 协作规范（Windsurf 必须遵守）

### 每次改动必须输出
- 修改/新增的文件列表
- 做了什么、为什么这么做
- 我如何验证（点击路径 + 预期结果）

### 每次改动的范围
- 只做一个“小任务”到可验收，不要一次做太多
- 不要“顺手重构”或“升级版本”

### Git 工作流建议
- baseline commit（Android Studio 初始模板能运行）后再开始
- 每个阶段单独 commit：feat(home), feat(room), feat(supabase) ...

---

## I. 阶段计划（严格按顺序）

Stage 0：工程初始化与骨架（可运行）  
Stage 1：Home 列表 + Detail（先假数据）  
Stage 2：Room（收藏 + 列表缓存）  
Stage 3：MMKV（userId + 简单偏好）  
Stage 4：Supabase 拉列表 + 写缓存（网络失败读缓存）  
Stage 5：Publish 多图选择 + 上传 + 创建 item  
Stage 6：MyItems（我的发布）+ 全链路打磨  
Stage 7：性能检查 + 报告/录屏素材准备

完成标准：P0 全部可演示闭环跑通。