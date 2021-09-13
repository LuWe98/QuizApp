package com.example.quizapp.recyclerview.impl

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.AnimRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.R

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