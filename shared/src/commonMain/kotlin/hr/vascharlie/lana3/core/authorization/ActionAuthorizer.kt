package hr.vascharlie.lana3.core.authorization

import hr.vascharlie.lana3.core.decision.Decision
import hr.vascharlie.lana3.core.intent.Intent

data class AuthorizedAction(
    val intent: Intent,
    val authorization: AuthorizationLevel,
)

sealed interface ActionAuthorizationResult {
    data class Authorized(val action: AuthorizedAction) : ActionAuthorizationResult
    data class Rejected(val reason: String) : ActionAuthorizationResult
}

/**
 * Converts a validated decision into an executable action. Execution layers receive only
 * AuthorizedAction, never a raw intent, preserving the brain/hands authorization boundary.
 */
class ActionAuthorizer {
    fun authorize(intent: Intent, decision: Decision): ActionAuthorizationResult = when (decision) {
        is Decision.CannotDecide -> ActionAuthorizationResult.Rejected(decision.reason)
        is Decision.Proposed -> if (decision.requiredAuthorization == AuthorizationLevel.EXECUTE) {
            ActionAuthorizationResult.Authorized(
                AuthorizedAction(intent, AuthorizationLevel.EXECUTE)
            )
        } else {
            ActionAuthorizationResult.Rejected(
                "Action is not authorized for execution at level " + decision.requiredAuthorization + "."
            )
        }
    }
}
