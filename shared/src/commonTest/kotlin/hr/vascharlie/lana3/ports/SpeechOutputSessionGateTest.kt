package hr.vascharlie.lana3.ports

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SpeechOutputSessionGateTest {
    @Test
    fun oldUtteranceCannotStartOrCompleteNewSession() {
        val gate = SpeechOutputSessionGate()
        val first = mutableListOf<SpeechOutputEvent>()
        val second = mutableListOf<SpeechOutputEvent>()

        gate.begin("first", first::add)
        assertTrue(gate.dispatch("first", SpeechOutputEvent.Started))

        gate.begin("second", second::add)
        assertFalse(gate.dispatch("first", SpeechOutputEvent.Completed))
        assertFalse(gate.dispatch("first", SpeechOutputEvent.RangeStarted(0, 3)))
        assertFalse(gate.dispatch("first", SpeechOutputEvent.Started))
        assertTrue(gate.dispatch("second", SpeechOutputEvent.Started))
        assertTrue(gate.dispatch("second", SpeechOutputEvent.Completed))

        assertEquals(listOf(SpeechOutputEvent.Started), first)
        assertEquals(
            listOf(SpeechOutputEvent.Started, SpeechOutputEvent.Completed),
            second,
        )
    }

    @Test
    fun cancelDropsQueuedCallbacksAndAllowsNewSession() {
        val gate = SpeechOutputSessionGate()
        val received = mutableListOf<SpeechOutputEvent>()
        gate.begin("old", received::add)
        gate.cancel()

        assertFalse(gate.dispatch("old", SpeechOutputEvent.Completed))
        assertFalse(
            gate.dispatch(
                "old",
                SpeechOutputEvent.Error("late", "Cancelled utterance error."),
            )
        )
        gate.begin("new", received::add)
        assertTrue(gate.dispatch("new", SpeechOutputEvent.Started))
        assertEquals(listOf(SpeechOutputEvent.Started), received)
    }

    @Test
    fun terminalEventDeliveredOnlyOnce() {
        val gate = SpeechOutputSessionGate()
        val received = mutableListOf<SpeechOutputEvent>()
        gate.begin("1", received::add)

        assertTrue(gate.dispatch("1", SpeechOutputEvent.Completed))
        assertFalse(gate.dispatch("1", SpeechOutputEvent.Completed))
        assertFalse(gate.dispatch("1", SpeechOutputEvent.Started))
        assertEquals(listOf(SpeechOutputEvent.Completed), received)
    }

    @Test
    fun errorEndsSessionAndSuppressesLateCompletion() {
        val gate = SpeechOutputSessionGate()
        val received = mutableListOf<SpeechOutputEvent>()
        val error = SpeechOutputEvent.Error("tts_failed", "Synthesis failed.")
        gate.begin("1", received::add)

        assertTrue(gate.dispatch("1", error))
        assertFalse(gate.dispatch("1", SpeechOutputEvent.Completed))
        assertEquals(listOf(error), received)
    }

    @Test
    fun progressBelongsToMatchingSessionOnly() {
        val gate = SpeechOutputSessionGate()
        val received = mutableListOf<SpeechOutputEvent>()
        val progress = SpeechOutputEvent.RangeStarted(2, 6)
        gate.begin("new", received::add)

        assertFalse(gate.dispatch(null, progress))
        assertFalse(gate.dispatch("old", progress))
        assertTrue(gate.dispatch("new", progress))
        assertEquals(listOf(progress), received)
    }

    @Test
    fun blankUtteranceIdIsRejected() {
        val gate = SpeechOutputSessionGate()
        assertFailsWith<IllegalArgumentException> {
            gate.begin("   ") { }
        }
        assertFalse(gate.dispatch("   ", SpeechOutputEvent.Started))
    }
}
