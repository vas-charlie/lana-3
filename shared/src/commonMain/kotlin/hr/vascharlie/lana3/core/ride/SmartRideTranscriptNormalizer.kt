package hr.vascharlie.lana3.core.ride

enum class RideOfferField {
    PRICE_EUR,
    PICKUP_KM,
    TRIP_KM,
    PICKUP_MINUTES,
    TRIP_MINUTES,
    EMPTY_RETURN_KM,
    EMPTY_RETURN_MINUTES,
}

sealed interface RideOfferTranscriptResult {
    data class Parsed(
        val offer: RideOffer,
        val recognizedFields: Set<RideOfferField>,
    ) : RideOfferTranscriptResult

    data class UnsupportedLanguage(
        val languageTag: String?,
    ) : RideOfferTranscriptResult

    data object NoRecognizedData : RideOfferTranscriptResult
}

interface RideOfferTranscriptNormalizer {
    fun normalize(
        transcript: String,
        languageTag: String?,
    ): RideOfferTranscriptResult
}

/**
 * Small, deterministic, offline-first transcript normalizer for the first Smart Ride
 * voice slice.
 *
 * It intentionally understands only explicitly labelled offer data and currently has
 * verified lexicons for Croatian and English. It does not try to infer destinations,
 * route lengths, return probability or missing numbers. A future AI-backed extractor can
 * implement the same [RideOfferTranscriptNormalizer] boundary without changing Smart Ride.
 */
class LabeledRideOfferTranscriptNormalizer : RideOfferTranscriptNormalizer {
    override fun normalize(
        transcript: String,
        languageTag: String?,
    ): RideOfferTranscriptResult {
        val lexicon = lexiconFor(languageTag)
            ?: return RideOfferTranscriptResult.UnsupportedLanguage(languageTag)

        val normalized = transcript
            .trim()
            .lowercase()
            .replace(',', '.')
            .replace(Regex("\\s+"), " ")

        if (normalized.isBlank()) {
            return RideOfferTranscriptResult.NoRecognizedData
        }

        val price = extractPrice(normalized, lexicon)
        val sections = findSections(normalized, lexicon)

        val pickup = sections[SectionKind.PICKUP]
        val trip = sections[SectionKind.TRIP]
        val emptyReturn = sections[SectionKind.EMPTY_RETURN]

        val pickupKm = pickup?.let(::extractKilometers)
        val pickupMinutes = pickup?.let(::extractMinutes)
        val tripKm = trip?.let(::extractKilometers)
        val tripMinutes = trip?.let(::extractMinutes)
        val returnKm = emptyReturn?.let(::extractKilometers)
        val returnMinutes = emptyReturn?.let(::extractMinutes)

        val fields = buildSet {
            if (price != null) add(RideOfferField.PRICE_EUR)
            if (pickupKm != null) add(RideOfferField.PICKUP_KM)
            if (tripKm != null) add(RideOfferField.TRIP_KM)
            if (pickupMinutes != null) add(RideOfferField.PICKUP_MINUTES)
            if (tripMinutes != null) add(RideOfferField.TRIP_MINUTES)
            if (returnKm != null) add(RideOfferField.EMPTY_RETURN_KM)
            if (returnMinutes != null) add(RideOfferField.EMPTY_RETURN_MINUTES)
        }

        if (fields.isEmpty()) {
            return RideOfferTranscriptResult.NoRecognizedData
        }

        return RideOfferTranscriptResult.Parsed(
            offer = RideOffer(
                priceEur = price,
                pickupKm = pickupKm,
                tripKm = tripKm,
                pickupMinutes = pickupMinutes,
                tripMinutes = tripMinutes,
                emptyReturnKm = returnKm,
                emptyReturnMinutes = returnMinutes,
            ),
            recognizedFields = fields,
        )
    }

    private fun extractPrice(
        text: String,
        lexicon: Lexicon,
    ): Double? {
        val labelled = Regex(
            "(?:${lexicon.priceLabelPattern})\\s*(?:${lexicon.priceJoinerPattern})?\\s*" +
                "($NUMBER_PATTERN)\\s*(?:${lexicon.currencyPattern})?",
            RegexOption.IGNORE_CASE,
        ).find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()

        if (labelled != null) return labelled

        return Regex(
            "($NUMBER_PATTERN)\\s*(?:${lexicon.currencyPattern})",
            RegexOption.IGNORE_CASE,
        ).find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
    }

