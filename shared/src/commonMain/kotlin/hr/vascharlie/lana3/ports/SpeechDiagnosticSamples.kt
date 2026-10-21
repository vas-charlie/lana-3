package hr.vascharlie.lana3.ports

/**
 * A fixed spoken-text sample for comparing Croatian TTS voices.
 *
 * It is a diagnostic prompt, not an automatic pronunciation assessment.
 * The user must still listen and decide whether the voice is correct.
 * Kept as a single shared source so all device adapters can reuse it.
 */
object SpeechDiagnosticSamples {
    const val CROATIAN_LANGUAGE_TAG: String = "hr-HR"

    /** Tests me/mi and Croatian đ in a full-sentence context. */
    const val CROATIAN_PRONUNCIATION: String =
        "Molim te, pogledaj me i reci mi razumiješ li me. " +
        "Čekam te kod Tvrđe. " +
        "Ova je daska tvrđa, ali treća je najtvrđa. " +
        "Tvrđavica je odmah iza ugla."
}
