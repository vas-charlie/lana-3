package hr.vascharlie.lana3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Lightweight local avatar life renderer.
 *
 * It owns visual micro-motion only. Semantic state still comes from the app.
 * Motions are intentionally subtle so Lana feels present without distracting
 * Charlie while driving.
 */
class LanaAvatarMotionController(
    private val avatarView: View,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var ambientMotion: AnimatorSet? = null
    private var currentState: LanaVisualState? = null
    private var gazeDirection = 1f

    private val idleBlink = object : Runnable {
        override fun run() {
            if (currentState != LanaVisualState.IDLE) return
            blink()
            handler.postDelayed(this, IDLE_BLINK_INTERVAL_MS)
        }
    }

    private val idleGaze = object : Runnable {
        override fun run() {
            if (currentState != LanaVisualState.IDLE) return
            gazeDirection *= -1f
            val distance = dp(IDLE_GAZE_DP) * gazeDirection
            avatarView.animate()
                .translationX(distance)
                .rotation(gazeDirection * IDLE_HEAD_TILT_DEGREES)
                .setDuration(IDLE_GAZE_DURATION_MS)
                .withEndAction {
                    if (currentState == LanaVisualState.IDLE) {
                        avatarView.animate()
                            .translationX(0f)
                            .rotation(0f)
                            .setDuration(IDLE_GAZE_RETURN_MS)
                            .start()
                    }
                }
                .start()
            handler.postDelayed(this, IDLE_GAZE_INTERVAL_MS)
        }
    }

    private val idleGlassesGesture = object : Runnable {
        override fun run() {
            if (currentState != LanaVisualState.IDLE) return
            adjustGlassesGesture()
            handler.postDelayed(this, IDLE_GLASSES_INTERVAL_MS)
        }
    }

    fun applyState(state: LanaVisualState) {
        stop()
        currentState = state

        avatarView.alpha = 1f
        avatarView.scaleX = 1f
        avatarView.scaleY = 1f
        avatarView.translationX = 0f
        avatarView.translationY = 0f
        avatarView.rotation = 0f

        when (state) {
            LanaVisualState.IDLE -> startIdleLife()
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
        handler.removeCallbacks(idleBlink)
        handler.removeCallbacks(idleGaze)
        handler.removeCallbacks(idleGlassesGesture)
        avatarView.animate().cancel()
    }

    private fun startIdleLife() {
        startBreathing(IDLE_SCALE_FACTOR, IDLE_BREATH_DURATION_MS)
        handler.postDelayed(idleBlink, 1_700L)
        handler.postDelayed(idleGaze, 3_800L)
        handler.postDelayed(idleGlassesGesture, 8_500L)
    }

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
        val tilt = ObjectAnimator.ofFloat(
            avatarView,
            View.ROTATION,
            0f,
            WRITING_TILT_DEGREES,
            0f,
        ).apply {
            duration = WRITING_DURATION_MS
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        ambientMotion = AnimatorSet().apply {
            playTogether(down, tilt)
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

    private fun blink() {
        val blink = ObjectAnimator.ofFloat(
            avatarView,
            View.SCALE_Y,
            avatarView.scaleY,
            avatarView.scaleY * BLINK_SCALE_Y,
            avatarView.scaleY,
        ).apply {
            duration = BLINK_DURATION_MS
        }
        blink.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (currentState == LanaVisualState.IDLE) {
                    avatarView.scaleY = 1f
                }
            }
        })
        blink.start()
    }

    private fun adjustGlassesGesture() {
        avatarView.animate()
            .rotation(-GLASSES_TILT_DEGREES)
            .translationY(-dp(GLASSES_LIFT_DP))
            .setDuration(GLASSES_GESTURE_HALF_MS)
            .withEndAction {
                if (currentState == LanaVisualState.IDLE) {
                    avatarView.animate()
                        .rotation(0f)
                        .translationY(0f)
                        .setDuration(GLASSES_GESTURE_HALF_MS)
                        .start()
                }
            }
            .start()
    }

    private fun dp(value: Int): Float =
        value * avatarView.resources.displayMetrics.density

    private companion object {
        const val IDLE_BREATH_DURATION_MS = 3_400L
        const val IDLE_SCALE_FACTOR = 1.012f
        const val IDLE_BLINK_INTERVAL_MS = 5_300L
        const val BLINK_DURATION_MS = 130L
        const val BLINK_SCALE_Y = 0.985f

        const val IDLE_GAZE_DP = 3
        const val IDLE_HEAD_TILT_DEGREES = 0.35f
        const val IDLE_GAZE_DURATION_MS = 650L
        const val IDLE_GAZE_RETURN_MS = 900L
        const val IDLE_GAZE_INTERVAL_MS = 7_600L

        const val IDLE_GLASSES_INTERVAL_MS = 16_000L
        const val GLASSES_TILT_DEGREES = 0.7f
        const val GLASSES_LIFT_DP = 1
        const val GLASSES_GESTURE_HALF_MS = 420L

        const val LISTENING_BREATH_DURATION_MS = 2_600L
        const val LISTENING_SCALE_FACTOR = 1.016f

        const val THINKING_DRIFT_DP = 2
        const val THINKING_DURATION_MS = 2_800L

        const val SPEAKING_BREATH_DURATION_MS = 1_900L
        const val SPEAKING_SCALE_FACTOR = 1.018f

        const val WRITING_DRIFT_DP = 3
        const val WRITING_TILT_DEGREES = 0.45f
        const val WRITING_DURATION_MS = 2_200L
    }
}
