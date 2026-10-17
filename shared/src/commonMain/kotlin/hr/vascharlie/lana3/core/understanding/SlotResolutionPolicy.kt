package hr.vascharlie.lana3.core.understanding

/**
 * A semantic slot becomes actionable only after a resolver reports what it found.
 * Text similarity or STT confidence alone is never treated as place verification.
 */
sealed interface SlotResolution {
    data class Resolved(
        val original: UnderstoodSlot,
        val canonicalValue: String,
    ) : SlotResolution

    data class Ambiguous(
        val original: UnderstoodSlot,
        val candidates: List<String>,
    ) : SlotResolution

    data class NotFound(
        val original: UnderstoodSlot,
    ) : SlotResolution
}

enum class SlotActionability {
    READY,
    CLARIFY,
}

data class SlotActionabilityAssessment(
    val actionability: SlotActionability,
    val value: String? = null,
)

object SlotResolutionPolicy {
    fun assess(resolution: SlotResolution): SlotActionabilityAssessment =
        when (resolution) {
            is SlotResolution.Resolved -> SlotActionabilityAssessment(
                actionability = SlotActionability.READY,
                value = resolution.canonicalValue.trim().takeIf { it.isNotEmpty() },
            ).let { assessment ->
                if (assessment.value == null) {
                    SlotActionabilityAssessment(SlotActionability.CLARIFY)
                } else {
                    assessment
                }
            }

            is SlotResolution.Ambiguous ->
                SlotActionabilityAssessment(SlotActionability.CLARIFY)

            is SlotResolution.NotFound ->
                SlotActionabilityAssessment(SlotActionability.CLARIFY)
        }
}
