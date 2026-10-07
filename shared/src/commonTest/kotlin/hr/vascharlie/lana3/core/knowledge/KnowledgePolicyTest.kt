package hr.vascharlie.lana3.core.knowledge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KnowledgePolicyTest {
    private val reuse = KnowledgeReusePolicy(minimumConfidence = 0.85)
    private val learning = KnowledgeLearningPolicy()

    @Test
    fun missingKnowledgeFallsBackToExternalReasoning() {
        val result = reuse.resolve(
            entry = null,
            nowEpochMillis = 1_000L,
        )

        assertEquals(
            KnowledgeFallbackReason.NOT_FOUND,
            assertIs<KnowledgeResolution.NeedsExternalReasoning>(result).reason,
        )
    }

    @Test
    fun verifiedFreshHighConfidenceKnowledgeIsReusedLocally() {
        val entry = verifiedEntry(confidence = 0.95)

        val result = reuse.resolve(
            entry = entry,
            nowEpochMillis = 2_000L,
        )

        assertEquals(
            entry,
            assertIs<KnowledgeResolution.UseLocal>(result).entry,
        )
    }

    @Test
    fun externalAiAnswerIsCandidateUntilVerified() {
        val candidate = learning.externalCandidate(
            id = "k-1",
            key = "taxi.rule.example",
            content = "Reusable result",
            scope = KnowledgeScope.PRIVATE,
            confidence = 0.95,
            learnedAtEpochMillis = 1_000L,
        )

        val result = reuse.resolve(
            entry = candidate,
            nowEpochMillis = 2_000L,
        )

        assertEquals(KnowledgeStatus.CANDIDATE, candidate.status)
        assertEquals(KnowledgeSourceKind.EXTERNAL_AI, candidate.sourceKind)
        assertEquals(
            KnowledgeFallbackReason.NOT_VERIFIED,
            assertIs<KnowledgeResolution.NeedsExternalReasoning>(result).reason,
        )
    }

    @Test
    fun verifiedCandidateBecomesReusable() {
        val candidate = learning.externalCandidate(
            id = "k-2",
            key = "taxi.rule.verified",
            content = "Verified reusable result",
            scope = KnowledgeScope.PRIVATE,
            confidence = 0.91,
            learnedAtEpochMillis = 1_000L,
        )

        val verified = learning.verify(
            entry = candidate,
            verifiedAtEpochMillis = 1_500L,
            sourceKind = KnowledgeSourceKind.OBSERVED_OUTCOME,
            sourceReference = "real-world-result",
        )

        val result = reuse.resolve(
            entry = verified,
            nowEpochMillis = 2_000L,
        )

        assertEquals(KnowledgeStatus.VERIFIED, verified.status)
        assertEquals(1_500L, verified.verifiedAtEpochMillis)
        assertEquals(
            verified,
            assertIs<KnowledgeResolution.UseLocal>(result).entry,
        )
    }

    @Test
    fun expiredKnowledgeMustBeRechecked() {
        val entry = verifiedEntry(
            confidence = 0.95,
            expiresAtEpochMillis = 2_000L,
        )

        val result = reuse.resolve(
            entry = entry,
            nowEpochMillis = 2_000L,
        )

        assertEquals(
            KnowledgeFallbackReason.STALE_OR_EXPIRED,
            assertIs<KnowledgeResolution.NeedsExternalReasoning>(result).reason,
        )
    }

    @Test
    fun lowConfidenceKnowledgeMustBeRechecked() {
        val entry = verifiedEntry(confidence = 0.70)

        val result = reuse.resolve(
            entry = entry,
            nowEpochMillis = 2_000L,
        )

        assertEquals(
            KnowledgeFallbackReason.LOW_CONFIDENCE,
            assertIs<KnowledgeResolution.NeedsExternalReasoning>(result).reason,
        )
    }

    @Test
    fun privateKnowledgeCannotLeakIntoGeneralizedContext() {
        val entry = verifiedEntry(
            confidence = 0.95,
            scope = KnowledgeScope.PRIVATE,
        )

        val result = reuse.resolve(
            entry = entry,
            nowEpochMillis = 2_000L,
            allowPrivate = false,
        )

        assertEquals(
            KnowledgeFallbackReason.PRIVATE_NOT_ALLOWED,
            assertIs<KnowledgeResolution.NeedsExternalReasoning>(result).reason,
        )
    }

    private fun verifiedEntry(
        confidence: Double,
        scope: KnowledgeScope = KnowledgeScope.PRIVATE,
        expiresAtEpochMillis: Long? = null,
    ): KnowledgeEntry =
        KnowledgeEntry(
            id = "verified",
            key = "known.rule",
            content = "Known local answer",
            scope = scope,
            sourceKind = KnowledgeSourceKind.USER_CONFIRMED,
            status = KnowledgeStatus.VERIFIED,
            confidence = confidence,
            learnedAtEpochMillis = 1_000L,
            verifiedAtEpochMillis = 1_500L,
            expiresAtEpochMillis = expiresAtEpochMillis,
        )
}
