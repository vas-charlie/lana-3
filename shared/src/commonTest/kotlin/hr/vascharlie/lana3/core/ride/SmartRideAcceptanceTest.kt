package hr.vascharlie.lana3.core.ride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SmartRideAcceptanceTest {
    private val engine = SmartRideAcceptance()
    private val rules = RideAcceptanceRules(
        acceptMinEurPerKm = 1.50,
        considerMinEurPerKm = 1.00,
        acceptMinEurPerHour = 30.0,
        considerMinEurPerHour = 20.0,
    )

    @Test
    fun acceptsOfferOnlyWhenBothConfiguredTargetsAreMet() {
        val result = engine.assess(
            RideOffer(30.0, 2.0, 13.0, 5.0, 25.0),
            rules,
        )
        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(RideRecommendation.ACCEPT, assessed.assessment.recommendation)
        assertEquals(2.0, assessed.assessment.eurPerKm)
        assertEquals(60.0, assessed.assessment.eurPerHour)
    }

    @Test
    fun considersBorderlineOffer() {
        val result = engine.assess(
            RideOffer(12.0, 2.0, 8.0, 10.0, 20.0),
            rules,
        )
        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(RideRecommendation.CONSIDER, assessed.assessment.recommendation)
    }

    @Test
    fun skipsOfferThatMissesConfiguredTargets() {
        val result = engine.assess(
            RideOffer(10.0, 5.0, 10.0, 15.0, 30.0),
            rules,
        )
        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(RideRecommendation.SKIP, assessed.assessment.recommendation)
    }

    @Test
    fun missingOfferDataIsNotGuessed() {
        val result = engine.assess(
            RideOffer(null, 2.0, 8.0, 5.0, 20.0),
            rules,
        )
        val missing = assertIs<RideAssessmentResult.InsufficientData>(result)
        assertTrue("priceEur" in missing.missingFields)
    }

    @Test
    fun pickupWorkIsIncludedInProfitability() {
        val result = engine.assess(
            RideOffer(20.0, 10.0, 10.0, 20.0, 20.0),
            rules,
        )
        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(1.0, assessed.assessment.eurPerKm)
        assertEquals(30.0, assessed.assessment.eurPerHour)
        assertEquals(RideRecommendation.CONSIDER, assessed.assessment.recommendation)
    }

    @Test
    fun negativeIndividualDistanceIsRejectedEvenWhenTotalWouldStayPositive() {
        val result = engine.assess(
            RideOffer(20.0, -1.0, 10.0, 5.0, 20.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("distance", ignoreCase = true)
            }
        )
    }

    @Test
    fun negativeIndividualTimeIsRejectedEvenWhenTotalWouldStayPositive() {
        val result = engine.assess(
            RideOffer(20.0, 1.0, 10.0, -2.0, 20.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("time", ignoreCase = true)
            }
        )
    }

    @Test
    fun nonFiniteOfferMetricIsRejected() {
        val result = engine.assess(
            RideOffer(Double.NaN, 1.0, 10.0, 5.0, 20.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("finite", ignoreCase = true)
            }
        )
    }

    @Test
    fun negativeRuleThresholdIsRejected() {
        val invalidRules = rules.copy(
            considerMinEurPerKm = -0.1,
        )

        val result = engine.assess(
            RideOffer(20.0, 1.0, 10.0, 5.0, 20.0),
            invalidRules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidRules>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("negative", ignoreCase = true)
            }
        )
    }

    @Test
    fun acceptThresholdMustNotBeLowerThanConsiderThreshold() {
        val invalidRules = rules.copy(
            acceptMinEurPerHour = 15.0,
            considerMinEurPerHour = 20.0,
        )

        val result = engine.assess(
            RideOffer(20.0, 1.0, 10.0, 5.0, 20.0),
            invalidRules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidRules>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("accept EUR/h", ignoreCase = true)
            }
        )
    }

    @Test
    fun zeroTotalDistanceIsRejected() {
        val result = engine.assess(
            RideOffer(20.0, 0.0, 0.0, 5.0, 20.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("distance", ignoreCase = true)
            }
        )
    }

    @Test
    fun zeroTotalTimeIsRejected() {
        val result = engine.assess(
            RideOffer(20.0, 1.0, 10.0, 0.0, 0.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(
            invalid.reasons.any {
                it.contains("time", ignoreCase = true)
            }
        )
    }
}
