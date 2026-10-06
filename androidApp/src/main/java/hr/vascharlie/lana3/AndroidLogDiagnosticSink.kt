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
        val metadata = buildList {
            safe.traceId?.let { add("trace=$it") }
            if (safe.outcome.name != "UNKNOWN") {
                add("outcome=" + safe.outcome.name)
            }
            safe.durationMillis?.let { add("durationMs=$it") }
            safe.occurredAtEpochMillis?.let { add("occurredAt=$it") }

            safe.attributes
                .entries
                .sortedBy { it.key }
                .forEach { (key, value) ->
                    add("$key=$value")
                }
        }.joinToString(separator = ", ")

        val message = buildString {
            append(safe.code)
            append(": ")
            append(safe.message)
            if (metadata.isNotBlank()) {
                append(" [")
                append(metadata)
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
