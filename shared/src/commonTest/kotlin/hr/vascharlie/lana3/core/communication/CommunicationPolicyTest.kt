package hr.vascharlie.lana3.core.communication

import hr.vascharlie.lana3.core.authorization.AuthorizationLevel
import hr.vascharlie.lana3.core.model.CapabilityAvailability
import hr.vascharlie.lana3.core.model.PlatformCapability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CommunicationPolicyTest {
    private val policy = CommunicationPolicy()

    @Test
    fun callRequiresConfirmationWhenPhoneCapabilityExists() {
        val result = policy.assess(
            CommunicationRequest(CommunicationChannel.PHONE_CALL, recipient = "Robert"),
            listOf(PlatformCapability("phone", CapabilityAvailability.AVAILABLE)),
        )

        val ready = assertIs<CommunicationAssessment.Ready>(result)
        assertEquals(AuthorizationLevel.ASK_CONFIRMATION, ready.requiredAuthorization)
    }

    @Test
    fun messageWithoutContentIsRejected() {
        val result = policy.assess(
            CommunicationRequest(CommunicationChannel.MESSAGE, recipient = "Robert"),
            listOf(PlatformCapability("messaging", CapabilityAvailability.AVAILABLE)),
        )
        assertIs<CommunicationAssessment.CannotProceed>(result)
    }

    @Test
    fun missingCapabilityIsNotGuessed() {
        val result = policy.assess(
            CommunicationRequest(CommunicationChannel.PHONE_CALL, recipient = "Robert"),
            emptyList(),
        )
        assertIs<CommunicationAssessment.CannotProceed>(result)
    }

    @Test
    fun unavailablePhoneCannotProceed() {
        val result = policy.assess(
            CommunicationRequest(CommunicationChannel.PHONE_CALL, recipient = "Robert"),
            listOf(PlatformCapability("phone", CapabilityAvailability.UNAVAILABLE)),
        )
        assertIs<CommunicationAssessment.CannotProceed>(result)
    }

    @Test
    fun bluetoothRouteNeedsReportedBluetoothCapability() {
        val bluetooth = BluetoothAudioPolicy()
        assertTrue(
            bluetooth.isUsable(
                BluetoothAudioContext(
                    AudioRoute.BLUETOOTH,
                    PlatformCapability("bluetooth-audio", CapabilityAvailability.DEGRADED),
                )
            )
        )
        assertFalse(bluetooth.isUsable(BluetoothAudioContext(AudioRoute.BLUETOOTH, null)))
        assertFalse(
            bluetooth.isUsable(
                BluetoothAudioContext(
                    AudioRoute.DEVICE,
                    PlatformCapability("bluetooth-audio", CapabilityAvailability.AVAILABLE),
                )
            )
        )
    }
}
