package hr.vascharlie.lana3.core.uncertainty

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvidenceValueTest {
    @Test
    fun knownCarriesValueWithoutConfirmation() {
        val value = EvidenceValue.known(42)
        assertEquals(42, value.value)
        assertFalse(value.needsConfirmation)
    }

    @Test
    fun uncertainRequiresConfirmation() {
        val candidate = EvidenceValue.uncertain("candidate", evidence = "partial")
        assertEquals(DataCertainty.UNCERTAIN, candidate.certainty)
        assertTrue(candidate.needsConfirmation)
    }

    @Test
    fun unknownHasNoValueAndNeedsConfirmation() {
        val value = EvidenceValue.unknown<String>()
        assertNull(value.value)
        assertTrue(value.needsConfirmation)
    }

    @Test
    fun knownCannotHaveNullValue() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue<String>(null, DataCertainty.KNOWN)
        }
    }

    @Test
    fun unknownCannotContainGuessedValue() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue("guess", DataCertainty.UNKNOWN)
        }
    }
}
