package dev.marko.safepaste.terminal

import com.intellij.terminal.frontend.view.TerminalView

sealed interface PasteStrategy {

    fun execute(view: TerminalView, text: String)

    data object BracketedPaste : PasteStrategy {
        override fun execute(view: TerminalView, text: String) {
            view.createSendTextBuilder()
                .useBracketedPasteMode()
                .send(text)
        }
    }

    data object PlainText : PasteStrategy {
        override fun execute(view: TerminalView, text: String) {
            TerminalViewAccessor.sendString(view, text)
        }
    }

    data object Execute : PasteStrategy {
        override fun execute(view: TerminalView, text: String) {
            text.lines()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    view.createSendTextBuilder()
                        .shouldExecute()
                        .send(line)
                }
        }
    }

    companion object {
        fun forShell(view: TerminalView): PasteStrategy =
            if (TerminalViewAccessor.isBracketedPasteModeActive(view)) BracketedPaste
            else PlainText
    }
}