package hr.vascharlie.lana3.core.uncertainty

enum class DataCertainty {
    KNOWN,
    UNCERTAIN,
    UNKNOWN,
}

data class EvidenceValue<T>(
    val value: T?,
    val certainty: DataCertainty,
    val evidence: String? = null,
) {
    init {
        require(certainty != DataCertainty.KNOWN || value != null) {
            "Known data must carry a value"
        }
        require(certainty != DataCertainty.UNKNOWN || value == null) {
            "Unknown data must not carry a guessed value"
        }
    }

    val needsConfirmation: Boolean
        get() = certainty != DataCertainty.KNOWN

    companion object {
        fun <T> known(value: T, evidence: String? = null): EvidenceValue<T> =
            EvidenceValue(value, DataCertainty.KNOWN, evidence)

        fun <T> uncertain(value: T?, evidence: String? = null): EvidenceValue<T> =
            EvidenceValue(value, DataCertainty.UNCERTAIN, evidence)

        fun <T> unknown(evidence: String? = null): EvidenceValue<T> =
            EvidenceValue(null, DataCertainty.UNKNOWN, evidence)
    }
}
