package hr.vascharlie.lana3.core.ride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SmartRideTranscriptNormalizerTest {
    private val normalizer: RideOfferTranscriptNormalizer =
        LabeledRideOfferTranscriptNormalizer()

    @Test
    fun parsesCroatianLabelledOfferIncludingEmptyReturn() {
        val result = normalizer.normalize(
            transcript =
                "Cijena 25,72 eura, dolazak 10 kilometara 15 minuta, " +
                    "vožnja 11 kilometara 20 minuta, " +
                    "prazni povratak 10 kilometara 15 minuta.",
            languageTag = "hr-HR",
        )

        val parsed = assertIs<RideOfferTranscriptResult.Parsed>(result)
        assertEquals(25.72, parsed.offer.priceEur)
        assertEquals(10.0, parsed.offer.pickupKm)
        assertEquals(15.0, parsed.offer.pickupMinutes)
        assertEquals(11.0, parsed.offer.tripKm)
        assertEquals(20.0, parsed.offer.tripMinutes)
        assertEquals(10.0, parsed.offer.emptyReturnKm)
        assertEquals(15.0, parsed.offer.emptyReturnMinutes)
        assertEquals(
            setOf(
                RideOfferField.PRICE_EUR,
                RideOfferField.PICKUP_KM,
                RideOfferField.TRIP_KM,
                RideOfferField.PICKUP_MINUTES,
                RideOfferField.TRIP_MINUTES,
                RideOfferField.EMPTY_RETURN_KM,
                RideOfferField.EMPTY_RETURN_MINUTES,
            ),
            parsed.recognizedFields,
        )
    }

    @Test
    fun parsesEnglishLabelledOfferWithoutInventingReturn() {
        val result = normalizer.normalize(
            transcript =
                "Fare is 18.50 EUR, pickup 2.4 km 6 minutes, " +
                    "passenger trip 8.7 km 18 minutes.",
            languageTag = "en-US",
        )

        val parsed = assertIs<RideOfferTranscriptResult.Parsed>(result)
        assertEquals(18.50, parsed.offer.priceEur)
        assertEquals(2.4, parsed.offer.pickupKm)
        assertEquals(6.0, parsed.offer.pickupMinutes)
        assertEquals(8.7, parsed.offer.tripKm)
        assertEquals(18.0, parsed.offer.tripMinutes)
        assertNull(parsed.offer.emptyReturnKm)
        assertNull(parsed.offer.emptyReturnMinutes)
        assertTrue(RideOfferField.EMPTY_RETURN_KM !in parsed.recognizedFields)
        assertTrue(RideOfferField.EMPTY_RETURN_MINUTES !in parsed.recognizedFields)
    }

    @Test
    fun partialTranscriptRemainsPartial() {
        val result = normalizer.normalize(
            transcript = "Cijena 20 eura, do putnika 3 kilometra.",
            languageTag = "hr",
        )

        val parsed = assertIs<RideOfferTranscriptResult.Parsed>(result)
        assertEquals(20.0, parsed.offer.priceEur)
        assertEquals(3.0, parsed.offer.pickupKm)
        assertNull(parsed.offer.pickupMinutes)
        assertNull(parsed.offer.tripKm)
        assertNull(parsed.offer.tripMinutes)
        assertNull(parsed.offer.emptyReturnKm)
        assertNull(parsed.offer.emptyReturnMinutes)
        assertEquals(
            setOf(
                RideOfferField.PRICE_EUR,
                RideOfferField.PICKUP_KM,
            ),
            parsed.recognizedFields,
        )
    }

    @Test
    fun returnWithOnlyDistanceIsPassedThroughAsIncompleteNotGuessed() {
        val result = normalizer.normalize(
            transcript =
                "Cijena 30 eura, dolazak 2 km 5 minuta, " +
                    "vožnja 13 km 25 minuta, povratak 10 km.",
            languageTag = "hr-HR",
        )

        val parsed = assertIs<RideOfferTranscriptResult.Parsed>(result)
        assertEquals(10.0, parsed.offer.emptyReturnKm)
        assertNull(parsed.offer.emptyReturnMinutes)
    }

    @Test
    fun unsupportedLanguageIsExplicit() {
        val result = normalizer.normalize(
            transcript = "Preis 20 Euro",
            languageTag = "de-DE",
        )

        val unsupported =
            assertIs<RideOfferTranscriptResult.UnsupportedLanguage>(result)
        assertEquals("de-DE", unsupported.languageTag)
    }

    @Test
    fun unrelatedSpeechProducesNoRecognizedData() {
        val result = normalizer.normalize(
            transcript = "Danas je lijep dan za vožnju gradom.",
            languageTag = "hr-HR",
        )

        assertIs<RideOfferTranscriptResult.NoRecognizedData>(result)
    }
}
