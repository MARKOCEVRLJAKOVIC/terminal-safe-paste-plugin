package dev.marko.safepaste.terminal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [SafePasteAction.requiresConfirmation].
 *
 * These are pure unit tests — no IntelliJ platform infrastructure needed.
 * The routing logic (whether to show the dialog or paste directly) is the
 * most critical correctness concern in this plugin, so it is extracted and
 * tested in isolation.
 */
class SafePasteActionTest {

    @Test
    fun testSingleLineTextDoesNotRequireConfirmation() {
        assertFalse(SafePasteAction.requiresConfirmation("echo hello"))
    }

    @Test
    fun testEmptyStringDoesNotRequireConfirmation() {
        assertFalse(SafePasteAction.requiresConfirmation(""))
    }

    @Test
    fun testBlankStringDoesNotRequireConfirmation() {
        assertFalse(SafePasteAction.requiresConfirmation("   "))
    }

    @Test
    fun testSingleLineWithTrailingNewlineDoesNotRequireConfirmation() {
        assertFalse(SafePasteAction.requiresConfirmation("echo hello\n"))
    }

    @Test
    fun testTextWithOnlyBlankLinesDoesNotRequireConfirmation() {
        assertFalse(SafePasteAction.requiresConfirmation("\n\n\n"))
    }

    @Test
    fun testOneNonBlankLineSurroundedByBlankLinesDoesNotRequireConfirmation() {
        assertFalse(SafePasteAction.requiresConfirmation("\necho hello\n"))
    }

    @Test
    fun testTwoNonBlankLinesRequireConfirmation() {
        assertTrue(SafePasteAction.requiresConfirmation("echo hello\necho world"))
    }

    @Test
    fun testThreeNonBlankLinesRequireConfirmation() {
        assertTrue(SafePasteAction.requiresConfirmation("cd /tmp\nls -la\nrm -rf *"))
    }

    @Test
    fun testTwoNonBlankLinesWithBlankLineBetweenThemRequireConfirmation() {
        assertTrue(SafePasteAction.requiresConfirmation("echo hello\n\necho world"))
    }

    @Test
    fun testCrlfLineEndingsAreHandledCorrectly() {
        assertTrue(SafePasteAction.requiresConfirmation("echo hello\r\necho world"))
    }

    @Test
    fun testScriptBlockWithManyLinesRequiresConfirmation() {
        val script = (1..10).joinToString("\n") { "command$it" }
        assertTrue(SafePasteAction.requiresConfirmation(script))
    }

    @Test
    fun testIndentedLinesCountAsNonBlank() {
        assertTrue(SafePasteAction.requiresConfirmation("  echo hello\n  echo world"))
    }
}