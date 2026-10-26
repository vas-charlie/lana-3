package hr.vascharlie.lana3.core.authorization

enum class AutomationMode {
    OBSERVE,
    SUGGEST,
    CONFIRM,
    EXECUTE,
}

enum class ExecutionModeBlockReason {
    OBSERVE_MODE,
    SUGGEST_MODE,
    USER_CONFIRMATION_REQUIRED,
    ACTION_CONFIRMATION_REQUIRED,
}

sealed interface ExecutionModeGateResult {
    data object Allowed : ExecutionModeGateResult

    data class Blocked(
        val reason: ExecutionModeBlockReason,
        val requiresConfirmation: Boolean = false,
    ) : ExecutionModeGateResult
}

/**
 * Global automation-mode guard for side-effecting actions.
 *
 * OBSERVE and SUGGEST never execute actions.
 * CONFIRM executes only after an explicit user confirmation.
 * EXECUTE may run directly unless a specific action forces confirmation.
 *
 * The shared gate returns semantic reason codes. Human-language wording belongs
 * at the presentation boundary.
 */
class ExecutionModeGate {
    fun evaluate(
        mode: AutomationMode,
        userConfirmed: Boolean = false,
        forceConfirmation: Boolean = false,
    ): ExecutionModeGateResult = when (mode) {
        AutomationMode.OBSERVE -> ExecutionModeGateResult.Blocked(
            reason = ExecutionModeBlockReason.OBSERVE_MODE,
        )

        AutomationMode.SUGGEST -> ExecutionModeGateResult.Blocked(
            reason = ExecutionModeBlockReason.SUGGEST_MODE,
        )

        AutomationMode.CONFIRM -> {
            if (userConfirmed) {
                ExecutionModeGateResult.Allowed
            } else {
                ExecutionModeGateResult.Blocked(
                    reason = ExecutionModeBlockReason.USER_CONFIRMATION_REQUIRED,
                    requiresConfirmation = true,
                )
            }
        }

        AutomationMode.EXECUTE -> {
            if (forceConfirmation && !userConfirmed) {
                ExecutionModeGateResult.Blocked(
                    reason = ExecutionModeBlockReason.ACTION_CONFIRMATION_REQUIRED,
                    requiresConfirmation = true,
                )
            } else {
                ExecutionModeGateResult.Allowed
            }
        }
    }
}
