package hr.vascharlie.lana3.core.authorization

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExecutionModeGateTest {
    private val gate = ExecutionModeGate()

    @Test
    fun observeModeNeverExecutesEvenAfterConfirmation() {
        val result = gate.evaluate(
            mode = AutomationMode.OBSERVE,
            userConfirmed = true,
        )

        val blocked = assertIs<ExecutionModeGateResult.Blocked>(result)
        assertFalse(blocked.requiresConfirmation)
    }

    @Test
    fun suggestModeNeverExecutes() {
        val result = gate.evaluate(AutomationMode.SUGGEST)

        val blocked = assertIs<ExecutionModeGateResult.Blocked>(result)
        assertEquals("Suggest mode never executes actions.", blocked.reason)
    }

    @Test
    fun confirmModeRequiresExplicitConfirmation() {
        val before = gate.evaluate(AutomationMode.CONFIRM)
        val after = gate.evaluate(
            mode = AutomationMode.CONFIRM,
            userConfirmed = true,
        )

        val blocked = assertIs<ExecutionModeGateResult.Blocked>(before)
        assertTrue(blocked.requiresConfirmation)
        assertIs<ExecutionModeGateResult.Allowed>(after)
    }

    @Test
    fun executeModeAllowsNormalActionWithoutConfirmation() {
        val result = gate.evaluate(AutomationMode.EXECUTE)

        assertIs<ExecutionModeGateResult.Allowed>(result)
    }

    @Test
    fun executeModeStillHonorsForcedConfirmation() {
        val before = gate.evaluate(
            mode = AutomationMode.EXECUTE,
            forceConfirmation = true,
        )
        val after = gate.evaluate(
            mode = AutomationMode.EXECUTE,
            userConfirmed = true,
            forceConfirmation = true,
        )

        val blocked = assertIs<ExecutionModeGateResult.Blocked>(before)
        assertTrue(blocked.requiresConfirmation)
        assertIs<ExecutionModeGateResult.Allowed>(after)
    }
}
