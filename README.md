# 轻序日程 (LitePlanner) - 原生 Android 日程规划 App

本项目是一个基于 **Kotlin + Jetpack Compose (Material 3) + Room (SQLite)** 开发的纯本地安卓日程规划应用，无需联网即可运行，所有数据 100% 存储在手机本地数据库。

## 核心功能
1. **日 / 周 / 月日程视图切换**：
   - **日视图**：按时间轴展示全天日程，支持分类标签、时间段与消息提醒开关。
   - **周视图**：7 天横向周历对比，快速浏览整周安排。
   - **月视图**：完整月历网格，点击任意日期查看或新增当日日程。
2. **四象限优先级待办（艾森豪威尔矩阵）**：
   - 划分为「重要且紧急」「重要不紧急」「紧急不重要」「不重要不紧急」四个象限卡片，支持一键勾选完成、象限调整与到期消息通知。
3. **番茄钟专注计时**：
   - 支持专注（默认 25 分钟，可自定义时长）、短休息（5 分钟）、长休息（15 分钟）模式，可关联待办任务，计时结束自动发送系统通知栏消息提醒并记录今日专注时长。
4. **习惯打卡**：
   - 支持自定义日常习惯图标与名称、每日打卡、最近 7 天打卡矩阵、连续打卡天数（Streak）统计与每日定时消息提醒。
5. **纯消息通知提醒（无闹钟吵闹）**：
   - 使用 Android `NotificationManager` + `AlarmManager` 推送标准系统状态栏与锁屏横幅消息提醒，不触发闹钟铃声循环。
6. **100% 手机本地存储**：
   - 采用 Jetpack Room (SQLite) 持久化保存日程、四象限待办、番茄钟记录与习惯打卡数据，无需账号登录。

---

## 如何生成并安装 APK 到安卓手机？

### 方法一：免电脑 / 免装环境，使用 GitHub Actions 云端自动生成 APK（推荐，约 3 分钟）
如果你身边没有安装 Android Studio，可以直接利用本项目内置的 `.github/workflows/build-apk.yml` 自动打包：
1. 注册或登录 [GitHub](https://github.com)，新建一个空白仓库（Repository）。
2. 将本压缩包解压后的所有文件上传到该仓库（确保 `.github/workflows/build-apk.yml` 包含在内）。
3. 点击仓库顶部的 **Actions** 标签页，等待 `Build Android APK` 工作流自动运行完成（绿色勾号）。
4. 点进该次构建任务，在底部的 **Artifacts** 区域点击下载 `LitePlanner-debug-apk`，解压即可获得 `app-debug.apk`，直接发送到安卓手机点击安装即可！

### 方法二：使用 Android Studio 本地编译生成 APK
1. 下载并安装 **Android Studio (Ladybug / Koala 或更新版本)**。
2. 解压本压缩包，在 Android Studio 中选择 **Open** 打开 `SchedulePlannerAndroid` 文件夹。
3. 等待 Gradle 自动同步依赖完成。
4. **直接安装到手机**：用数据线连接开启了「USB 调试」的安卓手机，点击顶部绿色运行按钮 **Run 'app'**。
5. **导出 APK 安装包**：点击顶部菜单 **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**，编译完成后在 `app/build/outputs/apk/debug/app-debug.apk` 获取安装包。

---

## 工程目录结构
```text
SchedulePlannerAndroid/
├── .github/workflows/build-apk.yml   # GitHub Actions 云端自动打包 APK 配置
├── build.gradle.kts                  # 根项目 Gradle 配置
├── settings.gradle.kts               # 模块与仓库设置
├── gradle.properties                 # Gradle 参数配置
└── app/
    ├── build.gradle.kts              # App 模块依赖 (Compose, Room, Material3)
    └── src/main/
        ├── AndroidManifest.xml       # 权限与组件声明 (通知权限、定时广播接收器)
        └── java/com/liteplanner/app/
            ├── MainActivity.kt               # 主界面入口与底部导航
            ├── data/
            │   ├── Entities.kt               # 日程、四象限待办、习惯、番茄钟本地数据表
            │   ├── PlannerDao.kt             # Room 数据库访问接口
            │   └── AppDatabase.kt            # 本地 SQLite 数据库单例
            ├── notification/
            │   ├── NotificationHelper.kt     # 通知渠道与定时消息提醒调度器
            │   └── ReminderReceiver.kt       # 广播接收器：到点发送系统消息通知
            ├── viewmodel/
            │   └── PlannerViewModel.kt       # 业务逻辑与状态管理
            └── ui/
                ├── Theme.kt                  # Material 3 主题配色
                ├── CalendarScreen.kt         # 日/周/月视图切换与日程管理页
                ├── QuadrantTodoScreen.kt     # 四象限优先级待办页
                ├── PomodoroScreen.kt         # 番茄钟专注计时页
                └── HabitScreen.kt            # 习惯打卡统计页
```
