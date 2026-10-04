package hr.vascharlie.lana3.core.decision

import hr.vascharlie.lana3.core.authorization.AuthorizationLevel
import hr.vascharlie.lana3.core.context.LanaContext
import hr.vascharlie.lana3.core.intent.Intent
import hr.vascharlie.lana3.core.model.LanguageContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DecisionEngineTest {
    private val context = LanaContext(LanguageContext("hr-HR"))

    @Test
    fun unknownIntentDoesNotGuess() {
        val decision = DecisionEngine().evaluate(Intent.Unknown("nejasno"), context)
        assertIs<Decision.CannotDecide>(decision)
    }

    @Test
    fun taxiCalculationUsesTotalKilometers() {
        val decision = DecisionEngine().evaluate(
            Intent.TaxiProfitability(revenue = 25.72, totalKilometers = 31.0),
            context,
        )
        val proposed = assertIs<Decision.Proposed>(decision)
        assertEquals(AuthorizationLevel.SUGGEST, proposed.requiredAuthorization)
        assertEquals("Nominal revenue per total kilometer is 0.83 EUR/km.", proposed.explanation)
    }

    @Test
    fun unicodeLanguageContextIsNotRestrictedToCroatian() {
        val arabicContext = LanaContext(LanguageContext("ar"))
        val decision = DecisionEngine().evaluate(Intent.SaveNote("مرحبا"), arabicContext)
        assertIs<Decision.Proposed>(decision)
    }
}
