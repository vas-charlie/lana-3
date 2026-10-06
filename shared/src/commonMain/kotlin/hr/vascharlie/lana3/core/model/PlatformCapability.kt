package hr.vascharlie.lana3.core.model

enum class CapabilityAvailability {
    AVAILABLE,
    UNAVAILABLE,
    DEGRADED,
    UNKNOWN,
}

enum class CapabilityPermissionState {
    NOT_REQUIRED,
    GRANTED,
    DENIED,
    UNKNOWN,
}

enum class CapabilityExecutionMode {
    LOCAL,
    PLATFORM,
    EXTERNAL_SERVICE,
    UNKNOWN,
}

data class PlatformCapability(
    val id: String,
    val availability: CapabilityAvailability,
    val permissionState: CapabilityPermissionState = CapabilityPermissionState.UNKNOWN,
    val executionMode: CapabilityExecutionMode = CapabilityExecutionMode.UNKNOWN,
    val limitation: String? = null,
    val verifiedPlatformVersion: String? = null,
    val lastVerifiedAtEpochMillis: Long? = null,
) {
    init {
        require(id.isNotBlank()) { "Capability id must not be blank." }
    }

    val usable: Boolean
        get() =
            availability in setOf(
                CapabilityAvailability.AVAILABLE,
                CapabilityAvailability.DEGRADED,
            ) && permissionState != CapabilityPermissionState.DENIED

    companion object {
        fun unknown(id: String): PlatformCapability = PlatformCapability(
            id = id,
            availability = CapabilityAvailability.UNKNOWN,
            permissionState = CapabilityPermissionState.UNKNOWN,
            executionMode = CapabilityExecutionMode.UNKNOWN,
            limitation = "Capability has not been verified on this platform.",
        )
    }
}

class PlatformCapabilitiesSnapshot(
    capabilities: List<PlatformCapability>,
) {
    private val byId: Map<String, PlatformCapability>

    init {
        val duplicateIds = capabilities
            .groupingBy { it.id }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        require(duplicateIds.isEmpty()) {
            "Capability ids must be unique: ${duplicateIds.sorted().joinToString()}"
        }

        byId = capabilities.associateBy { it.id }
    }

    val capabilities: List<PlatformCapability>
        get() = byId.values.toList()

    fun resolve(id: String): PlatformCapability =
        byId[id] ?: PlatformCapability.unknown(id)

    fun isUsable(id: String): Boolean =
        resolve(id).usable

    fun knownIds(): Set<String> =
        byId.keys
}
