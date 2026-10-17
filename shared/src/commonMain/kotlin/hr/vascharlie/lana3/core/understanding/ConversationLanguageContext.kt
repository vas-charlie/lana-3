package hr.vascharlie.lana3.core.understanding

/**
 * One understood speech turn. Speaker role and language evidence are kept explicit so
 * multilingual conversation can preserve who said what without guessing identity.
 */
enum class ConversationSpeakerRole {
    CHARLIE,
    OTHER_PERSON,
    UNKNOWN,
}

data class UnderstandingTurn(
    val speakerRole: ConversationSpeakerRole,
    val transcript: String,
    val detectedLanguageTag: String? = null,
)

data class ConversationLanguageContext(
    val charlieLanguageTag: String = "hr-HR",
    val lastOtherPersonLanguageTag: String? = null,
)

object ConversationLanguageContextPolicy {
    fun update(
        context: ConversationLanguageContext,
        turn: UnderstandingTurn,
    ): ConversationLanguageContext {
        val detected = turn.detectedLanguageTag
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        return when (turn.speakerRole) {
            ConversationSpeakerRole.CHARLIE -> context.copy(
                charlieLanguageTag = detected ?: context.charlieLanguageTag,
            )
            ConversationSpeakerRole.OTHER_PERSON -> context.copy(
                lastOtherPersonLanguageTag = detected ?: context.lastOtherPersonLanguageTag,
            )
            ConversationSpeakerRole.UNKNOWN -> context
        }
    }
}
