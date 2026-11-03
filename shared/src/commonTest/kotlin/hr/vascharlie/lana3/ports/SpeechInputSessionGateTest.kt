package hr.vascharlie.lana3.ports

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpeechInputSessionGateTest {
    @Test
    fun previousSessionCannotDeliverResultAfterRestart() {
        val gate = SpeechInputSessionGate()
        val old = mutableListOf<SpeechInputEvent>()
        val current = mutableListOf<SpeechInputEvent>()
        val first = gate.begin(old::add)
        val second = gate.begin(current::add)
        val late = SpeechInputEvent.FinalTranscript("stari rezultat")
        val fresh = SpeechInputEvent.FinalTranscript("novi rezultat")

        assertFalse(gate.dispatch(first, late))
        assertTrue(gate.dispatch(second, fresh))
        assertEquals<List<SpeechInputEvent>>(emptyList(), old)
        assertEquals<List<SpeechInputEvent>>(listOf(fresh), current)
    }

    @Test
    fun cancellationDropsLatePartialAndError() {
        val gate = SpeechInputSessionGate()
        val received = mutableListOf<SpeechInputEvent>()
        val token = gate.begin(received::add)
        gate.cancel()

        assertFalse(gate.dispatch(token, SpeechInputEvent.PartialTranscript("kasno")))
        assertFalse(gate.dispatch(token, SpeechInputEvent.Error("cancelled", "late", true)))
        assertTrue(received.isEmpty())
    }

    @Test
    fun terminalResultIsDeliveredOnlyOnce() {
        val gate = SpeechInputSessionGate()
        val received = mutableListOf<SpeechInputEvent>()
        val token = gate.begin(received::add)
        val final = SpeechInputEvent.FinalTranscript("gotovo")

        assertTrue(gate.dispatch(token, final))
        assertFalse(gate.dispatch(token, final))
        assertFalse(gate.dispatch(token, SpeechInputEvent.PartialTranscript("kasno")))
        assertEquals<List<SpeechInputEvent>>(listOf(final), received)
    }

    @Test
    fun oldErrorCannotTerminateNewSession() {
        val gate = SpeechInputSessionGate()
        val old = gate.begin { }
        val received = mutableListOf<SpeechInputEvent>()
        val current = gate.begin(received::add)

        assertFalse(gate.dispatch(old, SpeechInputEvent.Error("network", "late", true)))
        assertTrue(gate.dispatch(current, SpeechInputEvent.ListeningStarted))
        assertEquals<List<SpeechInputEvent>>(listOf(SpeechInputEvent.ListeningStarted), received)
    }
}
