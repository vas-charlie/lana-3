package hr.vascharlie.lana3

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Lightweight local avatar motion controller.
 *
 * This layer owns visual micro-movements only. It deliberately does not decide
 * LANA's semantic state and does not depend on network or AI services.
 */
class LanaAvatarMotionController(
    private val avatarView: View,
) {
    private var breathingAnimator: AnimatorSet? = null

    fun applyState(state: LanaVisualState) {
        stop()
        resetTransform()

        if (state == LanaVisualState.IDLE) {
            startBreathing()
        }
    }

    fun stop() {
        breathingAnimator?.cancel()
        breathingAnimator = null
    }

    private fun resetTransform() {
        avatarView.scaleX = 1f
        avatarView.scaleY = 1f
    }

    private fun startBreathing() {
        val breatheX = ObjectAnimator.ofFloat(avatarView, View.SCALE_X, 1f, 1.012f, 1f)
        val breatheY = ObjectAnimator.ofFloat(avatarView, View.SCALE_Y, 1f, 1.012f, 1f)

        breatheX.duration = BREATH_DURATION_MS
        breatheY.duration = BREATH_DURATION_MS
        breatheX.repeatCount = ObjectAnimator.INFINITE
        breatheY.repeatCount = ObjectAnimator.INFINITE
        breatheX.interpolator = AccelerateDecelerateInterpolator()
        breatheY.interpolator = AccelerateDecelerateInterpolator()

        breathingAnimator = AnimatorSet().apply {
            playTogether(breatheX, breatheY)
            start()
        }
    }

    private companion object {
        const val BREATH_DURATION_MS = 3200L
    }
}
