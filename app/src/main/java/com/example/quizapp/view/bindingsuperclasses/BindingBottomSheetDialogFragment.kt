package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import com.example.quizapp.R
import com.example.quizapp.extensions.log
import com.example.quizapp.utils.BindingUtils.getBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.max
import kotlin.math.min

abstract class BindingBottomSheetDialogFragment <VB : ViewBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    val bottomSheetDialog get() = dialog as BottomSheetDialog?

    val bottomSheetBehaviour get() = bottomSheetDialog?.behavior


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = getBinding(this).also{ _binding = it }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme() = R.style.Theme_QuizApp_BottomSheetDialog

    fun enableFullscreenMode(){
        view?.updateLayoutParams<FrameLayout.LayoutParams> {
            height = resources.displayMetrics.heightPixels
        }
        enableNonCollapsing()
    }

    fun enableNonCollapsing() {
        bottomSheetBehaviour?.apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun enableContinuousDim(maxDim: Float = 0.75f, minDim: Float = 0.25f){
        bottomSheetDialog?.window?.setDimAmount(maxDim)
        addBottomSheetCallBack(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val adjustedOffset = 1 + slideOffset
                if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
                    ViewCompat.getWindowInsetsController(dialog!!.window!!.decorView)?.isAppearanceLightStatusBars = adjustedOffset < 0.5f
                }
                bottomSheetDialog?.window?.setDimAmount(min(maxDim, max(adjustedOffset * maxDim, minDim)))
            }
        })
    }

    fun addBottomSheetCallBack(callBack: BottomSheetBehavior.BottomSheetCallback) {
        bottomSheetBehaviour?.addBottomSheetCallback(callBack)
    }

    fun removeBottomSheetCallBack(callBack: BottomSheetBehavior.BottomSheetCallback) {
        bottomSheetBehaviour?.removeBottomSheetCallback(callBack)
    }
}