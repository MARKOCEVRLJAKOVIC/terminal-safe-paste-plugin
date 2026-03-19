package dev.marko.safepaste.terminal

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.terminal.frontend.view.TerminalView
import java.awt.datatransfer.DataFlavor

class SafePasteAction : DumbAwareAction() {

    private val log = logger<SafePasteAction>()

    override fun actionPerformed(e: AnActionEvent) {
        println("=== SafePasteAction CALLED ===")

        val terminalView = e.getData(TerminalView.DATA_KEY) ?: run {
            println("SafePaste: DATA_KEY returned null")
            log.warn("SafePasteAction triggered but TerminalView data key returned null")
            return
        }

        val clipboardText = CopyPasteManager.getInstance()
            .getContents<String>(DataFlavor.stringFlavor) ?: run {
            log.debug("Clipboard is empty or does not contain text")
            return
        }

        val lines = clipboardText
            .lines()
            .filter { it.isNotBlank() }

        if (lines.size < 2) {
            println("=== Single line paste, no interception needed ===")
            terminalView.createSendTextBuilder()
                .useBracketedPasteMode()
                .send(clipboardText)
            return
        }

        log.debug("Multiline paste detected: ${lines.size} non-blank lines")

        // TODO: show inline warning panel with `lines` and `terminalView`
    }

    override fun update(e: AnActionEvent) {
        println("=== SafePasteAction UPDATE CALLED ===")
        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

}