package hr.vascharlie.lana3.core.voice

import hr.vascharlie.lana3.ports.SpeechOutputRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RemoteSpeechGateTest {
    private val gate = RemoteSpeechGate()
    private val request = SpeechOutputRequest(
        text = "Molim te, pogledaj me. Ova je daska tvrđa.",
        languageTag = "hr-HR",
    )

    private fun approvedContext() = RemoteSpeechGateContext(
        providerId = "approved-provider",
        voiceId = "verified-voice",
        providerApproved = true,
        voiceQualityAccepted = true,
        providerSupportsRequestedLanguage = true,
        consentedProviderId = "approved-provider",
        remoteProcessingPermitted = true,
        secureBackendReady = true,
        providerReachable = true,
        quote = RemoteSpeechPriceQuote(
            providerId = "approved-provider",
            voiceId = "verified-voice",
            estimatedCostMicros = 400L,
            verifiedForCurrentTariff = true,
        ),
        maxPerRequestMicros = 1000L,
        remainingMonthlyBudgetMicros = 900L,
    )

    private fun expectBlocked(
        reason: RemoteSpeechBlockReason,
        context: RemoteSpeechGateContext,
        text: String = request.text,
    ) {
        assertEquals(
            RemoteSpeechGateDecision.Blocked(reason),
            gate.evaluate(request.copy(text = text), context),
        )
    }

    @Test
    fun properlyVerifiedRequestIsEligibleOnlyForBackendReservation() {
        assertEquals(
            RemoteSpeechGateDecision.EligibleForBackendReservation(
                providerId = "approved-provider",
                voiceId = "verified-voice",
                estimatedCostMicros = 400L,
            ),
            gate.evaluate(request, approvedContext()),
        )
    }

    @Test
    fun emptyTextAndMissingIdentityAreRejected() {
        expectBlocked(RemoteSpeechBlockReason.EMPTY_TEXT, approvedContext(), text = " \t ")
        expectBlocked(
            RemoteSpeechBlockReason.PROVIDER_OR_VOICE_MISSING,
            approvedContext().copy(providerId = " "),
        )
        expectBlocked(
            RemoteSpeechBlockReason.PROVIDER_OR_VOICE_MISSING,
            approvedContext().copy(voiceId = null),
        )
    }

    @Test
    fun providerAndVoiceNeedExplicitAcceptance() {
        expectBlocked(
            RemoteSpeechBlockReason.PROVIDER_NOT_APPROVED,
            approvedContext().copy(providerApproved = false),
        )
        expectBlocked(
            RemoteSpeechBlockReason.VOICE_NOT_ACCEPTED,
            approvedContext().copy(voiceQualityAccepted = false),
        )
        expectBlocked(
            RemoteSpeechBlockReason.LANGUAGE_NOT_SUPPORTED,
            approvedContext().copy(providerSupportsRequestedLanguage = false),
        )
    }

    @Test
    fun consentIsProviderScopedAndRemoteProcessingCanBeProhibited() {
        expectBlocked(
            RemoteSpeechBlockReason.PROVIDER_CONSENT_MISSING,
            approvedContext().copy(consentedProviderId = null),
        )
        expectBlocked(
            RemoteSpeechBlockReason.PROVIDER_CONSENT_MISSING,
            approvedContext().copy(consentedProviderId = "other-provider"),
        )
        expectBlocked(
            RemoteSpeechBlockReason.REMOTE_PROCESSING_NOT_PERMITTED,
            approvedContext().copy(remoteProcessingPermitted = false),
        )
    }

    @Test
    fun cloudRequiresSecureBackendAndReachableProvider() {
        expectBlocked(
            RemoteSpeechBlockReason.SECURE_BACKEND_UNAVAILABLE,
            approvedContext().copy(secureBackendReady = false),
        )
        expectBlocked(
            RemoteSpeechBlockReason.PROVIDER_UNAVAILABLE,
            approvedContext().copy(providerReachable = false),
        )
    }

    @Test
    fun missingStaleOrInvalidQuoteFailsClosed() {
        val original = approvedContext()
        expectBlocked(RemoteSpeechBlockReason.PRICE_UNVERIFIED, original.copy(quote = null))
        expectBlocked(
            RemoteSpeechBlockReason.PRICE_UNVERIFIED,
            original.copy(quote = original.quote?.copy(verifiedForCurrentTariff = false)),
        )
        expectBlocked(
            RemoteSpeechBlockReason.PRICE_UNVERIFIED,
            original.copy(quote = original.quote?.copy(estimatedCostMicros = -1L)),
        )
    }

    @Test
    fun quotesCannotBeReusedAcrossProvidersOrVoices() {
        val original = approvedContext()
        expectBlocked(
            RemoteSpeechBlockReason.PRICE_UNVERIFIED,
            original.copy(quote = original.quote?.copy(providerId = "other-provider")),
        )
        expectBlocked(
            RemoteSpeechBlockReason.PRICE_UNVERIFIED,
            original.copy(quote = original.quote?.copy(voiceId = "other-voice")),
        )
    }

    @Test
    fun unknownOrNegativeBudgetsFailClosed() {
        expectBlocked(
            RemoteSpeechBlockReason.INVALID_BUDGET,
            approvedContext().copy(maxPerRequestMicros = null),
        )
        expectBlocked(
            RemoteSpeechBlockReason.INVALID_BUDGET,
            approvedContext().copy(remainingMonthlyBudgetMicros = null),
        )
        expectBlocked(
            RemoteSpeechBlockReason.INVALID_BUDGET,
            approvedContext().copy(maxPerRequestMicros = -1L),
        )
        expectBlocked(
            RemoteSpeechBlockReason.INVALID_BUDGET,
            approvedContext().copy(remainingMonthlyBudgetMicros = -1L),
        )
    }

    @Test
    fun quoteCannotExceedPerRequestOrRemainingMonthlyBudget() {
        expectBlocked(
            RemoteSpeechBlockReason.OVER_PER_REQUEST_LIMIT,
            approvedContext().copy(maxPerRequestMicros = 399L),
        )
        expectBlocked(
            RemoteSpeechBlockReason.OVER_MONTHLY_BUDGET,
            approvedContext().copy(remainingMonthlyBudgetMicros = 399L),
        )
    }

    @Test
    fun exactBudgetBoundaryIsEligibleButNotCharged() {
        val context = approvedContext().copy(
            maxPerRequestMicros = 400L,
            remainingMonthlyBudgetMicros = 400L,
        )
        assertIs<RemoteSpeechGateDecision.EligibleForBackendReservation>(
            gate.evaluate(request, context),
        )
    }

    @Test
    fun verifiedZeroCostRequestCanPassWithZeroAllowance() {
        val context = approvedContext().copy(
            quote = approvedContext().quote?.copy(estimatedCostMicros = 0L),
            maxPerRequestMicros = 0L,
            remainingMonthlyBudgetMicros = 0L,
        )
        assertIs<RemoteSpeechGateDecision.EligibleForBackendReservation>(
            gate.evaluate(request, context),
        )
    }

    @Test
    fun largeQuoteComparisonDoesNotOverflow() {
        val context = approvedContext().copy(
            quote = approvedContext().quote?.copy(estimatedCostMicros = Long.MAX_VALUE),
            maxPerRequestMicros = Long.MAX_VALUE,
            remainingMonthlyBudgetMicros = Long.MAX_VALUE,
        )
        assertIs<RemoteSpeechGateDecision.EligibleForBackendReservation>(
            gate.evaluate(request, context),
        )
    }
}
