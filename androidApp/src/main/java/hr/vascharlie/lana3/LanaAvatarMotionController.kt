package hr.vascharlie.lana3

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Lightweight fallback renderer for the current single-image Lana asset.
 *
 * IMPORTANT: a single flat portrait cannot truthfully blink, move its eyes,
 * articulate a mouth or lip-sync. This controller therefore animates only
 * whole-body presence that the asset can actually represent. Facial channels
 * belong to the richer avatar renderer.
 */
class LanaAvatarMotionController(
    private val avatarView: View,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var ambientMotion: AnimatorSet? = null
    private var currentState: LanaVisualState? = null

    fun applyState(state: LanaVisualState) {
        stop()
        currentState = state
        resetTransform()

        when (state) {
            LanaVisualState.IDLE -> startBreathing(IDLE_SCALE_FACTOR, IDLE_BREATH_DURATION_MS)
            LanaVisualState.LISTENING -> startListeningPresence()
            LanaVisualState.THINKING -> startThinkingPresence()
            LanaVisualState.SPEAKING -> startSpeakingPresence()
            LanaVisualState.OFFLINE,
            LanaVisualState.ERROR -> startWritingPresence()
        }
    }

    fun stop() {
        currentState = null
        ambientMotion?.cancel()
        ambientMotion = null
        handler.removeCallbacksAndMessages(null)
        avatarView.animate().cancel()
    }

    private fun resetTransform() {
        avatarView.alpha = 1f
        avatarView.scaleX = 1f
        avatarView.scaleY = 1f
        avatarView.translationX = 0f
        avatarView.translationY = 0f
        avatarView.rotation = 0f
    }

    /**
     * Charlie speaking -> Lana listens.
     *
     * Keep the portrait visually steady. Do not fake mouth movement, blinking,
     * gaze or head turns by translating/scaling the entire bitmap.
     */
    private fun startListeningPresence() {
        startBreathing(LISTENING_SCALE_FACTOR, LISTENING_BREATH_DURATION_MS)
    }

    private fun startThinkingPresence() {
        val drift = ObjectAnimator.ofFloat(
            avatarView,
            View.TRANSLATION_Y,
            0f,
            dp(THINKING_DRIFT_DP),
            0f,
        ).apply {
            duration = THINKING_DURATION_MS
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        ambientMotion = AnimatorSet().apply {
            playTogether(drift)
            start()
        }
    }

    /**
     * Lana speaking -> semantic SPEAKING state.
     *
     * The fallback portrait only breathes. Genuine lip-sync must be rendered
     * by an avatar asset with an independent mouth/facial channel.
     */
    private fun startSpeakingPresence() {
        startBreathing(SPEAKING_SCALE_FACTOR, SPEAKING_BREATH_DURATION_MS)
    }

    private fun startWritingPresence() {
        avatarView.alpha = 0.86f
        val down = ObjectAnimator.ofFloat(
            avatarView,
            View.TRANSLATION_Y,
            0f,
            dp(WRITING_DRIFT_DP),
            0f,
        ).apply {
            duration = WRITING_DURATION_MS
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        ambientMotion = AnimatorSet().apply {
            playTogether(down)
            start()
        }
    }

    private fun startBreathing(scaleFactor: Float, durationMs: Long) {
        val breatheX = ObjectAnimator.ofFloat(
            avatarView,
            View.SCALE_X,
            1f,
            scaleFactor,
            1f,
        ).apply {
            duration = durationMs
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        val breatheY = ObjectAnimator.ofFloat(
            avatarView,
            View.SCALE_Y,
            1f,
            scaleFactor,
            1f,
        ).apply {
            duration = durationMs
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        ambientMotion = AnimatorSet().apply {
            playTogether(breatheX, breatheY)
            start()
        }
    }

    private fun dp(value: Int): Float =
        value * avatarView.resources.displayMetrics.density

    private companion object {
        const val IDLE_BREATH_DURATION_MS = 3_800L
        const val IDLE_SCALE_FACTOR = 1.006f

        const val LISTENING_BREATH_DURATION_MS = 3_200L
        const val LISTENING_SCALE_FACTOR = 1.004f

        const val THINKING_DRIFT_DP = 1
        const val THINKING_DURATION_MS = 2_800L

        const val SPEAKING_BREATH_DURATION_MS = 2_600L
        const val SPEAKING_SCALE_FACTOR = 1.006f

        const val WRITING_DRIFT_DP = 1
        const val WRITING_DURATION_MS = 2_800L
    }
}
