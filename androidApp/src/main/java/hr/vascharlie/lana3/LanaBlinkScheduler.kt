package hr.vascharlie.lana3

import android.os.Handler
import android.os.Looper
import kotlin.random.Random

/**
 * Schedules natural local blink events independently from semantic avatar state.
 *
 * The scheduler owns timing only. A future renderer supplies the actual eyelid
 * animation through [onBlink], keeping timing separate from the avatar asset.
 */
class LanaBlinkScheduler(
    private val onBlink: () -> Unit,
    private val handler: Handler = Handler(Looper.getMainLooper()),
    private val random: Random = Random.Default,
) {
    private var running = false

    private val blinkTask = object : Runnable {
        override fun run() {
            if (!running) return

            onBlink()
            scheduleNext()
        }
    }

    fun start() {
        if (running) return
        running = true
        scheduleNext()
    }

    fun stop() {
        running = false
        handler.removeCallbacks(blinkTask)
    }

    private fun scheduleNext() {
        if (!running) return
        handler.postDelayed(blinkTask, nextDelayMs())
    }

    private fun nextDelayMs(): Long =
        random.nextLong(MIN_BLINK_INTERVAL_MS, MAX_BLINK_INTERVAL_MS + 1)

    private companion object {
        const val MIN_BLINK_INTERVAL_MS = 2400L
        const val MAX_BLINK_INTERVAL_MS = 6200L
    }
}
