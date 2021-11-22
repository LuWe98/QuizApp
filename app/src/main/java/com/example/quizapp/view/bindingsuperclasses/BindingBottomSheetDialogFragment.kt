package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import com.example.quizapp.R
import com.example.quizapp.utils.BindingUtils.getBinding
import com.example.quizapp.view.Navigator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

abstract class BindingBottomSheetDialogFragment <VB : ViewBinding> : BottomSheetDialogFragment() {

    @Inject lateinit var navigator: Navigator

    private var _binding: VB? = null
    val binding get() = _binding!!

    val bottomSheet get() = dialog as BottomSheetDialog?

    val bottomSheetBehaviour get() = bottomSheet?.behavior

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        getBinding(this).also{ _binding = it }.root

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
}