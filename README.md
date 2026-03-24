# Safe Paste

An IntelliJ IDEA plugin that intercepts multiline clipboard pastes into the terminal and warns you before potentially dangerous commands get executed.

---

## The Problem

You copy a snippet from the web or your notes, multiple lines of code or commands. You paste it into the terminal. The shell executes every line instantly. Things break.

This is especially dangerous in PowerShell, where a newline is enough to trigger execution.

---

## What Safe Paste Does

Safe Paste overrides the default `Terminal.Paste` action (`Ctrl+V`) and intercepts any paste that contains **2 or more non-blank lines**.

Instead of blindly dumping everything into your shell, it shows a dialog:

```
Warning: Multiline Paste

You are about to paste text that contains 3 non-empty lines.
If pasted directly into your shell, this may result in unexpected execution of commands.

Clipboard contents (preview):
┌─────────────────────────────────┐
│ git init                        │
│ git add .                       │
│ git commit                      │
└─────────────────────────────────┘

[ Paste as Plain Text ]  [ Paste Anyway (executes each line) ]  [ Cancel ]
```

### Options

| Option | Behavior |
|--------|----------|
| **Paste as Plain Text** | Pastes the text without executing it. Uses bracketed paste mode if the shell supports it; otherwise falls back to sending the text directly via the terminal input. You review the content in your prompt and press Enter when ready. |
| **Paste Anyway (executes each line)** | Sends each non-blank line to the terminal and executes it immediately, one by one. Same result as a normal paste in a shell that does not support bracketed paste. |
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

- IntelliJ IDEA 2025.3 or later (build 253+)
- The **new terminal** (Gen 2 / reworked engine) must be enabled — the plugin uses `com.intellij.terminal.frontend.view.TerminalView` which is part of the new terminal frontend. Make sure **"Enable New Terminal"** is active in **Settings → Tools → Terminal**.

---

## Compatibility

| Shell | Paste as Plain Text |
|-------|-------------------|
| PowerShell | Falls back to `sendString` via terminal input (no bracketed paste support) |
| bash / zsh | Uses bracketed paste mode if active — behavior depends on shell config |
| cmd | Not tested yet |

---

## Building from Source

```bash
git clone https://github.com/markocevrljakovic/safe-paste
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
        overrides="true">
</action>
```

On paste, it:

1. Reads clipboard contents
2. Counts non-blank lines
3. If fewer than 2 non-blank lines → passes through using bracketed paste mode directly
4. If 2+ non-blank lines → shows the warning dialog
5. Based on user choice:
   - **Paste as Plain Text** → calls `PasteStrategy.forShell()`, which uses bracketed paste mode if the shell supports it, or falls back to `sendString` via reflection on `TerminalView`
   - **Paste Anyway** → sends each non-blank line via `TerminalView.createSendTextBuilder().shouldExecute()`, executing them one by one
   - **Cancel** → no action

> **Note:** `TerminalViewAccessor` uses reflection against internal IDE APIs to detect bracketed paste mode and send text directly. This may break on future IDE updates — failures are caught, logged as warnings, and fall back to safe defaults.

---

## Contributing

PRs and issues welcome. This is an early version — shell compatibility and edge cases are the main areas that need work.

---

## License

Apache 2.0