package hr.vascharlie.lana3.core.decision

import hr.vascharlie.lana3.core.authorization.AuthorizationLevel
import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.intent.Intent

class DecisionEngine {
    fun evaluate(intent: Intent, context: LanaContext): Decision = when (intent) {
        is Intent.SaveNote -> if (intent.text.isBlank()) Decision.CannotDecide("Note text is missing.") else Decision.Proposed("A local note can be prepared for saving.", AuthorizationLevel.EXECUTE)
        is Intent.TaxiProfitability -> {
            if (intent.revenue < 0.0 || intent.totalKilometers <= 0.0) Decision.CannotDecide("Revenue and total kilometers must be valid.")
            else Decision.Proposed("Nominal revenue per total kilometer is %.2f EUR/km.".format(intent.revenue / intent.totalKilometers), AuthorizationLevel.SUGGEST)
        }
        is Intent.Navigate -> when {
            intent.stops.isEmpty() -> Decision.CannotDecide("At least one navigation destination is required.")
            intent.stops.any { it.destinationText.isBlank() } -> Decision.CannotDecide("Navigation destination is missing.")
            intent.stops.any { it.resolvedDestination == null } -> Decision.CannotDecide("Every navigation stop must be resolved to coordinates.")
            context.location?.usablePoint() == null -> Decision.CannotDecide("A reliable current location is required for navigation.")
            else -> Decision.Proposed("Navigation can be prepared for " + intent.stops.size + " ordered stop(s).", AuthorizationLevel.EXECUTE)
        }
        is Intent.Unknown -> Decision.CannotDecide("Intent is unknown; Lana must not guess.")
    }
}
