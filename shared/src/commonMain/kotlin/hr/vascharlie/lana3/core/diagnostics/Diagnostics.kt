package hr.vascharlie.lana3.core.diagnostics

enum class DiagnosticLevel { INFO, WARNING, ERROR }

data class DiagnosticEvent(
    val code: String,
    val level: DiagnosticLevel,
    val component: String,
    val message: String,
    val attributes: Map<String, String> = emptyMap()
)

data class SanitizedDiagnosticEvent(
    val code: String,
    val level: DiagnosticLevel,
    val component: String,
    val message: String,
    val attributes: Map<String, String>
)

class DiagnosticSanitizer(
    private val sensitiveKeys: Set<String> = setOf(
        "password", "token", "secret", "authorization", "api_key",
        "phone", "email", "message_body", "precise_location"
    )
) {
    fun sanitize(event: DiagnosticEvent): SanitizedDiagnosticEvent {
        val cleaned = event.attributes.mapValues { (key, value) ->
            if (key.lowercase() in sensitiveKeys) "[REDACTED]" else value
        }
        return SanitizedDiagnosticEvent(
            code = event.code,
            level = event.level,
            component = event.component,
            message = event.message,
            attributes = cleaned
        )
    }
}