    private fun findSections(
        text: String,
        lexicon: Lexicon,
    ): Map<SectionKind, String> {
        val markers = buildList {
            lexicon.sectionPatterns.forEach { section ->
                Regex(
                    section.pattern,
                    RegexOption.IGNORE_CASE,
                ).findAll(text).forEach { match ->
                    add(
                        SectionMarker(
                            kind = section.kind,
                            start = match.range.first,
                            endExclusive = match.range.last + 1,
                        )
                    )
                }
            }
        }.sortedBy { it.start }

        if (markers.isEmpty()) return emptyMap()

        return buildMap {
            markers.forEachIndexed { index, marker ->
                if (containsKey(marker.kind)) return@forEachIndexed

                val nextStart = markers
                    .drop(index + 1)
                    .firstOrNull { it.start >= marker.endExclusive }
                    ?.start
                    ?: text.length

                val body = text
                    .substring(marker.endExclusive, nextStart)
                    .trim()

                put(marker.kind, body)
            }
        }
    }

    private fun extractKilometers(section: String): Double? =
        Regex(
            "($NUMBER_PATTERN)\\s*(?:km\\b|kilomet(?:ar|ra|ara|ri)\\b|" +
                "kilometers?\\b|kilometres?\\b)",
            RegexOption.IGNORE_CASE,
        ).find(section)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()

    private fun extractMinutes(section: String): Double? =
        Regex(
            "($NUMBER_PATTERN)\\s*(?:min\\.?\\b|minuta\\b|minutu\\b|" +
                "minute\\b|minuti\\b|minutes?\\b)",
            RegexOption.IGNORE_CASE,
        ).find(section)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()

    private fun lexiconFor(languageTag: String?): Lexicon? {
        val base = languageTag
            ?.trim()
            ?.lowercase()
            ?.substringBefore('-')
            ?.substringBefore('_')
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return when (base) {
            "hr" -> CROATIAN
            "en" -> ENGLISH
            else -> null
        }
    }

    private enum class SectionKind {
        PICKUP,
        TRIP,
        EMPTY_RETURN,
    }

    private data class SectionPattern(
        val kind: SectionKind,
        val pattern: String,
    )

    private data class SectionMarker(
        val kind: SectionKind,
        val start: Int,
        val endExclusive: Int,
    )

    private data class Lexicon(
        val priceLabelPattern: String,
        val priceJoinerPattern: String,
        val currencyPattern: String,
        val sectionPatterns: List<SectionPattern>,
    )

    private companion object {
        const val NUMBER_PATTERN = "[0-9]+(?:\\.[0-9]+)?"

        val CROATIAN = Lexicon(
            priceLabelPattern = "cijena|iznos|zarada",
            priceJoinerPattern = "je",
            currencyPattern = "€|eur|eura?",
            sectionPatterns = listOf(
                SectionPattern(
                    SectionKind.PICKUP,
                    "do\\s+putnika|dolazak(?:\\s+do\\s+putnika)?",
                ),
                SectionPattern(
                    SectionKind.TRIP,
                    "vožnja(?:\\s+s\\s+putnikom)?|" +
                        "voznja(?:\\s+s\\s+putnikom)?|s\\s+putnikom",
                ),
                SectionPattern(
                    SectionKind.EMPTY_RETURN,
                    "(?:prazni\\s+)?povratak",
                ),
            ),
        )

        val ENGLISH = Lexicon(
            priceLabelPattern = "price|fare|earnings|amount",
            priceJoinerPattern = "is",
            currencyPattern = "€|eur|euros?",
            sectionPatterns = listOf(
                SectionPattern(
                    SectionKind.PICKUP,
                    "pickup|to\\s+passenger",
                ),
                SectionPattern(
                    SectionKind.TRIP,
                    "passenger\\s+trip|trip|with\\s+passenger",
                ),
                SectionPattern(
                    SectionKind.EMPTY_RETURN,
                    "empty\\s+return|return",
                ),
            ),
        )
    }
}
