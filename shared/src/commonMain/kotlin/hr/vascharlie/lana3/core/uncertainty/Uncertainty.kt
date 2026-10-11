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
            "KNOWN evidence must carry a value"
        }
        require(certainty != DataCertainty.UNKNOWN || value == null) {
            "UNKNOWN evidence must not carry a value"
        }
    }

    val needsConfirmation: Boolean
        get() = certainty != DataCertainty.KNOWN

    companion object {
        fun <T> known(value: T, evidence: String? = null): EvidenceValue<T> =
            EvidenceValue(value = value, certainty = DataCertainty.KNOWN, evidence = evidence)

        fun <T> uncertain(value: T?, evidence: String? = null): EvidenceValue<T> =
            EvidenceValue(value = value, certainty = DataCertainty.UNCERTAIN, evidence = evidence)

        fun <T> unknown(evidence: String? = null): EvidenceValue<T> =
            EvidenceValue(value = null, certainty = DataCertainty.UNKNOWN, evidence = evidence)
    }
}
