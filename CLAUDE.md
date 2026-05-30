# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

CopyToAgent 是一个 JetBrains IntelliJ 平台插件，功能对标 `vscodeExtend/` 目录中的 VSCode 插件 "Context Link"。

**核心功能**: 复制文件路径（带行号引用）到剪贴板，格式兼容 Claude (`@path/file#L10-20`) 和 OpenCode (`@path/file#10-20`)，用于 AI 助手的上下文引用。

当前状态：IntelliJ 插件模板代码（MyToolWindowFactory 等），尚未实现实际功能。

## 参考实现

`vscodeExtend/extension.js` 是完整的参考实现，核心逻辑：
- `buildContextLink(filePath, selection, format)` — 根据选区和格式生成上下文链接
- 无选区 → `@path/file`；单行 → `@path/file#L10`；多行 → `@path/file#L10-20`
- 配置项：`format`（claude/opencode）、`pathType`（relative/absolute）、`showNotification`

## 构建与开发命令

```bash
export JAVA_HOME=$(路径到你的 JDK 21)

# 构建插件
./gradlew buildPlugin

# 运行沙盒 IDE 测试插件
./gradlew runIde

# 运行测试
./gradlew test

# 代码检查
./gradlew verifyPlugin

# 运行单个测试类
./gradlew test --tests "ztf.extend.SomeTestClass"
```

## 技术栈

- **语言**: Kotlin
- **构建工具**: Gradle (Kotlin DSL) + IntelliJ Platform Gradle Plugin 2.16.0
- **目标平台**: IntelliJ IDEA 2025.3.5
- **Kotlin**: 2.2.20（注意：`kotlin.stdlib.default.dependency=false`，需手动添加 stdlib 依赖）
- **Group**: `ztf.extend`
- **包名**: `ztf.extend`

## 项目结构

```
src/main/kotlin/          # Kotlin 源码（包名 ztf.extend）
src/main/resources/
  META-INF/plugin.xml     # 插件注册入口（actions、extensions、resource-bundle）
  messages/               # 国际化 properties 文件
vscodeExtend/             # 参考的 VSCode 插件源码（只读参考）
.run/                     # IntelliJ 运行配置
gradle/libs.versions.toml # 版本目录
```

## 插件开发关键约定

- **plugin.xml** 是插件入口：注册 Action、ToolWindow、Extension Point、ResourceBundle
- **Action** 通过 `plugin.xml` 中的 `<actions>` 注册，快捷键通过 `<keyboard-shortcut>` 配置
- **配置项** 通过 `PersistentStateComponent` + `plugin.xml` 中的 `<extensions defaultExtensionNs="com.intellij">` 的 `<applicationConfigurable>` 注册
- **剪贴板操作**: 使用 `IdeClipboard.getInstance().setContents()` 或 `CopyPasteManager.getInstance().setContents()`
- **国际化**: 保持现有的 `DynamicBundle` 模式（MyMessageBundle），文本放 properties 文件
