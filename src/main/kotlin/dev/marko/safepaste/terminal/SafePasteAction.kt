package dev.marko.safepaste.terminal

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
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
 *
 * Note: Relies on [TerminalViewAccessor] which uses reflection against internal IDE APIs.
 * This may break on future IDE updates — failures are logged as warnings and fall back
 * to safe defaults.
 */
class SafePasteAction : DumbAwareAction() {

    companion object {
        private val LOG = logger<SafePasteAction>()

        /**
         * Returns true if the given text contains two or more non-blank lines,
         * meaning it requires user confirmation before pasting.
         * Extracted for testability.
         */
        internal fun requiresConfirmation(text: String): Boolean =
            text.lines().count { it.isNotBlank() } >= 2
    }

    override fun actionPerformed(e: AnActionEvent) {
        val view = e.getData(TerminalView.DATA_KEY) ?: run {
            LOG.warn("SafePaste: TerminalView not available in current context")
            return
        }
        val text = clipboardText() ?: return

        if (!requiresConfirmation(text)) {
            PasteStrategy.BracketedPaste.execute(view, text)
            return
        }

        val dialog = MultilinePasteDialog(text).also { it.show() }
        when (dialog.choice) {
            MultilinePasteDialog.Choice.PASTE_AS_PLAIN_TEXT -> PasteStrategy.forShell(view).execute(view, text)
            MultilinePasteDialog.Choice.PASTE_ANYWAY        -> PasteStrategy.Execute.execute(view, text)
            MultilinePasteDialog.Choice.CANCEL              -> Unit
        }
    }

    private fun clipboardText(): String? =
        CopyPasteManager.getInstance().getContents(DataFlavor.stringFlavor)

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT
}
