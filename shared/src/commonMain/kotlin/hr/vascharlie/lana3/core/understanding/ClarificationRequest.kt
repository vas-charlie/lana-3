package hr.vascharlie.lana3.core.understanding

/**
 * Structured clarification lets the UI/voice layer localize wording without hiding
 * why Lana needs another answer.
 */
enum class ClarificationKind {
    REPEAT_UTTERANCE,
    CONFIRM_DESTINATION,
    CHOOSE_DESTINATION,
}

data class ClarificationRequest(
    val kind: ClarificationKind,
    val subject: String? = null,
    val candidates: List<String> = emptyList(),
)

object NavigationClarificationPolicy {
    fun from(resolution: SlotResolution): ClarificationRequest? =
        when (resolution) {
            is SlotResolution.Resolved -> null

            is SlotResolution.NotFound -> ClarificationRequest(
                kind = ClarificationKind.CONFIRM_DESTINATION,
                subject = resolution.original.value,
            )

            is SlotResolution.Ambiguous -> {
                val candidates = resolution.candidates
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()

                if (candidates.isEmpty()) {
                    ClarificationRequest(
                        kind = ClarificationKind.CONFIRM_DESTINATION,
                        subject = resolution.original.value,
                    )
                } else {
                    ClarificationRequest(
                        kind = ClarificationKind.CHOOSE_DESTINATION,
                        subject = resolution.original.value,
                        candidates = candidates,
                    )
                }
            }
        }

    fun from(reason: ClarificationReason): ClarificationRequest =
        when (reason) {
            ClarificationReason.NO_USABLE_TRANSCRIPT ->
                ClarificationRequest(ClarificationKind.REPEAT_UTTERANCE)

            ClarificationReason.MISSING_RECOGNITION_EVIDENCE ->
                ClarificationRequest(ClarificationKind.REPEAT_UTTERANCE)
        }
}
