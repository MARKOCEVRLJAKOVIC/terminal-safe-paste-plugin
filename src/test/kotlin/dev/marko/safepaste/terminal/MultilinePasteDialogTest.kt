package dev.marko.safepaste.terminal

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [MultilinePasteDialog].
 *
 * Line-counting logic is tested via [MultilinePasteDialog.countNonBlankLines],
 * a companion object function. This avoids instantiating [DialogWrapper] entirely,
 * which would require a Swing/IntelliJ environment unavailable in headless unit tests.
 *
 * The Choice enum default (CANCEL) is tested directly on the enum — no dialog
 * instance needed.
 */
class MultilinePasteDialogTest {

    private fun lineCount(text: String) = MultilinePasteDialog.countNonBlankLines(text)

    @Test
    fun testCountEmptyText() {
        assertEquals(0, lineCount(""))
    }

    @Test
    fun testCountWhitespaceOnlyText() {
        assertEquals(0, lineCount("   \n   \n   "))
    }

    @Test
    fun testCountSingleNonBlankLine() {
        assertEquals(1, lineCount("echo hello"))
    }

    @Test
    fun testCountTwoNonBlankLines() {
        assertEquals(2, lineCount("echo hello\necho world"))
    }

    @Test
    fun testCountExcludingBlankLinesBetweenCommands() {
        assertEquals(2, lineCount("echo hello\n\n\necho world"))
    }

    @Test
    fun testCountExcludingTrailingNewline() {
        assertEquals(1, lineCount("echo hello\n"))
    }

    @Test
    fun testCountExcludingLeadingAndTrailingBlankLines() {
        assertEquals(1, lineCount("\n\necho hello\n\n"))
    }

    @Test
    fun testCountIndentedLinesAsNonBlank() {
        assertEquals(2, lineCount("  cd /tmp\n  ls -la"))
    }

    @Test
    fun testCountTenCommands() {
        val text = (1..10).joinToString("\n") { "cmd$it" }
        assertEquals(10, lineCount(text))
    }

    @Test
    fun testCountCrlfLineEndingsCorrectly() {
        assertEquals(2, lineCount("echo hello\r\necho world"))
    }

    @Test
    fun testVerifyCancelIsDistinctFromPasteChoices() {
        val cancel = MultilinePasteDialog.Choice.CANCEL
        assert(cancel != MultilinePasteDialog.Choice.PASTE_AS_PLAIN_TEXT)
        assert(cancel != MultilinePasteDialog.Choice.PASTE_ANYWAY)
    }

    @Test
    fun testVerifyChoiceEnumSize() {
        assertEquals(3, MultilinePasteDialog.Choice.entries.size)
    }
}