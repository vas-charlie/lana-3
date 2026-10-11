package hr.vascharlie.lana3.core.uncertainty

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EvidenceValueTest {
    @Test
    fun uncertainRequiresConfirmation() {
        val candidate = EvidenceValue.uncertain("candidate")
        assertEquals(DataCertainty.UNCERTAIN, candidate.certainty)
        assertTrue(candidate.needsConfirmation)
    }

    @Test
    fun unknownCannotContainGuessedValue() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue("guess", DataCertainty.UNKNOWN)
        }
    }
}
