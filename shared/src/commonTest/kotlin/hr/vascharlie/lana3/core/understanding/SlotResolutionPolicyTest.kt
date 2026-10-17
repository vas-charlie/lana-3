package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SlotResolutionPolicyTest {
    private val heardDestination = UnderstoodSlot(
        name = "destination",
        value = "tvrde u Osijeku",
        verified = false,
    )

    @Test
    fun uniquelyResolvedPlaceCanBecomeActionable() {
        val result = SlotResolutionPolicy.assess(
            SlotResolution.Resolved(
                original = heardDestination,
                canonicalValue = "Tvrđa, Osijek",
            )
        )

        assertEquals(SlotActionability.READY, result.actionability)
        assertEquals("Tvrđa, Osijek", result.value)
    }

    @Test
    fun ambiguousPlaceRequiresClarification() {
        val result = SlotResolutionPolicy.assess(
            SlotResolution.Ambiguous(
                original = heardDestination,
                candidates = listOf("Tvrđa, Osijek", "Tvrda, drugo mjesto"),
            )
        )

        assertEquals(SlotActionability.CLARIFY, result.actionability)
        assertNull(result.value)
    }

    @Test
    fun unresolvedPlaceRequiresClarificationInsteadOfGuessing() {
        val result = SlotResolutionPolicy.assess(
            SlotResolution.NotFound(heardDestination)
        )

        assertEquals(SlotActionability.CLARIFY, result.actionability)
        assertNull(result.value)
    }

    @Test
    fun emptyCanonicalValueCannotBeTreatedAsResolved() {
        val result = SlotResolutionPolicy.assess(
            SlotResolution.Resolved(
                original = heardDestination,
                canonicalValue = "   ",
            )
        )

        assertEquals(SlotActionability.CLARIFY, result.actionability)
        assertNull(result.value)
    }
}
