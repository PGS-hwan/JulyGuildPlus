# JulyGuildPlus

![JulyGuildPlus](./docs/assets/JulyGuildPlus.png)

[![GitHub](https://img.shields.io/badge/GitHub-JulyGuildPlus-blue?style=flat-square&logo=github)](https://github.com/PGS-hwan/JulyGuildPlus)
![Java](https://img.shields.io/badge/Java-21-red?style=flat-square&logo=java)
![Gradle](https://img.shields.io/badge/Build-Gradle-green?style=flat-square&logo=gradle)
![License](https://img.shields.io/badge/License-GPL3.0-orange?style=flat-square)

一款强大的 **Minecraft Bukkit 公会管理插件**，为您的服务器提供完整的公会系统解决方案。

## 🌟 核心特性

- ✨ **完整的公会系统** - 创建、加入、管理、解散公会，一站式解决方案
- 💰 **多经济插件支持** - 兼容 Vault、PlayerPoints 等经济插件
- ⚙️ **高度可自定义** - GUI、语言、商店等全面自定义，支持 PAPI 变量和彩色文本
- 💬 **聊天功能** - 兼容 TrChat 和 Essentials 聊天格式
- 🚀 **稳定高效** - 支持 Paper/Spigot 1.7.10 ~ 1.21.11
- 📦 **开源免费** - 新插件继续使用 GPL-3.0 协议开源

### 🤔 对比旧插件，我们的优势？

- ✨ **全新特性** - 优化代码架构，支持更新的 Minecraft 版本  
- 🔧 **配置兼容** - 迁移至新插件？配置文件完全兼容，轻松复制替换！
- 🎮 **指令优化** - 更好记的指令格式和多样的别名，玩家和管理都能轻松使用！

## 📋 系统要求

| 要求 | 版本 |
|------|------|
| **Java** | 8 或更高 |
| **服务端** | 1.7.10 ~ 1.21.11 |
| **必需依赖** | Vault |

## 🚀 快速开始

### 1️⃣ 安装插件

1. 从 [Release](../../release) 下载最新版本
2. 将 `JulyGuildPlus-x.x.x.jar` 放入服务器 `plugins` 文件夹
3. **完全重启**服务器。大功告成！

### 2️⃣ 验证安装

重启后，控制台应显示：
```bash
[JulyGuildPlus] 插件版本: x.x.x
[JulyGuildPlus] Vault: Hook成功!
[JulyGuildPlus] PlaceholderAPI: Hook成功!
```

### 玩家指令

| 指令 | 说明 | 权限 |
|------|------|------|
| `/jgp gui` | 打开公会主界面 | `JulyGuildPlus.use` |
| `/jgp version` | 显示插件版本 | 无 |

> 💡 **指令别名**: `/guild`, `/jg`, `/gh`

### 管理员指令

| 指令 | 说明 | 权限 |
|------|------|------|
| `/jgp reload` | 重载所有配置 | `JulyGuildPlus.admin` |
| `/jgp admin debug` | 切换调试模式 | `JulyGuildPlus.admin` |
| `/jgp admin getItemInfo` | 获取物品信息 | `JulyGuildPlus.admin` |

### 查看更多内容？
移步 [JulyGuildPlus-Wiki](../../wiki) 进行查阅！

## 🛠️ 技术栈

- **语言**: Java 21
- **构建系统**: Gradle 9.2.0 + ShadowJar 9.3.1

### 构建项目

```bash
# 清洁和打包完整文件
./gradlew clean ShadowJar

# 仅编译
./gradlew compileJava
```

## 🚨 常见问题（Q&A）

### Q: Vault Hook 失败怎么办？
A: 确保已安装 Vault 插件和经济插件（如 Essentials），然后**完全重启**服务器。

### Q: GUI 变量不显示？
A: 需要安装 PlaceholderAPI 插件，并下载相应扩展。

更多问题可查看 [完整 FAQ](docs/常见问题.md)

## 📖 更多内容

### 统计数据

本项目使用 bStats 来收集匿名统计数据，用于改进插件。您可以在 [bStats 平台](https://bstats.org/) 上查看统计信息。

### 贡献和反馈

- **GitHub Issues**: [提交问题和建议](../../issues)


### 致谢

- 感谢 Cobyqwq 的 [JulyLibrary-Fix](https://github.com/Cobyqwq/JulyLibrary_Fix/releases) 项目！
- 感谢所有为本项目做出贡献和反馈的人！

> ⭐ 如果您觉得本项目不错，欢迎给予 Star ⭐
