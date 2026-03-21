package dev.marko.safepaste.terminal

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.terminal.frontend.view.TerminalView
import java.awt.datatransfer.DataFlavor

/**
 * Overrides the default Terminal.Paste action (Ctrl+V) to intercept multiline pastes.
 *
 * When the clipboard contains multiple non-blank lines, the user is prompted with a dialog
 * offering three choices: paste as plain text (safe), paste anyway (executes each line),
 * or cancel. Single-line pastes are forwarded as-is using bracketed paste mode.
 *
 * Background: PowerShell does not support bracketed paste mode, so pasting multiline text
 * via Ctrl+V causes each line to be executed immediately as a separate command. This action
 * prevents that by routing multiline pastes through a safer code path.
 */
class SafePasteAction : DumbAwareAction() {

    private val log = logger<SafePasteAction>()

    override fun actionPerformed(e: AnActionEvent) {
        val terminalView = e.getData(TerminalView.DATA_KEY) ?: return
        val clipboardText = clipboardText() ?: return
        val lines = clipboardText.lines().filter { it.isNotBlank() }

        if (lines.size < 2) {
            // Single-line paste forward directly using bracketed paste mode
            terminalView.createSendTextBuilder()
                .useBracketedPasteMode()
                .send(clipboardText)
            return
        }

        // Multiline paste detected, show warning dialog before proceeding
        ApplicationManager.getApplication().invokeLater {
            val dialog = MultilinePasteDialog(lines)
            dialog.show()
            when (dialog.choice) {
                MultilinePasteDialog.Choice.PASTE_AS_PLAIN_TEXT -> terminalView.pasteAsPlainText(lines)
                MultilinePasteDialog.Choice.PASTE_ANYWAY -> terminalView.pasteAndExecute(lines)
                MultilinePasteDialog.Choice.CANCEL -> Unit
            }
        }
    }

    private fun clipboardText(): String? =
        CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor)

    /**
     * Pastes lines as plain text without triggering execution.
     *
     * Uses bracketed paste mode when the shell supports it (bash, zsh, fish).
     * Falls back to sendString via reflection for shells that don't support it (PowerShell).
     */
    private fun TerminalView.pasteAsPlainText(lines: List<String>) {
        if (isBracketedPasteModeActive()) {
            createSendTextBuilder()
                .useBracketedPasteMode()
                .send(lines.joinToString("\n"))
        } else {
            sendStringViaReflection(lines.joinToString("\n"))
        }
    }

    /**
     * Checks whether the active shell session has bracketed paste mode enabled.
     *
     * Bracketed paste mode is an ANSI terminal feature that wraps pasted text in
     * escape sequences, signaling to the shell that the input is a paste and should
     * not be executed line by line. PowerShell does not support this; bash/zsh/fish do.
     *
     * Accesses TerminalViewImpl internals via reflection since sessionModel is not
     * exposed on the TerminalView interface.
     */
    private fun TerminalView.isBracketedPasteModeActive(): Boolean = try {
        val sessionModel = javaClass.getDeclaredField("sessionModel")
            .also { it.isAccessible = true }
            .get(this)

        val terminalState = sessionModel.javaClass
            .getMethod("getTerminalState")
            .invoke(sessionModel)

        val stateValue = terminalState.javaClass
            .getMethod("getValue")
            .invoke(terminalState)

        stateValue.javaClass
            .getMethod("isBracketedPasteMode")
            .invoke(stateValue) as Boolean
    } catch (ex: Exception) {
        log.warn("Could not determine bracketed paste mode, assuming false", ex)
        false
    }

    /**
     * Sends text directly to the terminal PTY via TerminalInput.sendString().
     *
     * This bypasses the public TerminalView API and accesses TerminalInput internals
     * via reflection. This approach was discovered through reverse engineering — it
     * replicates the exact code path used by IntelliJ's own Ctrl+Shift+V (paste as
     * plain text) action, which also calls sendString under the hood.
     *
     * Multiline text joined with \n is displayed in PowerShell as a continuation
     * block (>> prompt) without triggering execution of individual lines.
     */
    private fun TerminalView.sendStringViaReflection(text: String) {
        try {
            val terminalInput = javaClass.getDeclaredField("terminalInput")
                .also { it.isAccessible = true }
                .get(this)

            terminalInput.javaClass
                .getDeclaredMethod("sendString", String::class.java)
                .also { it.isAccessible = true }
                .invoke(terminalInput, text)
        } catch (ex: Exception) {
            log.error("Failed to send text via reflection", ex)
        }
    }

    /**
     * Pastes each line as a separate command, executing them one by one.
     * Used when the user explicitly chooses "Paste anyway" in the warning dialog.
     */
    private fun TerminalView.pasteAndExecute(lines: List<String>) {
        lines.forEach { line ->
            createSendTextBuilder()
                .shouldExecute()
                .send(line)
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}