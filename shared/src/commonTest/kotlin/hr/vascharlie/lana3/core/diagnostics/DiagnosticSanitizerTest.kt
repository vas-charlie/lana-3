package hr.vascharlie.lana3.core.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals

class DiagnosticSanitizerTest {
    private val sanitizer = DiagnosticSanitizer()

    @Test
    fun redactsSensitiveAttributes() {
        val event = DiagnosticEvent(
            code = "VOICE_FAILURE",
            level = DiagnosticLevel.ERROR,
            component = "voice",
            message = "Voice action failed",
            attributes = mapOf("token" to "top-secret", "platform" to "android")
        )
        val result = sanitizer.sanitize(event)
        assertEquals("[REDACTED]", result.attributes["token"])
        assertEquals("android", result.attributes["platform"])
    }

    @Test
    fun keepsDiagnosticIdentityTraceable() {
        val event = DiagnosticEvent(
            code = "TASK_QUEUE_BLOCKED",
            level = DiagnosticLevel.WARNING,
            component = "task-arbiter",
            message = "Task queued by priority rule"
        )
        val result = sanitizer.sanitize(event)
        assertEquals("TASK_QUEUE_BLOCKED", result.code)
        assertEquals("task-arbiter", result.component)
        assertEquals(DiagnosticLevel.WARNING, result.level)
    }
}
