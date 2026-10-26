package hr.vascharlie.lana3.core.uncertainty

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvidenceValueTest {
    @Test
    fun knownValueDoesNotNeedConfirmation() {
        val value = EvidenceValue.known(42, evidence = "verified input")
        assertEquals(42, value.value)
        assertEquals(DataCertainty.KNOWN, value.certainty)
        assertFalse(value.needsConfirmation)
    }

    @Test
    fun uncertainValueAlwaysNeedsConfirmation() {
        val value = EvidenceValue.uncertain("Hotel Kolovare")
        assertEquals("Hotel Kolovare", value.value)
        assertTrue(value.needsConfirmation)
    }

    @Test
    fun unknownValueCannotCarryAGuess() {
        val value = EvidenceValue.unknown<String>("destination missing")
        assertNull(value.value)
        assertEquals(DataCertainty.UNKNOWN, value.certainty)
        assertTrue(value.needsConfirmation)
    }

    @Test
    fun knownWithoutValueIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue<String>(value = null, certainty = DataCertainty.KNOWN)
        }
    }

    @Test
    fun unknownWithValueIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue(value = "guessed", certainty = DataCertainty.UNKNOWN)
        }
    }
}
