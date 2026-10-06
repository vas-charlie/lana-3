package hr.vascharlie.lana3.core.model

data class CapabilityRequirement(
    val capabilityId: String,
    val allowDegraded: Boolean = false,
) {
    init {
        require(capabilityId.isNotBlank()) {
            "Capability requirement id must not be blank."
        }
    }
}

sealed interface CapabilityGateResult {
    data object Ready : CapabilityGateResult

    data class Blocked(
        val failures: List<CapabilityFailure>,
    ) : CapabilityGateResult
}

data class CapabilityFailure(
    val capabilityId: String,
    val reason: String,
)

class CapabilityGate {
    fun evaluate(
        snapshot: PlatformCapabilitiesSnapshot,
        requirements: List<CapabilityRequirement>,
    ): CapabilityGateResult {
        val failures = requirements.mapNotNull { requirement ->
            evaluateRequirement(snapshot.resolve(requirement.capabilityId), requirement)
        }

        return if (failures.isEmpty()) {
            CapabilityGateResult.Ready
        } else {
            CapabilityGateResult.Blocked(failures)
        }
    }

    private fun evaluateRequirement(
        capability: PlatformCapability,
        requirement: CapabilityRequirement,
    ): CapabilityFailure? {
        when (capability.permissionState) {
            CapabilityPermissionState.DENIED -> {
                return CapabilityFailure(
                    capabilityId = capability.id,
                    reason = "Required permission is denied.",
                )
            }

            CapabilityPermissionState.UNKNOWN -> {
                return CapabilityFailure(
                    capabilityId = capability.id,
                    reason = "Permission state has not been verified.",
                )
            }

            CapabilityPermissionState.GRANTED,
            CapabilityPermissionState.NOT_REQUIRED -> Unit
        }

        return when (capability.availability) {
            CapabilityAvailability.AVAILABLE -> null

            CapabilityAvailability.DEGRADED -> {
                if (requirement.allowDegraded) {
                    null
                } else {
                    CapabilityFailure(
                        capabilityId = capability.id,
                        reason = capability.limitation
                            ?: "Capability is available only in degraded mode.",
                    )
                }
            }

            CapabilityAvailability.UNAVAILABLE -> CapabilityFailure(
                capabilityId = capability.id,
                reason = capability.limitation
                    ?: "Capability is unavailable on this platform.",
            )

            CapabilityAvailability.UNKNOWN -> CapabilityFailure(
                capabilityId = capability.id,
                reason = capability.limitation
                    ?: "Capability has not been verified on this platform.",
            )
        }
    }
}
