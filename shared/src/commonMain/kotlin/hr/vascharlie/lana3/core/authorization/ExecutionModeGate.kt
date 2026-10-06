package hr.vascharlie.lana3.core.authorization

enum class AutomationMode {
    OBSERVE,
    SUGGEST,
    CONFIRM,
    EXECUTE,
}

sealed interface ExecutionModeGateResult {
    data object Allowed : ExecutionModeGateResult

    data class Blocked(
        val reason: String,
        val requiresConfirmation: Boolean = false,
    ) : ExecutionModeGateResult
}

/**
 * Global automation-mode guard for side-effecting actions.
 *
 * OBSERVE and SUGGEST never execute actions.
 * CONFIRM executes only after an explicit user confirmation.
 * EXECUTE may run directly unless a specific action forces confirmation.
 */
class ExecutionModeGate {
    fun evaluate(
        mode: AutomationMode,
        userConfirmed: Boolean = false,
        forceConfirmation: Boolean = false,
    ): ExecutionModeGateResult = when (mode) {
        AutomationMode.OBSERVE -> ExecutionModeGateResult.Blocked(
            reason = "Observe mode never executes actions.",
        )

        AutomationMode.SUGGEST -> ExecutionModeGateResult.Blocked(
            reason = "Suggest mode never executes actions.",
        )

        AutomationMode.CONFIRM -> {
            if (userConfirmed) {
                ExecutionModeGateResult.Allowed
            } else {
                ExecutionModeGateResult.Blocked(
                    reason = "User confirmation is required.",
                    requiresConfirmation = true,
                )
            }
        }

        AutomationMode.EXECUTE -> {
            if (forceConfirmation && !userConfirmed) {
                ExecutionModeGateResult.Blocked(
                    reason = "This action requires explicit confirmation.",
                    requiresConfirmation = true,
                )
            } else {
                ExecutionModeGateResult.Allowed
            }
        }
    }
}
