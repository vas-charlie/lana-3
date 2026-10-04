package hr.vascharlie.lana3.core.navigation

import hr.vascharlie.lana3.core.intent.Intent
import hr.vascharlie.lana3.core.intent.Intent.NavigationStop

sealed interface NavigationIntentResolution {
    data class Ready(val intent: Intent.Navigate) : NavigationIntentResolution
    data class NeedsChoice(val stopIndex: Int, val originalText: String, val candidates: List<PlaceCandidate>) : NavigationIntentResolution
    data class DestinationNotFound(val stopIndex: Int, val originalText: String) : NavigationIntentResolution
}

class NavigationIntentCoordinator(private val destinationResolver: DestinationResolver) {
    suspend fun resolve(intent: Intent.Navigate): NavigationIntentResolution {
        val resolvedStops = mutableListOf<NavigationStop>()
        intent.stops.forEachIndexed { index, stop ->
            if (stop.resolvedDestination != null) {
                resolvedStops += stop
            } else when (val resolution = destinationResolver.resolve(stop.destinationText)) {
                is DestinationResolution.Resolved -> resolvedStops += stop.copy(resolvedDestination = resolution.candidate.point)
                is DestinationResolution.Ambiguous -> return NavigationIntentResolution.NeedsChoice(index, stop.destinationText, resolution.candidates)
                is DestinationResolution.NotFound -> return NavigationIntentResolution.DestinationNotFound(index, stop.destinationText)
            }
        }
        return NavigationIntentResolution.Ready(Intent.Navigate(resolvedStops))
    }
}
