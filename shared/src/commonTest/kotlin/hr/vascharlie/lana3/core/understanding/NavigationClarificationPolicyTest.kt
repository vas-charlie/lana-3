package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NavigationClarificationPolicyTest {
    private val destination = UnderstoodSlot(
        name = "destination",
        value = "tvrde u Osijeku",
        verified = false,
    )

    @Test
    fun notFoundAsksToConfirmTheSpecificDestination() {
        val request = NavigationClarificationPolicy.from(
            SlotResolution.NotFound(destination)
        )
        requireNotNull(request)
        assertEquals(ClarificationKind.CONFIRM_DESTINATION, request.kind)
        assertEquals("tvrde u Osijeku", request.subject)
    }

    @Test
    fun ambiguityOffersTheActualCandidates() {
        val request = NavigationClarificationPolicy.from(
            SlotResolution.Ambiguous(
                original = destination,
                candidates = listOf("Tvrđa, Osijek", "Tvrda, drugo mjesto"),
            )
        )
        requireNotNull(request)
        assertEquals(ClarificationKind.CHOOSE_DESTINATION, request.kind)
        assertEquals(listOf("Tvrđa, Osijek", "Tvrda, drugo mjesto"), request.candidates)
    }

    @Test
    fun duplicateAndBlankCandidatesAreNotRepeatedToUser() {
        val request = NavigationClarificationPolicy.from(
            SlotResolution.Ambiguous(
                original = destination,
                candidates = listOf("Tvrđa, Osijek", " ", "Tvrđa, Osijek"),
            )
        )
        requireNotNull(request)
        assertEquals(listOf("Tvrđa, Osijek"), request.candidates)
    }

    @Test
    fun resolvedDestinationNeedsNoClarification() {
        val request = NavigationClarificationPolicy.from(
            SlotResolution.Resolved(destination, "Tvrđa, Osijek")
        )
        assertNull(request)
    }

    @Test
    fun unusableTranscriptAsksForRepeatInsteadOfGuessing() {
        val request = NavigationClarificationPolicy.from(
            ClarificationReason.NO_USABLE_TRANSCRIPT
        )
        assertEquals(ClarificationKind.REPEAT_UTTERANCE, request.kind)
    }
}
