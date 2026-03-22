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
            executableLines(text).forEach { line ->
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

        /**
         * Returns the lines that [Execute] will actually send to the terminal —
         * i.e. non-blank lines only. Extracted for testability: this pure function
         * can be verified without any IntelliJ platform infrastructure.
         */
        internal fun executableLines(text: String): List<String> =
            text.lines().filter { it.isNotBlank() }

        /**
         * Pure strategy-selection logic, decoupled from [TerminalView].
         * Extracted so the branching can be tested without platform infrastructure.
         *
         * [isBracketedPasteActive] mirrors [TerminalViewAccessor.isBracketedPasteModeActive].
         */
        internal fun selectStrategy(isBracketedPasteActive: Boolean): PasteStrategy =
            if (isBracketedPasteActive) BracketedPaste else PlainText
    }
}