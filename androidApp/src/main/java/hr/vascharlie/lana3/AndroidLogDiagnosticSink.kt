package hr.vascharlie.lana3

import android.util.Log
import hr.vascharlie.lana3.core.diagnostics.DiagnosticEvent
import hr.vascharlie.lana3.core.diagnostics.DiagnosticSanitizer
import hr.vascharlie.lana3.core.diagnostics.DiagnosticSink

class AndroidLogDiagnosticSink(
    private val sanitizer: DiagnosticSanitizer = DiagnosticSanitizer(),
) : DiagnosticSink {
    override fun record(event: DiagnosticEvent) {
        val safe = sanitizer.sanitize(event)
        val attributes = safe.attributes
            .entries
            .sortedBy { it.key }
            .joinToString(separator = ", ") { (key, value) -> "$key=$value" }

        val message = buildString {
            append(safe.code)
            append(": ")
            append(safe.message)
            if (attributes.isNotBlank()) {
                append(" [")
                append(attributes)
                append("]")
            }
        }

        when (safe.level) {
            hr.vascharlie.lana3.core.diagnostics.DiagnosticLevel.INFO ->
                Log.i("LANA3", message)

            hr.vascharlie.lana3.core.diagnostics.DiagnosticLevel.WARNING ->
                Log.w("LANA3", message)

            hr.vascharlie.lana3.core.diagnostics.DiagnosticLevel.ERROR ->
                Log.e("LANA3", message)
        }
    }
}
