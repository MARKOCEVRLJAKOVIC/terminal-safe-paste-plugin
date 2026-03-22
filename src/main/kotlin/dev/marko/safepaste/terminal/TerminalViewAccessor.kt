package dev.marko.safepaste.terminal

import com.intellij.openapi.diagnostic.logger
import com.intellij.terminal.frontend.view.TerminalView

object TerminalViewAccessor {

    private val LOG = logger<TerminalViewAccessor>()

    fun isBracketedPasteModeActive(view: TerminalView): Boolean =
        runCatching {
            val sessionModel = view.reflectField("sessionModel")
            val terminalState = sessionModel.invokeMethod("getTerminalState")
            val stateValue = terminalState.invokeViaInterface(
                interfaceName = "kotlinx.coroutines.flow.StateFlow",
                methodName = "getValue"
            )
            stateValue.invokeMethod("isBracketedPasteMode") as Boolean
        }.getOrElse { e ->
            LOG.warn("SafePaste: could not determine bracketed paste mode via reflection", e)
            false
        }

    fun sendString(view: TerminalView, text: String) {
        runCatching {
            val terminalInput = view.reflectField("terminalInput")
            terminalInput.invokeMethod("sendString", String::class.java to text)
        }.onFailure { e ->
            LOG.warn("SafePaste: sendString via reflection failed", e)
        }
    }

    private fun Any.reflectField(name: String): Any =
        javaClass.getDeclaredField(name)
            .also { it.isAccessible = true }
            .get(this)

    private fun Any.invokeMethod(name: String, vararg args: Pair<Class<*>, Any?>): Any {
        val (types, values) = args.unzip()
        return javaClass.getDeclaredMethod(name, *types.toTypedArray())
            .also { it.isAccessible = true }
            .invoke(this, *values.toTypedArray())
    }

    private fun Any.invokeViaInterface(interfaceName: String, methodName: String): Any =
        javaClass.interfaces
            .first { it.name == interfaceName }
            .getMethod(methodName)
            .also { it.isAccessible = true }
            .invoke(this)
}