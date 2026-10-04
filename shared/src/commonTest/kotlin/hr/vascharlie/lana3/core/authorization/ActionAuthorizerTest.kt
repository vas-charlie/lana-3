package hr.vascharlie.lana3.core.authorization

import hr.vascharlie.lana3.core.decision.Decision
import hr.vascharlie.lana3.core.intent.Intent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ActionAuthorizerTest {
    private val authorizer = ActionAuthorizer()

    @Test
    fun executeDecisionProducesAuthorizedAction() {
        val intent = Intent.SaveNote("test")
        val result = authorizer.authorize(
            intent,
            Decision.Proposed("validated", AuthorizationLevel.EXECUTE),
        )

        assertEquals(
            ActionAuthorizationResult.Authorized(
                AuthorizedAction(intent, AuthorizationLevel.EXECUTE)
            ),
            result,
        )
    }

    @Test
    fun suggestDecisionCannotCrossExecutionBoundary() {
        val result = authorizer.authorize(
            Intent.TaxiProfitability(10.0, 5.0),
            Decision.Proposed("suggest only", AuthorizationLevel.SUGGEST),
        )

        assertTrue(result is ActionAuthorizationResult.Rejected)
    }

    @Test
    fun cannotDecideCannotCrossExecutionBoundary() {
        val result = authorizer.authorize(
            Intent.Unknown("unknown"),
            Decision.CannotDecide("Intent is unknown"),
        )

        assertTrue(result is ActionAuthorizationResult.Rejected)
    }
}
