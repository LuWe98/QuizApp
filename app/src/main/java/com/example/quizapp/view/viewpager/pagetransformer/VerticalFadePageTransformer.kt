package com.example.quizapp.view.viewpager.pagetransformer
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class VerticalFadePageTransformer : ViewPager2.PageTransformer {

    companion object {
        private const val POW_HOME_SCREEN = 5
        private const val POW_DRAWER = 1
        private const val TRANSLATION_Y_HOME_SCREEN = -0.85f
    }

    override fun transformPage(page: View, position: Float) {
        if (position == -1f || position == 1f) {
            page.translationY = 0f
            page.alpha = getMinAlpha(0f)
            return
        } else if (position < -1 || position > 1) return
        when {
            position < 0 -> {
                val value = (1 + position).toDouble().pow(POW_HOME_SCREEN.toDouble()).toFloat()
                page.alpha = getMinAlpha(value)
                page.translationY = position * page.height * TRANSLATION_Y_HOME_SCREEN
            }
            position > 0 -> {
                val value = (1 - position).toDouble().pow(POW_DRAWER.toDouble()).toFloat()
                page.alpha = getMinAlpha(value)
                page.translationY = position * page.height * value * -1
            }
            else -> {
                page.translationY = 1f
                page.alpha = getMinAlpha(1f)
            }
        }
    }

    private fun getMinAlpha(value: Float) = max(value, 0.01f)
}