package hr.vascharlie.lana3.core.decision

import hr.vascharlie.lana3.core.authorization.AuthorizationLevel

sealed interface Decision {
    data class Proposed(
        val explanation: String,
        val requiredAuthorization: AuthorizationLevel,
    ) : Decision

    data class CannotDecide(
        val reason: String,
    ) : Decision
}
