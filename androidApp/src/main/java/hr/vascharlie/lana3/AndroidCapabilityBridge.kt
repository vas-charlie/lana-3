package hr.vascharlie.lana3

import hr.vascharlie.lana3.core.model.CapabilityAvailability
import hr.vascharlie.lana3.core.model.CapabilityExecutionMode
import hr.vascharlie.lana3.core.model.CapabilityIds
import hr.vascharlie.lana3.core.model.CapabilityPermissionState
import hr.vascharlie.lana3.core.model.PlatformCapabilitiesSnapshot
import hr.vascharlie.lana3.core.model.PlatformCapability

object AndroidCapabilityBridge {
    fun toCoreSnapshot(
        readiness: AndroidDeviceReadiness,
        verifiedAtEpochMillis: Long = System.currentTimeMillis(),
    ): PlatformCapabilitiesSnapshot {
        val platformVersion = "Android ${readiness.androidVersion}"

        return PlatformCapabilitiesSnapshot(
            listOf(
                hardwareCapability(
                    id = CapabilityIds.CAMERA,
                    available = readiness.cameraAvailable,
                    permissionGranted = readiness.cameraPermissionGranted,
                    platformVersion = platformVersion,
                    verifiedAtEpochMillis = verifiedAtEpochMillis,
                    unavailableReason = "Camera hardware is not available on this device.",
                ),
                hardwareCapability(
                    id = CapabilityIds.MICROPHONE,
                    available = readiness.microphoneAvailable,
                    permissionGranted = readiness.microphonePermissionGranted,
                    platformVersion = platformVersion,
                    verifiedAtEpochMillis = verifiedAtEpochMillis,
                    unavailableReason = "Microphone hardware is not available on this device.",
                ),
                locationCapability(
                    readiness = readiness,
                    platformVersion = platformVersion,
                    verifiedAtEpochMillis = verifiedAtEpochMillis,
                ),
            )
        )
    }

    private fun hardwareCapability(
        id: String,
        available: Boolean,
        permissionGranted: Boolean,
        platformVersion: String,
        verifiedAtEpochMillis: Long,
        unavailableReason: String,
    ): PlatformCapability =
        if (!available) {
            PlatformCapability(
                id = id,
                availability = CapabilityAvailability.UNAVAILABLE,
                permissionState = CapabilityPermissionState.NOT_REQUIRED,
                executionMode = CapabilityExecutionMode.PLATFORM,
                limitation = unavailableReason,
                verifiedPlatformVersion = platformVersion,
                lastVerifiedAtEpochMillis = verifiedAtEpochMillis,
            )
        } else {
            PlatformCapability(
                id = id,
                availability = CapabilityAvailability.AVAILABLE,
                permissionState =
                    if (permissionGranted) {
                        CapabilityPermissionState.GRANTED
                    } else {
                        CapabilityPermissionState.DENIED
                    },
                executionMode = CapabilityExecutionMode.PLATFORM,
                limitation =
                    if (permissionGranted) null
                    else "Android runtime permission has not been granted.",
                verifiedPlatformVersion = platformVersion,
                lastVerifiedAtEpochMillis = verifiedAtEpochMillis,
            )
        }

    private fun locationCapability(
        readiness: AndroidDeviceReadiness,
        platformVersion: String,
        verifiedAtEpochMillis: Long,
    ): PlatformCapability {
        if (!readiness.locationAvailable) {
            return PlatformCapability(
                id = CapabilityIds.LOCATION,
                availability = CapabilityAvailability.UNAVAILABLE,
                permissionState = CapabilityPermissionState.NOT_REQUIRED,
                executionMode = CapabilityExecutionMode.PLATFORM,
                limitation = "Location capability is not available on this device.",
                verifiedPlatformVersion = platformVersion,
                lastVerifiedAtEpochMillis = verifiedAtEpochMillis,
            )
        }

        return when {
            readiness.preciseLocationGranted -> PlatformCapability(
                id = CapabilityIds.LOCATION,
                availability = CapabilityAvailability.AVAILABLE,
                permissionState = CapabilityPermissionState.GRANTED,
                executionMode = CapabilityExecutionMode.PLATFORM,
                verifiedPlatformVersion = platformVersion,
                lastVerifiedAtEpochMillis = verifiedAtEpochMillis,
            )

            readiness.coarseLocationGranted -> PlatformCapability(
                id = CapabilityIds.LOCATION,
                availability = CapabilityAvailability.DEGRADED,
                permissionState = CapabilityPermissionState.GRANTED,
                executionMode = CapabilityExecutionMode.PLATFORM,
                limitation = "Only approximate location is currently authorized.",
                verifiedPlatformVersion = platformVersion,
                lastVerifiedAtEpochMillis = verifiedAtEpochMillis,
            )

            else -> PlatformCapability(
                id = CapabilityIds.LOCATION,
                availability = CapabilityAvailability.AVAILABLE,
                permissionState = CapabilityPermissionState.DENIED,
                executionMode = CapabilityExecutionMode.PLATFORM,
                limitation = "Android location permission has not been granted.",
                verifiedPlatformVersion = platformVersion,
                lastVerifiedAtEpochMillis = verifiedAtEpochMillis,
            )
        }
    }
}
