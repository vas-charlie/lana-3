package hr.vascharlie.lana3.core.location

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

data class LocationReading(
    val point: GeoPoint?,
    val accuracyMeters: Double?,
    val ageMillis: Long?,
    val permissionGranted: Boolean,
    val locationEnabled: Boolean
)

enum class LocationStatus {
    AVAILABLE,
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    UNAVAILABLE,
    STALE,
    INACCURATE
}

data class LocationAssessment(
    val status: LocationStatus,
    val point: GeoPoint? = null,
    val accuracyMeters: Double? = null,
    val reason: String
)

class LocationPolicy(
    private val maxAgeMillis: Long = 30_000,
    private val maxAccuracyMeters: Double = 100.0
) {
    fun assess(reading: LocationReading): LocationAssessment {
        if (!reading.permissionGranted) {
            return LocationAssessment(LocationStatus.PERMISSION_DENIED, reason = "Location permission is not granted")
        }
        if (!reading.locationEnabled) {
            return LocationAssessment(LocationStatus.LOCATION_DISABLED, reason = "Device location is disabled")
        }
        val point = reading.point
            ?: return LocationAssessment(LocationStatus.UNAVAILABLE, reason = "No location reading is available")
        val age = reading.ageMillis
        if (age == null || age < 0 || age > maxAgeMillis) {
            return LocationAssessment(LocationStatus.STALE, point, reading.accuracyMeters, "Location reading is stale or its age is unknown")
        }
        val accuracy = reading.accuracyMeters
        if (accuracy == null || accuracy < 0 || accuracy > maxAccuracyMeters) {
            return LocationAssessment(LocationStatus.INACCURATE, point, accuracy, "Location accuracy is insufficient or unknown")
        }
        return LocationAssessment(LocationStatus.AVAILABLE, point, accuracy, "Location is usable")
    }
}
