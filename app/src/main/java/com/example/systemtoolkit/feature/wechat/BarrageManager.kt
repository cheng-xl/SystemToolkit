package com.example.systemtoolkit.feature.wechat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.TextView

class BarrageManager(private val context: Context) {

    private val wm: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentBarrage: View? = null

    fun canDraw(): Boolean = Settings.canDrawOverlays(context)

    fun isLandscape(): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun show(text: String) {
        if (!canDraw()) return
        dismiss()

        val density = context.resources.displayMetrics.density
        val screenWidth = getScreenWidth()
        val statusBarHeight = getStatusBarHeight()
        val yOffset = (statusBarHeight + (60 + Math.random() * 120).toInt() * density).toInt()

        val tv = TextView(context).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
            maxLines = 1
            alpha = 0.92f
            gravity = Gravity.CENTER_VERTICAL
            setPadding(
                (16 * density).toInt(), (8 * density).toInt(),
                (16 * density).toInt(), (8 * density).toInt()
            )
            measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            y = yOffset
            x = screenWidth
        }

        wm.addView(tv, params)
        currentBarrage = tv

        val textWidth = tv.measuredWidth.coerceAtLeast(1)
        val animator = ObjectAnimator.ofFloat(
            tv, "translationX",
            screenWidth.toFloat(),
            -textWidth.toFloat()
        ).apply {
            duration = 5000L
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    dismiss()
                }
            })
        }
        animator.start()
    }

    fun dismiss() {
        currentBarrage?.let { view ->
            try {
                if (view.isAttachedToWindow) {
                    wm.removeView(view)
                }
            } catch (_: Exception) {
            }
            currentBarrage = null
        }
    }

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.width()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.width
        }
    }

    private fun getStatusBarHeight(): Int {
        val resId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resId > 0) context.resources.getDimensionPixelSize(resId) else 0
    }
}
