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
    private val lines: List<String>
) : DialogWrapper(true) {

    enum class Choice { PASTE_AS_PLAIN_TEXT, PASTE_ANYWAY, CANCEL }

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
            "<html>You are about to paste text that contains <b>${lines.size} lines</b>.<br>" +
                    "If pasted directly into your shell, this may result in unexpected execution of commands.<br><br>" +
                    "Clipboard contents (preview):</html>"
        )
        panel.add(warning, BorderLayout.NORTH)

        val previewText = lines.joinToString("\n")
        val textArea = JBTextArea(previewText).apply {
            isEditable = false
            rows = minOf(lines.size, 10).coerceAtLeast(1)
            columns = 60
            font = JBUI.Fonts.create("JetBrains Mono", 12) ?: font.deriveFont(12f)
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
            buildAction("Paste as Plain Text", Choice.PASTE_AS_PLAIN_TEXT),
            buildAction("Paste anyway", Choice.PASTE_ANYWAY),
            buildAction("Cancel", Choice.CANCEL),
        )
    }

    private fun buildAction(label: String, result: Choice): Action {
        return object : DialogWrapperAction(label) {
            override fun doAction(e: ActionEvent) {
                choice = result
                close(CANCEL_EXIT_CODE)
            }
        }
    }
}