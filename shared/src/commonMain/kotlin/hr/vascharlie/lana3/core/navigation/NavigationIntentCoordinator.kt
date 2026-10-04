package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.intent.Intent

sealed interface NavigationIntentResolution {
    data class Ready(val intent: Intent.Navigate) : NavigationIntentResolution
    data class NeedsChoice(
        val originalText: String,
        val candidates: List<PlaceCandidate>,
    ) : NavigationIntentResolution
    data class DestinationNotFound(val originalText: String) : NavigationIntentResolution
}

/**
 * Bridges natural-language navigation intent with destination resolution.
 * It never silently chooses between multiple place candidates.
 */
class NavigationIntentCoordinator(
    private val destinationResolver: DestinationResolver,
) {
    suspend fun resolve(intent: Intent.Navigate): NavigationIntentResolution {
        return when (val resolution = destinationResolver.resolve(intent.destinationText)) {
            is DestinationResolution.Resolved ->
                NavigationIntentResolution.Ready(
                    intent.copy(resolvedDestination = resolution.candidate.point)
                )
            is DestinationResolution.Ambiguous ->
                NavigationIntentResolution.NeedsChoice(
                    originalText = intent.destinationText,
                    candidates = resolution.candidates,
                )
            is DestinationResolution.NotFound ->
                NavigationIntentResolution.DestinationNotFound(intent.destinationText)
        }
    }
}
