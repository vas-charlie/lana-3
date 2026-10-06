package hr.vascharlie.lana3.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CapabilityGateTest {
    private val gate = CapabilityGate()

    @Test
    fun readyWhenAllRequiredCapabilitiesAreAvailable() {
        val snapshot = PlatformCapabilitiesSnapshot(
            listOf(
                PlatformCapability(
                    id = "camera",
                    availability = CapabilityAvailability.AVAILABLE,
                    permissionState = CapabilityPermissionState.GRANTED,
                    executionMode = CapabilityExecutionMode.PLATFORM,
                ),
                PlatformCapability(
                    id = "microphone",
                    availability = CapabilityAvailability.AVAILABLE,
                    permissionState = CapabilityPermissionState.GRANTED,
                    executionMode = CapabilityExecutionMode.PLATFORM,
                ),
            )
        )

        val result = gate.evaluate(
            snapshot,
            listOf(
                CapabilityRequirement("camera"),
                CapabilityRequirement("microphone"),
            )
        )

        assertIs<CapabilityGateResult.Ready>(result)
    }

    @Test
    fun deniedPermissionBlocksExecution() {
        val snapshot = PlatformCapabilitiesSnapshot(
            listOf(
                PlatformCapability(
                    id = "camera",
                    availability = CapabilityAvailability.AVAILABLE,
                    permissionState = CapabilityPermissionState.DENIED,
                    executionMode = CapabilityExecutionMode.PLATFORM,
                )
            )
        )

        val result = gate.evaluate(snapshot, listOf(CapabilityRequirement("camera")))
        val blocked = assertIs<CapabilityGateResult.Blocked>(result)

        assertEquals("camera", blocked.failures.single().capabilityId)
        assertEquals("Required permission is denied.", blocked.failures.single().reason)
    }

    @Test
    fun unknownCapabilityBlocksInsteadOfGuessing() {
        val result = gate.evaluate(
            PlatformCapabilitiesSnapshot(emptyList()),
            listOf(CapabilityRequirement("screen_context"))
        )
        val blocked = assertIs<CapabilityGateResult.Blocked>(result)

        assertEquals("screen_context", blocked.failures.single().capabilityId)
        assertEquals(
            "Capability has not been verified on this platform.",
            blocked.failures.single().reason,
        )
    }

    @Test
    fun degradedCapabilityIsBlockedUnlessExplicitlyAllowed() {
        val snapshot = PlatformCapabilitiesSnapshot(
            listOf(
                PlatformCapability(
                    id = "location",
                    availability = CapabilityAvailability.DEGRADED,
                    permissionState = CapabilityPermissionState.GRANTED,
                    executionMode = CapabilityExecutionMode.PLATFORM,
                    limitation = "Only approximate location is available.",
                )
            )
        )

        val strict = gate.evaluate(
            snapshot,
            listOf(CapabilityRequirement("location", allowDegraded = false))
        )
        val tolerant = gate.evaluate(
            snapshot,
            listOf(CapabilityRequirement("location", allowDegraded = true))
        )

        val blocked = assertIs<CapabilityGateResult.Blocked>(strict)
        assertEquals(
            "Only approximate location is available.",
            blocked.failures.single().reason,
        )
        assertIs<CapabilityGateResult.Ready>(tolerant)
    }

    @Test
    fun returnsAllFailuresSoCallerCanExplainWhatIsMissing() {
        val snapshot = PlatformCapabilitiesSnapshot(
            listOf(
                PlatformCapability(
                    id = "camera",
                    availability = CapabilityAvailability.UNAVAILABLE,
                    limitation = "No camera hardware.",
                )
            )
        )

        val result = gate.evaluate(
            snapshot,
            listOf(
                CapabilityRequirement("camera"),
                CapabilityRequirement("microphone"),
            )
        )
        val blocked = assertIs<CapabilityGateResult.Blocked>(result)

        assertEquals(2, blocked.failures.size)
        assertEquals(setOf("camera", "microphone"), blocked.failures.map { it.capabilityId }.toSet())
    }
}
