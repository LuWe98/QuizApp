package com.example.quizapp.view.viewpager.pagetransformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.pow

class FadeOutPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        position.let {
            page.apply {
                if (it == -1f || it == 1f) {
                    alpha = 0f
                    translationX = 0f
                    return
                } else if (it < -1 || it > 1) return

                when{
                    it < 0 -> {
                        val value = (1 + it).toDouble().pow(8.0).toFloat()
                        alpha = value
                        translationX = it * width * -0.95f
                    }
                    it > 0 -> {
                        val value = (1 - it).toDouble().pow(8.0).toFloat()
                        alpha = value
                        translationX = it * width * -0.95f
                    }
                    else -> {
                        alpha = 1f
                        translationX = 1f
                    }
                }
            }
        }
    }
}