package hr.vascharlie.lana3.core.communication

import hr.vascharlie.lana3.core.authorization.AuthorizationLevel
import hr.vascharlie.lana3.core.model.CapabilityAvailability
import hr.vascharlie.lana3.core.model.PlatformCapability

enum class CommunicationChannel { PHONE_CALL, MESSAGE }

data class CommunicationRequest(
    val channel: CommunicationChannel,
    val recipient: String?,
    val content: String? = null,
)

sealed interface CommunicationAssessment {
    data class Ready(
        val request: CommunicationRequest,
        val requiredAuthorization: AuthorizationLevel,
    ) : CommunicationAssessment

    data class CannotProceed(val reason: String) : CommunicationAssessment
}

/**
 * Core policy only. Platform adapters decide how a supported device performs a call
 * or message. The core never assumes telephony, messaging or Bluetooth support.
 */
class CommunicationPolicy {
    fun assess(
        request: CommunicationRequest,
        capabilities: List<PlatformCapability>,
    ): CommunicationAssessment {
        if (request.recipient.isNullOrBlank()) {
            return CommunicationAssessment.CannotProceed("Recipient is required.")
        }
        if (request.channel == CommunicationChannel.MESSAGE && request.content.isNullOrBlank()) {
            return CommunicationAssessment.CannotProceed("Message content is required.")
        }

        val capabilityId = when (request.channel) {
            CommunicationChannel.PHONE_CALL -> "phone"
            CommunicationChannel.MESSAGE -> "messaging"
        }
        val capability = capabilities.firstOrNull { it.id == capabilityId }
            ?: return CommunicationAssessment.CannotProceed("Capability $capabilityId is unknown.")

        if (capability.availability == CapabilityAvailability.UNAVAILABLE ||
            capability.availability == CapabilityAvailability.UNKNOWN
        ) {
            return CommunicationAssessment.CannotProceed(
                capability.limitation ?: "Capability $capabilityId is not available."
            )
        }

        return CommunicationAssessment.Ready(
            request = request,
            requiredAuthorization = AuthorizationLevel.ASK_CONFIRMATION,
        )
    }
}

enum class AudioRoute { DEVICE, BLUETOOTH, OTHER, UNKNOWN }

data class BluetoothAudioContext(
    val route: AudioRoute,
    val capability: PlatformCapability?,
)

class BluetoothAudioPolicy {
    fun isUsable(context: BluetoothAudioContext): Boolean {
        if (context.route != AudioRoute.BLUETOOTH) return false
        val capability = context.capability ?: return false
        return capability.availability == CapabilityAvailability.AVAILABLE ||
            capability.availability == CapabilityAvailability.DEGRADED
    }
}
