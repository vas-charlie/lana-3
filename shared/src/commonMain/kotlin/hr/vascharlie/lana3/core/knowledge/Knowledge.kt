package hr.vascharlie.lana3.core.knowledge

/**
 * Durable knowledge that LANA can reuse before asking an external reasoning service.
 *
 * This is intentionally platform- and provider-neutral. It does not train model weights and
 * it does not assume that an external AI answer is automatically trustworthy.
 */
enum class KnowledgeScope {
    PRIVATE,
    GENERALIZED,
}

enum class KnowledgeSourceKind {
    LOCAL_RULE,
    USER_CONFIRMED,
    DOCUMENT,
    EXTERNAL_AI,
    OBSERVED_OUTCOME,
}

enum class KnowledgeStatus {
    CANDIDATE,
    VERIFIED,
    STALE,
    REJECTED,
}

data class KnowledgeEntry(
    val id: String,
    val key: String,
    val content: String,
    val scope: KnowledgeScope,
    val sourceKind: KnowledgeSourceKind,
    val status: KnowledgeStatus,
    val confidence: Double,
    val learnedAtEpochMillis: Long,
    val verifiedAtEpochMillis: Long? = null,
    val expiresAtEpochMillis: Long? = null,
    val sourceReference: String? = null,
) {
    init {
        require(id.isNotBlank()) { "Knowledge id must not be blank." }
        require(key.isNotBlank()) { "Knowledge key must not be blank." }
        require(content.isNotBlank()) { "Knowledge content must not be blank." }
        require(confidence in 0.0..1.0) { "Knowledge confidence must be between 0 and 1." }
    }

    fun isExpired(nowEpochMillis: Long): Boolean =
        expiresAtEpochMillis?.let { nowEpochMillis >= it } ?: false
}

/**
 * Storage boundary only. Concrete persistence belongs in platform/storage adapters.
 */
interface KnowledgeStore {
    fun findByKey(key: String): KnowledgeEntry?
    fun upsert(entry: KnowledgeEntry)
}

enum class KnowledgeFallbackReason {
    NOT_FOUND,
    NOT_VERIFIED,
    STALE_OR_EXPIRED,
    LOW_CONFIDENCE,
    PRIVATE_NOT_ALLOWED,
}

sealed class KnowledgeResolution {
    data class UseLocal(
        val entry: KnowledgeEntry,
    ) : KnowledgeResolution()

    data class NeedsExternalReasoning(
        val reason: KnowledgeFallbackReason,
    ) : KnowledgeResolution()
}

/**
 * Deterministic gate used before paying for external reasoning.
 *
 * A verified, fresh and sufficiently confident local entry wins. Anything uncertain is
 * rechecked instead of being guessed.
 */
class KnowledgeReusePolicy(
    private val minimumConfidence: Double = DEFAULT_MINIMUM_CONFIDENCE,
) {
    init {
        require(minimumConfidence in 0.0..1.0) {
            "Minimum knowledge confidence must be between 0 and 1."
        }
    }

    fun resolve(
        entry: KnowledgeEntry?,
        nowEpochMillis: Long,
        allowPrivate: Boolean = true,
    ): KnowledgeResolution {
        if (entry == null) {
            return KnowledgeResolution.NeedsExternalReasoning(
                KnowledgeFallbackReason.NOT_FOUND,
            )
        }

        if (!allowPrivate && entry.scope == KnowledgeScope.PRIVATE) {
            return KnowledgeResolution.NeedsExternalReasoning(
                KnowledgeFallbackReason.PRIVATE_NOT_ALLOWED,
            )
        }

        if (entry.status == KnowledgeStatus.STALE || entry.isExpired(nowEpochMillis)) {
            return KnowledgeResolution.NeedsExternalReasoning(
                KnowledgeFallbackReason.STALE_OR_EXPIRED,
            )
        }

        if (entry.status != KnowledgeStatus.VERIFIED) {
            return KnowledgeResolution.NeedsExternalReasoning(
                KnowledgeFallbackReason.NOT_VERIFIED,
            )
        }

        if (entry.confidence < minimumConfidence) {
            return KnowledgeResolution.NeedsExternalReasoning(
                KnowledgeFallbackReason.LOW_CONFIDENCE,
            )
        }

        return KnowledgeResolution.UseLocal(entry)
    }

    companion object {
        const val DEFAULT_MINIMUM_CONFIDENCE = 0.85
    }
}

/**
 * Defines how external answers enter LANA's own knowledge.
 *
 * External AI output starts as CANDIDATE. A separate verifier must promote it to VERIFIED.
 */
class KnowledgeLearningPolicy {
    fun externalCandidate(
        id: String,
        key: String,
        content: String,
        scope: KnowledgeScope,
        confidence: Double,
        learnedAtEpochMillis: Long,
        expiresAtEpochMillis: Long? = null,
        sourceReference: String? = null,
    ): KnowledgeEntry =
        KnowledgeEntry(
            id = id,
            key = key,
            content = content,
            scope = scope,
            sourceKind = KnowledgeSourceKind.EXTERNAL_AI,
            status = KnowledgeStatus.CANDIDATE,
            confidence = confidence,
            learnedAtEpochMillis = learnedAtEpochMillis,
            expiresAtEpochMillis = expiresAtEpochMillis,
            sourceReference = sourceReference,
        )

    fun verify(
        entry: KnowledgeEntry,
        verifiedAtEpochMillis: Long,
        confidence: Double = entry.confidence,
        sourceKind: KnowledgeSourceKind = entry.sourceKind,
        sourceReference: String? = entry.sourceReference,
    ): KnowledgeEntry {
        require(confidence in 0.0..1.0) {
            "Knowledge confidence must be between 0 and 1."
        }

        return entry.copy(
            status = KnowledgeStatus.VERIFIED,
            confidence = confidence,
            verifiedAtEpochMillis = verifiedAtEpochMillis,
            sourceKind = sourceKind,
            sourceReference = sourceReference,
        )
    }
}
