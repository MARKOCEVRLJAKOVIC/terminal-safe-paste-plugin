package dev.marko.safepaste.terminal

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [PasteStrategy].
 *
 * Following IntelliJ Platform SDK guidance, mocks are avoided entirely.
 * Instead, the two pieces of logic worth testing are extracted as pure functions
 * on the companion object and verified directly:
 *
 * - [PasteStrategy.executableLines] — which lines [Execute] will actually send
 * - [PasteStrategy.selectStrategy]  — which strategy [forShell] picks
 *
 * The TerminalView interaction (actual sending) is covered by integration/manual
 * tests where a real IDE environment is available.
 */
class PasteStrategyTest {

    @Test
    fun testExecutableLinesReturnsAllNonBlankLines() {
        assertEquals(
            listOf("echo hello", "echo world"),
            PasteStrategy.executableLines("echo hello\necho world")
        )
    }

    @Test
    fun testExecutableLinesSkipsBlankLinesBetweenCommands() {
        assertEquals(
            listOf("echo hello", "echo world", "echo done"),
            PasteStrategy.executableLines("echo hello\n\necho world\n   \necho done")
        )
    }

    @Test
    fun testExecutableLinesReturnsEmptyListForBlankOnlyText() {
        assertEquals(emptyList<String>(), PasteStrategy.executableLines("\n\n   \n"))
    }

    @Test
    fun testExecutableLinesReturnsEmptyListForEmptyString() {
        assertEquals(emptyList<String>(), PasteStrategy.executableLines(""))
    }

    @Test
    fun testExecutableLinesStripsTrailingNewlineCorrectly() {
        assertEquals(listOf("echo hello"), PasteStrategy.executableLines("echo hello\n"))
    }

    @Test
    fun testExecutableLinesPreservesIndentedLines() {
        assertEquals(
            listOf("  cd /tmp", "  ls -la"),
            PasteStrategy.executableLines("  cd /tmp\n  ls -la")
        )
    }

    @Test
    fun testExecutableLinesHandlesCrlfLineEndings() {
        assertEquals(
            listOf("echo hello", "echo world"),
            PasteStrategy.executableLines("echo hello\r\necho world")
        )
    }

    @Test
    fun testExecutableLinesPreservesOriginalLineContentWithoutTrimming() {
        val line = "  git commit -m 'fix: something'  "
        assertEquals(listOf(line), PasteStrategy.executableLines(line))
    }

    @Test
    fun testSelectStrategyReturnsBracketedPasteWhenActive() {
        assertEquals(PasteStrategy.BracketedPaste, PasteStrategy.selectStrategy(isBracketedPasteActive = true))
    }

    @Test
    fun testSelectStrategyReturnsPlainTextWhenInactive() {
        assertEquals(PasteStrategy.PlainText, PasteStrategy.selectStrategy(isBracketedPasteActive = false))
    }
}