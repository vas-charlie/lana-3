package hr.vascharlie.lana3.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlatformCapabilityTest {
    @Test
    fun availableCapabilityWithGrantedPermissionIsUsable() {
        val capability = PlatformCapability(
            id = "camera",
            availability = CapabilityAvailability.AVAILABLE,
            permissionState = CapabilityPermissionState.GRANTED,
            executionMode = CapabilityExecutionMode.PLATFORM,
        )

        assertTrue(capability.usable)
    }

    @Test
    fun localCapabilityWithNoPermissionRequirementIsUsable() {
        val capability = PlatformCapability(
            id = "local_notes",
            availability = CapabilityAvailability.AVAILABLE,
            permissionState = CapabilityPermissionState.NOT_REQUIRED,
            executionMode = CapabilityExecutionMode.LOCAL,
        )

        assertTrue(capability.usable)
    }

    @Test
    fun deniedPermissionBlocksOtherwiseAvailableCapability() {
        val capability = PlatformCapability(
            id = "microphone",
            availability = CapabilityAvailability.AVAILABLE,
            permissionState = CapabilityPermissionState.DENIED,
            executionMode = CapabilityExecutionMode.PLATFORM,
        )

        assertFalse(capability.usable)
    }

    @Test
    fun unknownPermissionDoesNotPretendCapabilityIsUsable() {
        val capability = PlatformCapability(
            id = "camera",
            availability = CapabilityAvailability.AVAILABLE,
            permissionState = CapabilityPermissionState.UNKNOWN,
            executionMode = CapabilityExecutionMode.PLATFORM,
        )

        assertFalse(capability.usable)
    }

    @Test
    fun degradedCapabilityCanStillBeUsable() {
        val capability = PlatformCapability(
            id = "location",
            availability = CapabilityAvailability.DEGRADED,
            permissionState = CapabilityPermissionState.GRANTED,
            executionMode = CapabilityExecutionMode.PLATFORM,
            limitation = "Only approximate location is available.",
        )

        assertTrue(capability.usable)
        assertEquals("Only approximate location is available.", capability.limitation)
    }

    @Test
    fun unknownCapabilityDoesNotPretendToBeAvailable() {
        val snapshot = PlatformCapabilitiesSnapshot(emptyList())

        val capability = snapshot.resolve("screen_context")

        assertEquals(CapabilityAvailability.UNKNOWN, capability.availability)
        assertFalse(capability.usable)
        assertFalse(snapshot.isUsable("screen_context"))
    }

    @Test
    fun duplicateCapabilityIdsAreRejected() {
        val duplicate = PlatformCapability(
            id = "camera",
            availability = CapabilityAvailability.AVAILABLE,
        )

        assertFailsWith<IllegalArgumentException> {
            PlatformCapabilitiesSnapshot(listOf(duplicate, duplicate))
        }
    }

    @Test
    fun snapshotReturnsVerifiedCapabilityWithoutChangingItsEvidence() {
        val capability = PlatformCapability(
            id = "speech_input",
            availability = CapabilityAvailability.AVAILABLE,
            permissionState = CapabilityPermissionState.GRANTED,
            executionMode = CapabilityExecutionMode.LOCAL,
            verifiedPlatformVersion = "Android 16",
            lastVerifiedAtEpochMillis = 1_797_000_000_000L,
        )
        val snapshot = PlatformCapabilitiesSnapshot(listOf(capability))

        assertEquals(capability, snapshot.resolve("speech_input"))
        assertEquals(setOf("speech_input"), snapshot.knownIds())
    }
}
