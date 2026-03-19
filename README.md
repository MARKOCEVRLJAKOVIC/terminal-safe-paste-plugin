# Safe Paste

An IntelliJ IDEA plugin that intercepts multiline clipboard pastes into the terminal and warns you before potentially dangerous commands get executed.

---

## The Problem

You copy a snippet from the web or your notes and multiple lines of code or commands. You paste it into the terminal. Shell executes every line instantly. Things break.

This is especially dangerous in PowerShell where a newline is enough to trigger execution.

---

## What Safe Paste Does

Safe Paste overrides the default `Terminal.Paste` action (`Ctrl+V`) and intercepts any paste that contains **2 or more non-blank lines**.

Instead of blindly dumping everything into your shell, it shows a dialog:

```
Warning: Multiline Paste

You are about to paste text that contains 3 lines.
If pasted directly into your shell, this may result in unexpected execution of commands.

Clipboard contents (preview):
┌─────────────────────────────────┐
│ git init                        │
│ git add .                       │
│ git commit                      │
└─────────────────────────────────┘

[ Paste as Plain Text ]  [ Paste Anyway ]  [ Cancel ]
```

### Options

| Option | Behavior |
|--------|----------|
| **Paste as Plain Text** | Pastes all lines into the prompt using PowerShell line continuation (`` ` ``). Lines appear in your prompt for review — you press Enter when ready. |
| **Paste Anyway** | Executes each line immediately, one by one. Same as normal paste. |
| **Cancel** | Does nothing. Clipboard contents are not pasted. |

Single-line pastes are passed through instantly with no dialog.

---

## Installation

1. Open IntelliJ IDEA
2. Go to **Settings → Plugins → Marketplace**
3. Search for **Safe Paste**
4. Click **Install**

Or install manually from a `.zip`:

1. Download the latest release from [Releases](../../releases)
2. Go to **Settings → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded `.zip`

---

## Requirements

- IntelliJ IDEA 2024.1 or later
- The **new terminal** (reworked engine) must be enabled — the plugin uses `com.intellij.terminal.frontend.view.TerminalView` which is part of the new terminal frontend

---

## Compatibility

| Shell | Paste as Plain Text |
|-------|-------------------|
| PowerShell | Uses `` ` `` line continuation |
| bash / zsh | May vary — bracketed paste mode behavior depends on shell config |
| cmd | Not tested yet |

---

## Building from Source

```bash
git clone https://github.com/yourname/safe-paste
cd safe-paste
./gradlew buildPlugin
```

The plugin `.zip` will be in `build/distributions/`.

To run in a sandboxed IDE:

```bash
./gradlew runIde
```

---

## How It Works

Safe Paste registers itself as an override of the built-in `Terminal.Paste` action via `plugin.xml`:

```xml
<action id="Terminal.Paste"
        class="dev.marko.safepaste.terminal.SafePasteAction"
        overrides="true"
        text="Safe Paste">
</action>
```

On paste, it:
1. Reads clipboard contents
2. Counts non-blank lines
3. If 1 line → passes through normally
4. If 2+ lines → shows the warning dialog
5. Based on user choice → sends text via `TerminalView.createSendTextBuilder()`

For **Paste as Plain Text**, each line except the last is sent with a trailing `` ` `` (PowerShell continuation character) and executed, placing the cursor on the next continuation line. The final line is sent without execution, waiting for the user to press Enter.

---

## Contributing

PRs and issues welcome. This is an early version, shell compatibility and edge cases are the main areas that need work.

---

## License

MIT

# IntelliJ Platform Plugin Template

[![Twitter Follow](https://img.shields.io/badge/follow-%40JBPlatform-1DA1F2?logo=twitter)](https://twitter.com/JBPlatform)
[![Developers Forum](https://img.shields.io/badge/JetBrains%20Platform-Join-blue)][jb:forum]

## Plugin template structure

A generated project contains the following content structure:

```
.
├── .run/                   Predefined Run/Debug Configurations
├── build/                  Output build directory
├── gradle
│   ├── wrapper/            Gradle Wrapper
├── src                     Plugin sources
│   ├── main
│   │   ├── kotlin/         Kotlin production sources
│   │   └── resources/      Resources - plugin.xml, icons, messages
├── .gitignore              Git ignoring rules
├── build.gradle.kts        Gradle build configuration
├── gradle.properties       Gradle configuration properties
├── gradlew                 *nix Gradle Wrapper script
├── gradlew.bat             Windows Gradle Wrapper script
├── README.md               README
└── settings.gradle.kts     Gradle project settings
```

In addition to the configuration files, the most crucial part is the `src` directory, which contains our implementation
and the manifest for our plugin – [plugin.xml][file:plugin.xml].

> [!NOTE]
> To use Java in your plugin, create the `/src/main/java` directory.

## Plugin configuration file

The plugin configuration file is a [plugin.xml][file:plugin.xml] file located in the `src/main/resources/META-INF`
directory.
It provides general information about the plugin, its dependencies, extensions, and listeners.

You can read more about this file in the [Plugin Configuration File][docs:plugin.xml] section of our documentation.

If you're still not quite sure what this is all about, read our
introduction: [What is the IntelliJ Platform?][docs:intro]

$H$H Predefined Run/Debug configurations

Within the default project structure, there is a `.run` directory provided containing predefined *Run/Debug
configurations* that expose corresponding Gradle tasks:

| Configuration name | Description                                                                                                                                                                         |
|--------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Run Plugin         | Runs [`:runIde`][gh:intellij-platform-gradle-plugin-runIde] IntelliJ Platform Gradle Plugin task. Use the *Debug* icon for plugin debugging.                                        |
| Run Tests          | Runs [`:test`][gradle:lifecycle-tasks] Gradle task.                                                                                                                                 |
| Run Verifications  | Runs [`:verifyPlugin`][gh:intellij-platform-gradle-plugin-verifyPlugin] IntelliJ Platform Gradle Plugin task to check the plugin compatibility against the specified IntelliJ IDEs. |

> [!NOTE]
> You can find the logs from the running task in the `idea.log` tab.

## Publishing the plugin

> [!TIP]
> Make sure to follow all guidelines listed in [Publishing a Plugin][docs:publishing] to follow all recommended and
> required steps.

Releasing a plugin to [JetBrains Marketplace](https://plugins.jetbrains.com) is a straightforward operation that uses
the `publishPlugin` Gradle task provided by
the [intellij-platform-gradle-plugin][gh:intellij-platform-gradle-plugin-docs].

You can also upload the plugin to the [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/upload)
manually via UI.

## Useful links

- [IntelliJ Platform SDK Plugin SDK][docs]
- [IntelliJ Platform Gradle Plugin Documentation][gh:intellij-platform-gradle-plugin-docs]
- [IntelliJ Platform Explorer][jb:ipe]
- [JetBrains Marketplace Quality Guidelines][jb:quality-guidelines]
- [IntelliJ Platform UI Guidelines][jb:ui-guidelines]
- [JetBrains Marketplace Paid Plugins][jb:paid-plugins]
- [IntelliJ SDK Code Samples][gh:code-samples]

[docs]: https://plugins.jetbrains.com/docs/intellij

[docs:intro]: https://plugins.jetbrains.com/docs/intellij/intellij-platform.html?from=IJPluginTemplate

[docs:plugin.xml]: https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html?from=IJPluginTemplate

[docs:publishing]: https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate

[file:plugin.xml]: ./src/main/resources/META-INF/plugin.xml

[gh:code-samples]: https://github.com/JetBrains/intellij-sdk-code-samples

[gh:intellij-platform-gradle-plugin]: https://github.com/JetBrains/intellij-platform-gradle-plugin

[gh:intellij-platform-gradle-plugin-docs]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html

[gh:intellij-platform-gradle-plugin-runIde]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#runIde

[gh:intellij-platform-gradle-plugin-verifyPlugin]: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#verifyPlugin

[gradle:lifecycle-tasks]: https://docs.gradle.org/current/userguide/java_plugin.html#lifecycle_tasks

[jb:github]: https://github.com/JetBrains/.github/blob/main/profile/README.md

[jb:forum]: https://platform.jetbrains.com/

[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html

[jb:paid-plugins]: https://plugins.jetbrains.com/docs/marketplace/paid-plugins-marketplace.html

[jb:quality-guidelines]: https://plugins.jetbrains.com/docs/marketplace/quality-guidelines.html

[jb:ipe]: https://jb.gg/ipe

[jb:ui-guidelines]: https://jetbrains.github.io/ui
