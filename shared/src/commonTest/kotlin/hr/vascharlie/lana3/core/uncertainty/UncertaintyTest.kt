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
        val subject = EvidenceValue.known(value = "Osijek", evidence = "verified source")

        assertEquals(DataCertainty.KNOWN, subject.certainty)
        assertEquals("Osijek", subject.value)
        assertFalse(subject.needsConfirmation)
    }

    @Test
    fun uncertainValueNeedsConfirmationWithoutInventingConfidence() {
        val subject = EvidenceValue.uncertain(value = "Osijek", evidence = "ambiguous input")

        assertEquals(DataCertainty.UNCERTAIN, subject.certainty)
        assertEquals("Osijek", subject.value)
        assertTrue(subject.needsConfirmation)
    }

    @Test
    fun unknownValueCarriesNoGuessedValue() {
        val subject = EvidenceValue.unknown<String>(evidence = "not provided")

        assertEquals(DataCertainty.UNKNOWN, subject.certainty)
        assertNull(subject.value)
        assertTrue(subject.needsConfirmation)
    }

    @Test
    fun knownRequiresAValue() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue<String>(value = null, certainty = DataCertainty.KNOWN)
        }
    }

    @Test
    fun unknownRejectsAValue() {
        assertFailsWith<IllegalArgumentException> {
            EvidenceValue(value = "guess", certainty = DataCertainty.UNKNOWN)
        }
    }

    @Test
    fun evidenceMetadataNeverUpgradesCertainty() {
        val subject = EvidenceValue.uncertain(value = 42, evidence = "some evidence")

        assertEquals(DataCertainty.UNCERTAIN, subject.certainty)
        assertTrue(subject.needsConfirmation)
    }
}
