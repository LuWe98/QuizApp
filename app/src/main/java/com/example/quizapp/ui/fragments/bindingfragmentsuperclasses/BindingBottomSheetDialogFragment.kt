package com.example.quizapp.ui.fragments.bindingfragmentsuperclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.quizapp.R
import com.example.quizapp.ui.Navigator
import com.example.quizapp.utils.BindingUtils.getBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

abstract class BindingBottomSheetDialogFragment <VM : ViewBinding> : BottomSheetDialogFragment() {

    @Inject lateinit var navigator: Navigator

    private var _binding: VM? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)  = getBinding().also{ _binding = it }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme() = R.style.Theme_QuizApp_BottomSheetDialog
}