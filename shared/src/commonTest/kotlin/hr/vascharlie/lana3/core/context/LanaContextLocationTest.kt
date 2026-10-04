package hr.vascharlie.lana3.core.context

import hr.vascharlie.lana3.core.location.GeoPoint
import hr.vascharlie.lana3.core.location.LocationAssessment
import hr.vascharlie.lana3.core.location.LocationSnapshot
import hr.vascharlie.lana3.core.location.LocationStatus
import hr.vascharlie.lana3.core.model.LanguageContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanaContextLocationTest {
    private val language = LanguageContext(
        languageTag = "hr-HR",
        confidence = 1.0,
        userOverride = false,
    )

    @Test
    fun contextCanCarryAssessedUsableLocation() {
        val point = GeoPoint(45.55496, 18.69551)
        val context = LanaContext(
            language = language,
            location = LocationSnapshot(
                location = LocationAssessment(
                    status = LocationStatus.AVAILABLE,
                    point = point,
                    accuracyMeters = 8.0,
                    reason = "Location is usable",
                )
            )
        )

        assertEquals(point, context.location?.usablePoint())
    }

    @Test
    fun missingLocationObservationRemainsUnknown() {
        val context = LanaContext(language = language)
        assertNull(context.location)
    }

    @Test
    fun staleLocationInContextIsNotUsable() {
        val point = GeoPoint(45.55496, 18.69551)
        val context = LanaContext(
            language = language,
            location = LocationSnapshot(
                location = LocationAssessment(
                    status = LocationStatus.STALE,
                    point = point,
                    accuracyMeters = 8.0,
                    reason = "Location reading is stale",
                )
            )
        )

        assertNull(context.location?.usablePoint())
    }
}
