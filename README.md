# CopyToAgent

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20IDEA-2025.3+-purple.svg)](https://plugins.jetbrains.com)

Copy file paths with line number references to your clipboard, formatted for Claude, OpenCode, and other AI coding assistants.

## Features

- Copy file path with line references from the editor (e.g. `@src/Main.kt#L10-20`)
- Supports two formats:
  - **Claude**: `@path/file#L10-20`
  - **OpenCode**: `@path/file#10-20`
- Relative or absolute path mode
- Without selection: copies file path only (no line numbers)
- Configurable notification on copy

## Installation

### From JetBrains Marketplace

1. Open **Settings → Plugins → Marketplace**
2. Search for **Copy To Agent**
3. Click **Install**

### Manual Installation

1. Download the `.zip` file from [Releases](https://github.com/baibaibaixiong23/CopyToAgent/releases)
2. Open **Settings → Plugins → ⚙️ → Install Plugin from Disk...**
3. Select the downloaded file

## Usage

1. Open a file in the editor
2. (Optional) Select lines of code
3. Press **Ctrl+Alt+U** (or right-click → **Copy Context Link**)
4. Paste into your AI assistant

### Examples

| Selection | Copied Text (Claude format) |
|-----------|----------------------------|
| No selection | `@src/Main.kt` |
| Single line (line 5) | `@src/Main.kt#L5` |
| Lines 10-20 | `@src/Main.kt#L10-20` |

## Configuration

**Settings → Tools → CopyToAgent**

| Option | Values | Default | Description |
|--------|--------|---------|-------------|
| Format | `claude`, `opencode` | `claude` | Line number reference format |
| Path Type | `relative`, `absolute` | `relative` | Path relative to project root or absolute |
| Show Notification | checkbox | off | Show balloon notification on copy |

> Changes require IDE restart to take effect.

## Building from Source

```bash
# Set JDK 21 path (adjust for your environment)
export JAVA_HOME=/path/to/jdk-21

# Build plugin
./gradlew buildPlugin

# Run sandbox IDE to test
./gradlew runIde

# Run tests
./gradlew test

# Verify plugin compatibility
./gradlew verifyPlugin
```

The built plugin zip will be in `build/distributions/`.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
