package hr.vascharlie.lana3.core.decision

import hr.vascharlie.lana3.core.authorization.AuthorizationLevel
import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.intent.Intent

class DecisionEngine {
    fun evaluate(intent: Intent, context: LanaContext): Decision = when (intent) {
        is Intent.SaveNote -> {
            if (intent.text.isBlank()) {
                Decision.CannotDecide("Note text is missing.")
            } else {
                Decision.Proposed(
                    explanation = "A local note can be prepared for saving.",
                    requiredAuthorization = AuthorizationLevel.EXECUTE,
                )
            }
        }
        is Intent.TaxiProfitability -> {
            if (intent.revenue < 0.0 || intent.totalKilometers <= 0.0) {
                Decision.CannotDecide("Revenue and total kilometers must be valid.")
            } else {
                val euroPerKm = intent.revenue / intent.totalKilometers
                Decision.Proposed(
                    explanation = "Nominal revenue per total kilometer is %.2f EUR/km.".format(euroPerKm),
                    requiredAuthorization = AuthorizationLevel.SUGGEST,
                )
            }
        }
        is Intent.Navigate -> {
            when {
                intent.destinationText.isBlank() ->
                    Decision.CannotDecide("Navigation destination is missing.")
                intent.resolvedDestination == null ->
                    Decision.CannotDecide("Navigation destination has not been resolved to coordinates.")
                context.location?.usablePoint() == null ->
                    Decision.CannotDecide("A reliable current location is required for navigation.")
                else ->
                    Decision.Proposed(
                        explanation = "Navigation can be prepared for the resolved destination.",
                        requiredAuthorization = AuthorizationLevel.EXECUTE,
                    )
            }
        }
        is Intent.Unknown -> Decision.CannotDecide("Intent is unknown; Lana must not guess.")
    }
}
