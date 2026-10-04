package hr.vascharlie.lana3.core.driving

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DrivingContextPolicyTest {
    private val policy = DrivingContextPolicy()

    @Test
    fun drivingUsesMinimalHandsFreeInteraction() {
        val result = policy.forState(DrivingState.DRIVING)

        assertEquals(InteractionDetail.MINIMAL, result.speechDetail)
        assertEquals(InteractionDetail.MINIMAL, result.screenDetail)
        assertFalse(result.touchInteractionAllowed)
    }

    @Test
    fun parkedAllowsDetailedInteraction() {
        val result = policy.forState(DrivingState.PARKED)

        assertEquals(InteractionDetail.DETAILED, result.speechDetail)
        assertEquals(InteractionDetail.DETAILED, result.screenDetail)
        assertTrue(result.touchInteractionAllowed)
    }

    @Test
    fun breakAndOutOfVehicleAreExplicitStates() {
        assertEquals(
            InteractionDetail.DETAILED,
            policy.forState(DrivingState.BREAK).screenDetail,
        )
        assertEquals(
            InteractionDetail.DETAILED,
            policy.forState(DrivingState.OUT_OF_VEHICLE).screenDetail,
        )
    }

    @Test
    fun unknownStateDoesNotAssumeParked() {
        val result = policy.forState(DrivingState.UNKNOWN)

        assertEquals(InteractionDetail.MINIMAL, result.speechDetail)
        assertEquals(InteractionDetail.MINIMAL, result.screenDetail)
        assertFalse(result.touchInteractionAllowed)
    }
}
