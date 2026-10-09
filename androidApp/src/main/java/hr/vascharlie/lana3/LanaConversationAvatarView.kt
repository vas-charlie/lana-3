package hr.vascharlie.lana3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import hr.vascharlie.lana3.avatar.AvatarFrame
import hr.vascharlie.lana3.avatar.AvatarMode
import kotlin.math.sin

/**
 * Visible avatar renderer scaffold.
 *
 * This is intentionally vector-rendered rather than reusing the old flat portrait.
 * Facial channels are independent: eyelids, pupils, brows and mouth are drawn
 * separately and can later be driven by a production avatar asset/renderer.
 */
class LanaConversationAvatarView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var frame = AvatarFrame(AvatarMode.IDLE, hr.vascharlie.lana3.avatar.AvatarAttention.NEUTRAL)
    private var phase = 0f

    private val ticker = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2400L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            phase = it.animatedFraction
            invalidate()
        }
        start()
    }

    fun render(next: AvatarFrame) {
        frame = next
        invalidate()
    }

    override fun onDetachedFromWindow() {
        ticker.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val breathe = sin(phase * Math.PI * 2).toFloat() * h * 0.004f

        paint.color = Color.rgb(16, 31, 50)
        canvas.drawRoundRect(RectF(w * .08f, h * .04f, w * .92f, h * .98f), 36f, 36f, paint)

        // body / blazer
        paint.color = Color.rgb(38, 48, 63)
        canvas.drawOval(RectF(w * .20f, h * .55f + breathe, w * .80f, h * 1.10f + breathe), paint)
        paint.color = Color.rgb(225, 219, 210)
        canvas.drawPath(android.graphics.Path().apply {
            moveTo(cx, h * .58f)
            lineTo(w * .38f, h * .93f)
            lineTo(w * .62f, h * .93f)
            close()
        }, paint)

        // neck + face
        paint.color = Color.rgb(224, 181, 157)
        canvas.drawRoundRect(RectF(w * .43f, h * .47f + breathe, w * .57f, h * .66f + breathe), 28f, 28f, paint)
        canvas.drawOval(RectF(w * .30f, h * .12f + breathe, w * .70f, h * .62f + breathe), paint)

        // hair
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * .055f
        paint.color = Color.rgb(76, 49, 38)
        canvas.drawArc(RectF(w * .28f, h * .09f + breathe, w * .72f, h * .62f + breathe), 185f, 170f, false, paint)
        paint.style = Paint.Style.FILL

        val blink = if (phase > .965f) .12f else 1f
        drawEye(canvas, w * .405f, h * .34f + breathe, w, h, blink)
        drawEye(canvas, w * .595f, h * .34f + breathe, w, h, blink)

        // glasses are an independent overlay
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * .012f
        paint.color = Color.rgb(45, 45, 48)
        canvas.drawRoundRect(RectF(w * .33f, h * .29f + breathe, w * .48f, h * .39f + breathe), 18f, 18f, paint)
        canvas.drawRoundRect(RectF(w * .52f, h * .29f + breathe, w * .67f, h * .39f + breathe), 18f, 18f, paint)
        canvas.drawLine(w * .48f, h * .335f + breathe, w * .52f, h * .335f + breathe, paint)
        paint.style = Paint.Style.FILL

        // brows react to semantic state without moving the whole head.
        paint.color = Color.rgb(86, 56, 43)
        val browLift = if (frame.mode == AvatarMode.LISTENING) -h * .008f else 0f
        canvas.drawRoundRect(RectF(w * .35f, h * .265f + breathe + browLift, w * .46f, h * .278f + breathe + browLift), 8f, 8f, paint)
        canvas.drawRoundRect(RectF(w * .54f, h * .265f + breathe + browLift, w * .65f, h * .278f + breathe + browLift), 8f, 8f, paint)

        // mouth is its own channel. It opens only in SPEAKING.
        paint.color = Color.rgb(135, 57, 67)
        val speech = if (frame.mode == AvatarMode.SPEAKING) frame.speechLevel.coerceIn(0f, 1f) else 0f
        val mouthHeight = h * (.012f + .055f * speech)
        canvas.drawOval(RectF(w * .43f, h * .49f + breathe - mouthHeight / 2f, w * .57f, h * .49f + breathe + mouthHeight / 2f), paint)

        // Charlie C brooch
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w * .012f
        paint.color = Color.rgb(220, 190, 90)
        canvas.drawArc(RectF(w * .66f, h * .69f + breathe, w * .73f, h * .76f + breathe), 45f, 270f, false, paint)
        paint.style = Paint.Style.FILL
    }

    private fun drawEye(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, openness: Float) {
        paint.color = Color.WHITE
        canvas.drawOval(RectF(x - w * .045f, y - h * .018f * openness, x + w * .045f, y + h * .018f * openness), paint)
        if (openness > .3f) {
            paint.color = Color.rgb(70, 87, 78)
            canvas.drawCircle(x, y, w * .014f, paint)
            paint.color = Color.rgb(24, 28, 29)
            canvas.drawCircle(x, y, w * .006f, paint)
        }
    }
}
