package hr.vascharlie.lana3.core.model

/** Language is context, never a business-rule constant. */
data class LanguageContext(
    val languageTag: String,
    val confidence: Double? = null,
    val userOverride: Boolean = false,
)
