package hr.vascharlie.lana3

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Lightweight local avatar micro-motion.
 *
 * This controller owns visual motion only. It does not decide semantic state,
 * trigger actions, or depend on network/AI services.
 */
class LanaAvatarMotionController(
    private val avatarView: View,
) {
    private var idleBreathing: AnimatorSet? = null

    fun applyState(state: LanaVisualState) {
        stop()

        if (state == LanaVisualState.IDLE) {
            startIdleBreathing()
        }
    }

    fun stop() {
        idleBreathing?.cancel()
        idleBreathing = null
    }

    private fun startIdleBreathing() {
        val baseScaleX = avatarView.scaleX
        val baseScaleY = avatarView.scaleY

        val breatheX = ObjectAnimator.ofFloat(
            avatarView,
            View.SCALE_X,
            baseScaleX,
            baseScaleX * IDLE_SCALE_FACTOR,
            baseScaleX,
        )
        val breatheY = ObjectAnimator.ofFloat(
            avatarView,
            View.SCALE_Y,
            baseScaleY,
            baseScaleY * IDLE_SCALE_FACTOR,
            baseScaleY,
        )

        breatheX.duration = IDLE_BREATH_DURATION_MS
        breatheY.duration = IDLE_BREATH_DURATION_MS
        breatheX.repeatCount = ObjectAnimator.INFINITE
        breatheY.repeatCount = ObjectAnimator.INFINITE
        breatheX.interpolator = AccelerateDecelerateInterpolator()
        breatheY.interpolator = AccelerateDecelerateInterpolator()

        idleBreathing = AnimatorSet().apply {
            playTogether(breatheX, breatheY)
            start()
        }
    }

    private companion object {
        const val IDLE_BREATH_DURATION_MS = 3_200L
        const val IDLE_SCALE_FACTOR = 1.012f
    }
}
