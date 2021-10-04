package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.quizapp.extensions.makeFullScreen
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

abstract class BindingFullScreenBottomSheetDialogFragment<VB : ViewBinding> : BindingBottomSheetDialogFragment<VB>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        super.onCreateView(inflater, container, savedInstanceState).makeFullScreen()

    override fun onCreateDialog(savedInstanceState: Bundle?) = (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}