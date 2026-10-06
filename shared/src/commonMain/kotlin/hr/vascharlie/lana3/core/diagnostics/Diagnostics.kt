package hr.vascharlie.lana3.core.diagnostics

enum class DiagnosticLevel { INFO, WARNING, ERROR }

enum class DiagnosticOutcome {
    SUCCESS,
    FAILURE,
    BLOCKED,
    DEGRADED,
    UNKNOWN,
}

data class DiagnosticEvent(
    val code: String,
    val level: DiagnosticLevel,
    val component: String,
    val message: String,
    val attributes: Map<String, String> = emptyMap(),
    val traceId: String? = null,
    val outcome: DiagnosticOutcome = DiagnosticOutcome.UNKNOWN,
    val durationMillis: Long? = null,
    val occurredAtEpochMillis: Long? = null,
)

data class SanitizedDiagnosticEvent(
    val code: String,
    val level: DiagnosticLevel,
    val component: String,
    val message: String,
    val attributes: Map<String, String>,
    val traceId: String?,
    val outcome: DiagnosticOutcome,
    val durationMillis: Long?,
    val occurredAtEpochMillis: Long?,
)

class DiagnosticSanitizer(
    private val sensitiveKeyFragments: Set<String> = setOf(
        "password",
        "token",
        "secret",
        "authorization",
        "api_key",
        "apikey",
        "phone",
        "email",
        "message_body",
        "precise_location",
    )
) {
    fun sanitize(event: DiagnosticEvent): SanitizedDiagnosticEvent {
        val cleaned = event.attributes.mapValues { (key, value) ->
            if (isSensitiveKey(key)) "[REDACTED]" else value
        }
        return SanitizedDiagnosticEvent(
            code = event.code,
            level = event.level,
            component = event.component,
            message = event.message,
            attributes = cleaned,
            traceId = event.traceId,
            outcome = event.outcome,
            durationMillis = event.durationMillis,
            occurredAtEpochMillis = event.occurredAtEpochMillis,
        )
    }

    private fun isSensitiveKey(key: String): Boolean {
        val normalized = key.lowercase()
        return sensitiveKeyFragments.any { fragment ->
            normalized.contains(fragment)
        }
    }
}
