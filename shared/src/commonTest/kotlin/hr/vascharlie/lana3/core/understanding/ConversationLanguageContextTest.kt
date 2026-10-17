package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConversationLanguageContextPolicyTest {
    @Test
    fun passengerLanguageDoesNotOverwriteCharliesLanguage() {
        val start = ConversationLanguageContext(charlieLanguageTag = "hr-HR")

        val updated = ConversationLanguageContextPolicy.update(
            context = start,
            turn = UnderstandingTurn(
                speakerRole = ConversationSpeakerRole.OTHER_PERSON,
                transcript = "Guten Abend",
                detectedLanguageTag = "de-DE",
            ),
        )

        assertEquals("hr-HR", updated.charlieLanguageTag)
        assertEquals("de-DE", updated.lastOtherPersonLanguageTag)
    }

    @Test
    fun unknownSpeakerDoesNotInventConversationRoles() {
        val start = ConversationLanguageContext(charlieLanguageTag = "hr-HR")

        val updated = ConversationLanguageContextPolicy.update(
            context = start,
            turn = UnderstandingTurn(
                speakerRole = ConversationSpeakerRole.UNKNOWN,
                transcript = "Hello",
                detectedLanguageTag = "en-US",
            ),
        )

        assertEquals(start, updated)
        assertNull(updated.lastOtherPersonLanguageTag)
    }

    @Test
    fun missingPassengerDetectionKeepsLastKnownPassengerLanguage() {
        val start = ConversationLanguageContext(
            charlieLanguageTag = "hr-HR",
            lastOtherPersonLanguageTag = "it-IT",
        )

        val updated = ConversationLanguageContextPolicy.update(
            context = start,
            turn = UnderstandingTurn(
                speakerRole = ConversationSpeakerRole.OTHER_PERSON,
                transcript = "Grazie",
                detectedLanguageTag = null,
            ),
        )

        assertEquals("it-IT", updated.lastOtherPersonLanguageTag)
    }

    @Test
    fun explicitCharlieDetectionCanUpdateCharliesLanguageForThatContext() {
        val start = ConversationLanguageContext(charlieLanguageTag = "hr-HR")

        val updated = ConversationLanguageContextPolicy.update(
            context = start,
            turn = UnderstandingTurn(
                speakerRole = ConversationSpeakerRole.CHARLIE,
                transcript = "Let's continue in English",
                detectedLanguageTag = "en-GB",
            ),
        )

        assertEquals("en-GB", updated.charlieLanguageTag)
    }
}
