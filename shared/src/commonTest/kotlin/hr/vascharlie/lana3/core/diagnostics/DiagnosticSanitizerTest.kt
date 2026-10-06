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
            attributes = mapOf(
                "token" to "top-secret",
                "platform" to "android",
            )
        )
        val result = sanitizer.sanitize(event)
        assertEquals("[REDACTED]", result.attributes["token"])
        assertEquals("android", result.attributes["platform"])
    }

    @Test
    fun redactsSensitiveFragmentsInsideRealisticAttributeNames() {
        val event = DiagnosticEvent(
            code = "AUTH_FAILURE",
            level = DiagnosticLevel.ERROR,
            component = "auth",
            message = "Authentication failed",
            attributes = mapOf(
                "access_token" to "abc",
                "refreshToken" to "def",
                "user_email" to "x@example.test",
                "customerPhoneNumber" to "+385000000",
                "apiKeyValue" to "secret-value",
                "platform_version" to "Android 16",
            )
        )

        val result = sanitizer.sanitize(event)

        assertEquals("[REDACTED]", result.attributes["access_token"])
        assertEquals("[REDACTED]", result.attributes["refreshToken"])
        assertEquals("[REDACTED]", result.attributes["user_email"])
        assertEquals("[REDACTED]", result.attributes["customerPhoneNumber"])
        assertEquals("[REDACTED]", result.attributes["apiKeyValue"])
        assertEquals("Android 16", result.attributes["platform_version"])
    }

    @Test
    fun mixedCaseSensitiveKeysAreStillRedacted() {
        val event = DiagnosticEvent(
            code = "TEST",
            level = DiagnosticLevel.INFO,
            component = "test",
            message = "test",
            attributes = mapOf(
                "AuthorizationHeader" to "Bearer secret",
                "Precise_Location_LatLng" to "45.0,15.0",
            )
        )

        val result = sanitizer.sanitize(event)

        assertEquals("[REDACTED]", result.attributes["AuthorizationHeader"])
        assertEquals("[REDACTED]", result.attributes["Precise_Location_LatLng"])
    }

    @Test
    fun keepsDiagnosticIdentityTraceable() {
        val event = DiagnosticEvent(
            code = "TASK_QUEUE_BLOCKED",
            level = DiagnosticLevel.WARNING,
            component = "task-arbiter",
            message = "Task queued by priority rule",
            traceId = "trace-123",
            outcome = DiagnosticOutcome.BLOCKED,
            durationMillis = 42L,
            occurredAtEpochMillis = 1_000L,
        )

        val result = sanitizer.sanitize(event)

        assertEquals("TASK_QUEUE_BLOCKED", result.code)
        assertEquals("task-arbiter", result.component)
        assertEquals(DiagnosticLevel.WARNING, result.level)
        assertEquals("trace-123", result.traceId)
        assertEquals(DiagnosticOutcome.BLOCKED, result.outcome)
        assertEquals(42L, result.durationMillis)
        assertEquals(1_000L, result.occurredAtEpochMillis)
    }

    @Test
    fun structuredFieldsAreOptionalForExistingEvents() {
        val event = DiagnosticEvent(
            code = "LEGACY_EVENT",
            level = DiagnosticLevel.INFO,
            component = "test",
            message = "Existing event",
        )

        val result = sanitizer.sanitize(event)

        assertEquals(null, result.traceId)
        assertEquals(DiagnosticOutcome.UNKNOWN, result.outcome)
        assertEquals(null, result.durationMillis)
        assertEquals(null, result.occurredAtEpochMillis)
    }
}
