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
        assertEquals(30.0, assessed.assessment.priceEur)
        assertEquals(15.0, assessed.assessment.totalKilometers)
        assertEquals(30.0, assessed.assessment.totalMinutes)
        assertEquals(0.0, assessed.assessment.emptyReturnKilometers)
        assertEquals(0.0, assessed.assessment.emptyReturnMinutes)
        assertEquals(
            RideCalculationScope.PICKUP_AND_TRIP,
            assessed.assessment.calculationScope,
        )
        assertEquals(2.0, assessed.assessment.eurPerKm)
        assertEquals(60.0, assessed.assessment.eurPerHour)
        assertEquals(
            RideDecisionReason.MEETS_ACCEPT_THRESHOLDS,
            assessed.assessment.explanation.reason,
        )
        assertEquals(1.50, assessed.assessment.explanation.thresholdEurPerKm)
        assertEquals(30.0, assessed.assessment.explanation.thresholdEurPerHour)
        assertTrue(assessed.assessment.explanation.failedMetrics.isEmpty())
    }

    @Test
    fun considersBorderlineOffer() {
        val result = engine.assess(
            RideOffer(12.0, 2.0, 8.0, 10.0, 20.0),
            rules,
        )
        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(RideRecommendation.CONSIDER, assessed.assessment.recommendation)
        assertEquals(
            RideDecisionReason.MEETS_CONSIDER_THRESHOLDS,
            assessed.assessment.explanation.reason,
        )
        assertEquals(1.00, assessed.assessment.explanation.thresholdEurPerKm)
        assertEquals(20.0, assessed.assessment.explanation.thresholdEurPerHour)
        assertTrue(assessed.assessment.explanation.failedMetrics.isEmpty())
    }

    @Test
    fun skipsOfferThatMissesConfiguredTargets() {
        val result = engine.assess(
            RideOffer(10.0, 5.0, 10.0, 15.0, 30.0),
            rules,
        )
        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(RideRecommendation.SKIP, assessed.assessment.recommendation)
        assertEquals(
            RideDecisionReason.BELOW_CONSIDER_THRESHOLDS,
            assessed.assessment.explanation.reason,
        )
        assertEquals(
            listOf(
                RideProfitabilityMetric.EUR_PER_KM,
                RideProfitabilityMetric.EUR_PER_HOUR,
            ),
            assessed.assessment.explanation.failedMetrics,
        )
    }

    @Test
    fun skipExplanationNamesOnlyTheMetricThatMissedTheMinimum() {
        val result = engine.assess(
            RideOffer(20.0, 2.0, 8.0, 20.0, 50.0),
            rules,
        )

        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(RideRecommendation.SKIP, assessed.assessment.recommendation)
        assertEquals(
            listOf(RideProfitabilityMetric.EUR_PER_HOUR),
            assessed.assessment.explanation.failedMetrics,
        )
        assertEquals(1.00, assessed.assessment.explanation.thresholdEurPerKm)
        assertEquals(20.0, assessed.assessment.explanation.thresholdEurPerHour)
    }

    @Test
    fun explicitEmptyReturnIsIncludedAndCanDowngradeRecommendation() {
        val result = engine.assess(
            RideOffer(
                priceEur = 30.0,
                pickupKm = 2.0,
                tripKm = 13.0,
                pickupMinutes = 5.0,
                tripMinutes = 25.0,
                emptyReturnKm = 10.0,
                emptyReturnMinutes = 30.0,
            ),
            rules,
        )

        val assessed = assertIs<RideAssessmentResult.Assessed>(result)
        assertEquals(25.0, assessed.assessment.totalKilometers)
        assertEquals(60.0, assessed.assessment.totalMinutes)
        assertEquals(10.0, assessed.assessment.emptyReturnKilometers)
        assertEquals(30.0, assessed.assessment.emptyReturnMinutes)
        assertEquals(
            RideCalculationScope.PICKUP_TRIP_AND_EMPTY_RETURN,
            assessed.assessment.calculationScope,
        )
        assertEquals(1.2, assessed.assessment.eurPerKm)
        assertEquals(30.0, assessed.assessment.eurPerHour)
        assertEquals(
            RideRecommendation.CONSIDER,
            assessed.assessment.recommendation,
        )
    }

    @Test
    fun partialEmptyReturnIsNotGuessed() {
        val result = engine.assess(
            RideOffer(
                priceEur = 30.0,
                pickupKm = 2.0,
                tripKm = 13.0,
                pickupMinutes = 5.0,
                tripMinutes = 25.0,
                emptyReturnKm = 10.0,
                emptyReturnMinutes = null,
            ),
            rules,
        )

        val missing = assertIs<RideAssessmentResult.InsufficientData>(result)
        assertEquals(listOf("emptyReturnMinutes"), missing.missingFields)
    }

    @Test
    fun negativeEmptyReturnDistanceIsRejected() {
        val result = engine.assess(
            RideOffer(
                priceEur = 30.0,
                pickupKm = 2.0,
                tripKm = 13.0,
                pickupMinutes = 5.0,
                tripMinutes = 25.0,
                emptyReturnKm = -1.0,
                emptyReturnMinutes = 10.0,
            ),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(RideInvalidOfferReason.NEGATIVE_DISTANCE in invalid.reasons)
    }

    @Test
    fun negativeEmptyReturnTimeIsRejected() {
        val result = engine.assess(
            RideOffer(
                priceEur = 30.0,
                pickupKm = 2.0,
                tripKm = 13.0,
                pickupMinutes = 5.0,
                tripMinutes = 25.0,
                emptyReturnKm = 5.0,
                emptyReturnMinutes = -10.0,
            ),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(RideInvalidOfferReason.NEGATIVE_TIME in invalid.reasons)
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
        assertTrue(RideInvalidOfferReason.NEGATIVE_DISTANCE in invalid.reasons)
    }

    @Test
    fun negativeIndividualTimeIsRejectedEvenWhenTotalWouldStayPositive() {
        val result = engine.assess(
            RideOffer(20.0, 1.0, 10.0, -2.0, 20.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(RideInvalidOfferReason.NEGATIVE_TIME in invalid.reasons)
    }

    @Test
    fun nonFiniteOfferMetricIsRejected() {
        val result = engine.assess(
            RideOffer(Double.NaN, 1.0, 10.0, 5.0, 20.0),
            rules,
        )

        val invalid = assertIs<RideAssessmentResult.InvalidOffer>(result)
        assertTrue(RideInvalidOfferReason.NON_FINITE_METRIC in invalid.reasons)
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
        assertTrue(RideInvalidRuleReason.NEGATIVE_THRESHOLD in invalid.reasons)
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
            RideInvalidRuleReason.ACCEPT_HOUR_BELOW_CONSIDER in invalid.reasons
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
            RideInvalidOfferReason.NON_POSITIVE_TOTAL_DISTANCE in invalid.reasons
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
            RideInvalidOfferReason.NON_POSITIVE_TOTAL_TIME in invalid.reasons
        )
    }
}
