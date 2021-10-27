package com.example.quizapp.view.recyclerview.impl

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R
import kotlin.math.abs
import kotlin.math.pow

object RvScrollListenerUtil {


    fun createFadeTransformer(alphaAmplifier: Float = 3f, yTranslationFraction: Float = 2f) =
        FadeTransformer(alphaAmplifier, yTranslationFraction)


    class FadeTransformer constructor(private val alphaAmplifier: Float, private val yTranslationAmplifier: Float) : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            recyclerView.viewHolders.forEach { holder ->
                if (holder.itemView.top < recyclerView.top) {
                    val translationFactor = abs(holder.itemView.top - recyclerView.top) / holder.itemView.height.toFloat()
                    val alphaFactor = (1 - translationFactor).toDouble().pow(alphaAmplifier.toDouble()).toFloat()
                    holder.itemView.alpha = alphaFactor
                    holder.itemView.translationY = holder.itemView.height * translationFactor / yTranslationAmplifier
                } else {
                    holder.itemView.alpha = 1f
                    holder.itemView.translationY = 1f
                }
            }
        }
    }


    class ViewHideScrollListener(
        private val viewToHide: View,
        @AnimRes private val hideAnimResource: Int = R.anim.trans_upwards,
        @AnimRes private val showAnimResource: Int = R.anim.trans_downwards,
    ) : RecyclerView.OnScrollListener() {

        var animation: Animation? = null

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy <= 0 ) showView() else hideView()
        }

        private fun hideView() {
            if (!viewToHide.isVisible) return
            if(animation != null) return
            checkIfAnimationExists()
            startAnimationWith(hideAnimResource, false)
        }

        private fun showView() {
            if (viewToHide.isVisible) return
            if(animation != null) return
            checkIfAnimationExists()
            startAnimationWith(showAnimResource, true)
        }

        private fun checkIfAnimationExists() {
            if (animation != null) {
                animation!!.cancel()
                viewToHide.clearAnimation()
            }
        }

        private fun startAnimationWith(@AnimRes animRes: Int, show : Boolean){
            animation = AnimationUtils.loadAnimation(viewToHide.context, animRes)
            animation!!.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(p0: Animation?) {
                    viewToHide.isVisible = !show
                }
                override fun onAnimationRepeat(p0: Animation?) {}

                override fun onAnimationEnd(p0: Animation?) {
                    viewToHide.isVisible = show
                    animation = null
                }
            })
            viewToHide.startAnimation(animation!!)
        }
    }


    private val RecyclerView.viewHolders
        get() : List<RecyclerView.ViewHolder> {
            return mutableListOf<RecyclerView.ViewHolder>().apply {
                for (i in 0 until childCount) {
                    getChildAt(i)?.let { child ->
                        findContainingViewHolder(child)?.let { add(it) }
                    }
                }
            }
        }
}