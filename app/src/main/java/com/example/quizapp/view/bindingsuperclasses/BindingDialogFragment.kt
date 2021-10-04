package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.example.quizapp.R
import com.example.quizapp.view.Navigator
import com.example.quizapp.utils.BindingUtils.getBinding
import javax.inject.Inject

abstract class BindingDialogFragment<VB : ViewBinding> : DialogFragment() {

    @Inject
    lateinit var navigator: Navigator

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        getBinding(this).also { _binding = it }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme() = R.style.Theme_QuizApp_DialogFragment
}