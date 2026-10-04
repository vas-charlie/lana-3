package hr.vascharlie.lana3.core.offline

enum class ConnectivityState { ONLINE, OFFLINE, UNKNOWN }

enum class NetworkRequirement { LOCAL_ONLY, NETWORK_REQUIRED, NETWORK_OPTIONAL }

data class CapabilityAvailability(
    val available: Boolean,
    val mode: AvailabilityMode,
    val reason: String
)

enum class AvailabilityMode { LOCAL, ONLINE, DEGRADED, UNAVAILABLE }

class OfflineCapabilityResolver {
    fun resolve(
        connectivity: ConnectivityState,
        requirement: NetworkRequirement
    ): CapabilityAvailability = when (requirement) {
        NetworkRequirement.LOCAL_ONLY -> CapabilityAvailability(
            available = true,
            mode = AvailabilityMode.LOCAL,
            reason = "Capability is available locally"
        )
        NetworkRequirement.NETWORK_REQUIRED -> when (connectivity) {
            ConnectivityState.ONLINE -> CapabilityAvailability(true, AvailabilityMode.ONLINE, "Network is available")
            ConnectivityState.OFFLINE -> CapabilityAvailability(false, AvailabilityMode.UNAVAILABLE, "Internet connection is required")
            ConnectivityState.UNKNOWN -> CapabilityAvailability(false, AvailabilityMode.UNAVAILABLE, "Network state is unknown")
        }
        NetworkRequirement.NETWORK_OPTIONAL -> when (connectivity) {
            ConnectivityState.ONLINE -> CapabilityAvailability(true, AvailabilityMode.ONLINE, "Full online capability")
            ConnectivityState.OFFLINE -> CapabilityAvailability(true, AvailabilityMode.DEGRADED, "Running with local capability only")
            ConnectivityState.UNKNOWN -> CapabilityAvailability(true, AvailabilityMode.DEGRADED, "Network state is unknown; using local capability only")
        }
    }
}
