package dev.marko.safepaste.terminal

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ScrollPaneConstants

class MultilinePasteDialog(
    private val text: String
) : DialogWrapper(true) {

    enum class Choice { PASTE_AS_PLAIN_TEXT, PASTE_ANYWAY, CANCEL }

    companion object {
        /**
         * Returns the number of non-blank lines in [text] — i.e. the number of
         * commands that would actually execute. Extracted for testability so the
         * logic can be verified without instantiating [DialogWrapper].
         */
        internal fun countNonBlankLines(text: String): Int =
            text.lines().count { it.isNotBlank() }
    }

    private val nonBlankLineCount = countNonBlankLines(text)

    var choice: Choice = Choice.CANCEL
        private set

    init {
        title = "Warning: Multiline Paste"
        isModal = true
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, JBUI.scale(12)))
        panel.border = JBUI.Borders.empty(8)

        val warning = JBLabel(
            "<html>You are about to paste text that contains <b>$nonBlankLineCount non-empty lines</b>.<br>" +
                    "If pasted directly into your shell, this may result in unexpected execution of commands.<br><br>" +
                    "Clipboard contents (preview):</html>"
        )
        panel.add(warning, BorderLayout.NORTH)

        val textArea = JBTextArea(text).apply {
            isEditable = false
            rows = minOf(nonBlankLineCount, 10).coerceAtLeast(1)
            columns = 60
            // JBUI.Fonts.create always returns a non-null font (falls back internally), no ?: needed
            font = JBUI.Fonts.create("JetBrains Mono", 12)
        }
        val scrollPane = JBScrollPane(textArea).apply {
            horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            preferredSize = Dimension(JBUI.scale(500), JBUI.scale(200))
        }
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(
            buildAction("Paste as Plain Text", Choice.PASTE_AS_PLAIN_TEXT, OK_EXIT_CODE),
            buildAction("Paste Anyway (executes each line)", Choice.PASTE_ANYWAY, OK_EXIT_CODE),
            buildAction("Cancel", Choice.CANCEL, CANCEL_EXIT_CODE),
        )
    }

    private fun buildAction(label: String, result: Choice, exitCode: Int): Action {
        return object : DialogWrapperAction(label) {
            override fun doAction(e: ActionEvent) {
                choice = result
                close(exitCode)
            }
        }
    }
}