package hr.vascharlie.lana3.ports

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpeechDiagnosticSamplesTest {
    @Test
    fun croatianPronunciationSampleRemainsStableForVoiceComparison() {
        assertEquals("hr-HR", SpeechDiagnosticSamples.CROATIAN_LANGUAGE_TAG)
        assertEquals(
            "Molim te, pogledaj me i reci mi razumiješ li me. " +
                "Čekam te kod Tvrđe. " +
                "Ova je daska tvrđa, ali treća je najtvrđa. " +
                "Tvrđavica je odmah iza ugla.",
            SpeechDiagnosticSamples.CROATIAN_PRONUNCIATION,
        )
    }

    @Test
    fun sampleContainsBothCriticalPronunciationContrasts() {
        val sample = SpeechDiagnosticSamples.CROATIAN_PRONUNCIATION
        assertTrue(Regex("\\bme\\b").containsMatchIn(sample))
        assertTrue(Regex("\\bmi\\b").containsMatchIn(sample))
        for (word in listOf("Tvrđe", "tvrđa", "najtvrđa", "Tvrđavica")) {
            assertTrue(sample.contains(word), "Missing Croatian diagnostic word: $word")
        }
    }
}
