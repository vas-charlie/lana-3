package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UnderstandingLanguagePolicyTest {
    @Test
    fun requestedLanguageAloneIsNotDetectionEvidence() {
        val result = UnderstandingLanguagePolicy.assess(
            UnderstandingLanguageEvidence(requestedLanguageTag = "hr-HR")
        )

        assertEquals(UnderstandingLanguageStatus.NOT_DETECTED, result.status)
        assertEquals("hr-HR", result.requestedLanguageTag)
        assertNull(result.detectedLanguageTag)
    }

    @Test
    fun samePrimaryLanguageWithDifferentRegionIsConsistent() {
        val result = UnderstandingLanguagePolicy.assess(
            UnderstandingLanguageEvidence(
                requestedLanguageTag = "en-US",
                detectedLanguageTag = "en-GB",
            )
        )

        assertEquals(UnderstandingLanguageStatus.DETECTED, result.status)
        assertEquals("en-GB", result.detectedLanguageTag)
    }

    @Test
    fun differentDetectedLanguageIsPreservedInsteadOfOverwritten() {
        val result = UnderstandingLanguagePolicy.assess(
            UnderstandingLanguageEvidence(
                requestedLanguageTag = "hr-HR",
                detectedLanguageTag = "de-DE",
            )
        )

        assertEquals(
            UnderstandingLanguageStatus.REQUEST_DIFFERS_FROM_DETECTION,
            result.status,
        )
        assertEquals("hr-HR", result.requestedLanguageTag)
        assertEquals("de-DE", result.detectedLanguageTag)
    }

    @Test
    fun detectionWithoutRequestedLanguageRemainsValidEvidence() {
        val result = UnderstandingLanguagePolicy.assess(
            UnderstandingLanguageEvidence(detectedLanguageTag = "it-IT")
        )

        assertEquals(UnderstandingLanguageStatus.DETECTED, result.status)
        assertEquals("it-IT", result.detectedLanguageTag)
    }
}
