package com.example.quizapp.view.bindingsuperclasses

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.example.quizapp.R
import com.example.quizapp.extensions.log
import com.example.quizapp.utils.BindingUtils.getBinding

abstract class BindingDialog<VB : ViewBinding>(context: Context, theme: Int = R.style.Theme_QuizApp_DialogFragment) : Dialog(context, theme) {

    private var _binding: VB? = null
    val binding get() = _binding!!

    init { initBinding() }

    private fun initBinding () {
        _binding = getBinding(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        _binding = null
    }
}