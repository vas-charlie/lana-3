package hr.vascharlie.lana3.core.understanding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CroatianNavigationUnderstandingTest {
    @Test
    fun extractsNavigationIntentAndDestinationFromExactPhrase() {
        val result = CroatianNavigationUnderstanding.understand(
            "Lana odvezi me do Tvrđe u Osijeku"
        )

        assertEquals(UnderstoodIntentKind.NAVIGATE, result.intent)
        assertEquals("Tvrđe u Osijeku", result.slots.single().value)
        assertFalse(result.slots.single().verified)
    }

    @Test
    fun grammaticalMeMiRecognitionDifferenceDoesNotDestroyIntent() {
        val result = CroatianNavigationUnderstanding.understand(
            "Lana odvezi mi do tvrde u Osijeku"
        )

        assertEquals(UnderstoodIntentKind.NAVIGATE, result.intent)
        assertEquals("tvrde u Osijeku", result.slots.single().value)
        assertFalse(result.slots.single().verified)
    }

    @Test
    fun doesNotSilentlyCorrectTvrdeToTvrdeWithDiacritic() {
        val result = CroatianNavigationUnderstanding.understand(
            "Lana odvezi me do tvrde u Osijeku"
        )

        assertEquals("tvrde u Osijeku", result.slots.single().value)
        assertFalse(result.slots.single().verified)
    }

    @Test
    fun unrelatedSentenceRemainsUnknown() {
        val result = CroatianNavigationUnderstanding.understand(
            "Lana kakvo je vrijeme u Osijeku"
        )

        assertEquals(UnderstoodIntentKind.UNKNOWN, result.intent)
        assertEquals(emptyList(), result.slots)
    }
}
