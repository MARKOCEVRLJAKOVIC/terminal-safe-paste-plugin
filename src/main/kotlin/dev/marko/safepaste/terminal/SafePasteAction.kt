package dev.marko.safepaste.terminal

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
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
            // NOTE: TerminalView.sendText is experimental API (com.intellij.terminal.frontend.view)
            terminalView.createSendTextBuilder()
                .useBracketedPasteMode()
                .send(clipboardText)
            return
        }

        log.debug("Multiline paste detected: ${lines.size} non-blank lines")

        ApplicationManager.getApplication().invokeLater {
            val dialog = MultilinePasteDialog(lines)
            dialog.show()

            when (dialog.choice) {
                MultilinePasteDialog.Choice.PASTE_AS_PLAIN_TEXT -> {

                    // TODO: Change Temporary Hardcoded PASTE_AS_PLAIN_TEXT

                    val singleLine = clipboardText
                        .replace("\r\n", " `\n")
                        .replace("\n\r", " `\n")
                        .replace("\n\r", " `\n")
                        .replace("\r", " `\n")
                        .replace("\n", " `\n")
                        .replace("`r`n", " `\n")
                        .replace("`n`r", " `\n")
                        .replace("`n", " `\n")
                        .replace("`r", " `\n")
                        .trim()

                    terminalView.createSendTextBuilder()
                        .send(singleLine)


                }

                MultilinePasteDialog.Choice.PASTE_ANYWAY -> {

                    val builder = terminalView.createSendTextBuilder()
                    lines.forEach { line ->
                        builder
                            .shouldExecute()
                            .send(line)
                    }
                }
                MultilinePasteDialog.Choice.CANCEL -> {
                    // skipping
                }
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.EDT

}