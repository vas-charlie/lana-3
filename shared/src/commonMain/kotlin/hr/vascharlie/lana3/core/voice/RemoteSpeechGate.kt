package hr.vascharlie.lana3.core.voice

import hr.vascharlie.lana3.ports.SpeechOutputRequest

/**
 * An estimated provider price, in millionths of the configured billing currency.
 * No exchange-rate conversion, tax calculation or monetary reservation happens here.
 *
 * This quote must come from an approved backend pricing configuration, not a
 * value supplied by an untrusted client.
 */
data class RemoteSpeechPriceQuote(
    val providerId: String,
    val voiceId: String,
    val estimatedCostMicros: Long,
    val currencyCode: String,
    val verifiedForCurrentTariff: Boolean,
)

/**
 * Evidence required before LANA can even ask the backend to reserve budget
 * for an external TTS request. No provider, tariff or language is hard-coded.
 */
data class RemoteSpeechGateContext(
    val providerId: String?,
    val voiceId: String?,
    val providerApproved: Boolean,
    val voiceQualityAccepted: Boolean,
    val providerSupportsRequestedLanguage: Boolean,
    val consentedProviderId: String?,
    val remoteProcessingPermitted: Boolean,
    val secureBackendReady: Boolean,
    val providerReachable: Boolean,
    val quote: RemoteSpeechPriceQuote?,
    val budgetCurrencyCode: String?,
    val maxPerRequestMicros: Long?,
    val remainingMonthlyBudgetMicros: Long?,
)

enum class RemoteSpeechBlockReason {
    EMPTY_TEXT,
    PROVIDER_OR_VOICE_MISSING,
    PROVIDER_NOT_APPROVED,
    VOICE_NOT_ACCEPTED,
    LANGUAGE_NOT_SUPPORTED,
    PROVIDER_CONSENT_MISSING,
    REMOTE_PROCESSING_NOT_PERMITTED,
    SECURE_BACKEND_UNAVAILABLE,
    PROVIDER_UNAVAILABLE,
    PRICE_UNVERIFIED,
    INVALID_BUDGET,
    CURRENCY_MISMATCH,
    OVER_PER_REQUEST_LIMIT,
    OVER_MONTHLY_BUDGET,
}

sealed interface RemoteSpeechGateDecision {
    /** Preflight only. Backend must atomically reserve budget before sending text. */
    data class EligibleForBackendReservation(
        val providerId: String,
        val voiceId: String,
        val estimatedCostMicros: Long,
        val currencyCode: String,
    ) : RemoteSpeechGateDecision

    data class Blocked(val reason: RemoteSpeechBlockReason) : RemoteSpeechGateDecision
}

/**
 * Fail-closed, vendor-neutral preflight for paid / remote speech.
 *
 * It never transmits user text, obtains tokens, stores consent or spends money.
 * The authenticated backend must independently verify consent/provider policy,
 * recompute the quote, atomically reserve the budget and enforce actual limits.
 *
 * Do not use this as the only billing, access-control or privacy boundary.
 */
class RemoteSpeechGate {
    fun evaluate(
        request: SpeechOutputRequest,
        context: RemoteSpeechGateContext,
    ): RemoteSpeechGateDecision {
        fun block(reason: RemoteSpeechBlockReason) = RemoteSpeechGateDecision.Blocked(reason)

        if (request.text.isBlank()) return block(RemoteSpeechBlockReason.EMPTY_TEXT)

        val providerId = context.providerId?.takeIf(String::isNotBlank)
            ?: return block(RemoteSpeechBlockReason.PROVIDER_OR_VOICE_MISSING)
        val voiceId = context.voiceId?.takeIf(String::isNotBlank)
            ?: return block(RemoteSpeechBlockReason.PROVIDER_OR_VOICE_MISSING)

        if (!context.providerApproved) return block(RemoteSpeechBlockReason.PROVIDER_NOT_APPROVED)
        if (!context.voiceQualityAccepted) return block(RemoteSpeechBlockReason.VOICE_NOT_ACCEPTED)
        if (!context.providerSupportsRequestedLanguage) {
            return block(RemoteSpeechBlockReason.LANGUAGE_NOT_SUPPORTED)
        }

        // Consent is scoped to the provider. A new vendor needs fresh consent.
        if (context.consentedProviderId != providerId) {
            return block(RemoteSpeechBlockReason.PROVIDER_CONSENT_MISSING)
        }
        if (!context.remoteProcessingPermitted) {
            return block(RemoteSpeechBlockReason.REMOTE_PROCESSING_NOT_PERMITTED)
        }
        if (!context.secureBackendReady) {
            return block(RemoteSpeechBlockReason.SECURE_BACKEND_UNAVAILABLE)
        }
        if (!context.providerReachable) return block(RemoteSpeechBlockReason.PROVIDER_UNAVAILABLE)

        val quote = context.quote
            ?: return block(RemoteSpeechBlockReason.PRICE_UNVERIFIED)
        if (!quote.verifiedForCurrentTariff ||
            quote.providerId != providerId ||
            quote.voiceId != voiceId ||
            quote.estimatedCostMicros < 0L ||
            !quote.currencyCode.matches(Regex("[A-Z]{3}"))
        ) {
            return block(RemoteSpeechBlockReason.PRICE_UNVERIFIED)
        }

        val budgetCurrency = context.budgetCurrencyCode
        if (budgetCurrency == null || !budgetCurrency.matches(Regex("[A-Z]{3}"))) {
            return block(RemoteSpeechBlockReason.INVALID_BUDGET)
        }
        if (budgetCurrency != quote.currencyCode) {
            return block(RemoteSpeechBlockReason.CURRENCY_MISMATCH)
        }

        val perRequestLimit = context.maxPerRequestMicros
            ?: return block(RemoteSpeechBlockReason.INVALID_BUDGET)
        val remainingBudget = context.remainingMonthlyBudgetMicros
            ?: return block(RemoteSpeechBlockReason.INVALID_BUDGET)
        if (perRequestLimit < 0L || remainingBudget < 0L) {
            return block(RemoteSpeechBlockReason.INVALID_BUDGET)
        }

        // No floating-point arithmetic or subtraction: comparisons cannot overflow.
        if (quote.estimatedCostMicros > perRequestLimit) {
            return block(RemoteSpeechBlockReason.OVER_PER_REQUEST_LIMIT)
        }
        if (quote.estimatedCostMicros > remainingBudget) {
            return block(RemoteSpeechBlockReason.OVER_MONTHLY_BUDGET)
        }

        return RemoteSpeechGateDecision.EligibleForBackendReservation(
            providerId = providerId,
            voiceId = voiceId,
            estimatedCostMicros = quote.estimatedCostMicros,
            currencyCode = quote.currencyCode,
        )
    }
}
