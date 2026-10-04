package hr.vascharlie.lana3.core.model

enum class CapabilityAvailability {
    AVAILABLE,
    UNAVAILABLE,
    DEGRADED,
    UNKNOWN,
}

data class PlatformCapability(
    val id: String,
    val availability: CapabilityAvailability,
    val limitation: String? = null,
)
