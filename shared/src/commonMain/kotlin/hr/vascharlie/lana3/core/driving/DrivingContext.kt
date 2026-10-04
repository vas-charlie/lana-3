package hr.vascharlie.lana3.core.driving

enum class DrivingState {
    DRIVING,
    PARKED,
    BREAK,
    OUT_OF_VEHICLE,
    UNKNOWN,
}

enum class InteractionDetail {
    MINIMAL,
    NORMAL,
    DETAILED,
}

data class DrivingInteractionPolicy(
    val speechDetail: InteractionDetail,
    val screenDetail: InteractionDetail,
    val touchInteractionAllowed: Boolean,
    val reason: String,
)

/**
 * Device-independent behavior policy derived only from reported driving state.
 * UNKNOWN is treated conservatively; the core does not infer that the vehicle is parked.
 */
class DrivingContextPolicy {
    fun forState(state: DrivingState): DrivingInteractionPolicy = when (state) {
        DrivingState.DRIVING -> DrivingInteractionPolicy(
            speechDetail = InteractionDetail.MINIMAL,
            screenDetail = InteractionDetail.MINIMAL,
            touchInteractionAllowed = false,
            reason = "Driving: keep speech and screen concise and avoid touch interaction.",
        )
        DrivingState.PARKED -> DrivingInteractionPolicy(
            speechDetail = InteractionDetail.DETAILED,
            screenDetail = InteractionDetail.DETAILED,
            touchInteractionAllowed = true,
            reason = "Parked: detailed interaction is allowed.",
        )
        DrivingState.BREAK -> DrivingInteractionPolicy(
            speechDetail = InteractionDetail.NORMAL,
            screenDetail = InteractionDetail.DETAILED,
            touchInteractionAllowed = true,
            reason = "Break: detailed screen interaction is allowed.",
        )
        DrivingState.OUT_OF_VEHICLE -> DrivingInteractionPolicy(
            speechDetail = InteractionDetail.NORMAL,
            screenDetail = InteractionDetail.DETAILED,
            touchInteractionAllowed = true,
            reason = "Out of vehicle: normal interaction is allowed.",
        )
        DrivingState.UNKNOWN -> DrivingInteractionPolicy(
            speechDetail = InteractionDetail.MINIMAL,
            screenDetail = InteractionDetail.MINIMAL,
            touchInteractionAllowed = false,
            reason = "Driving state is unknown; use conservative interaction.",
        )
    }
}
