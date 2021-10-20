package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.quizapp.utils.BindingUtils.getBinding
import com.google.android.material.snackbar.Snackbar

abstract class BindingActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding : VB

    val rootView get() = binding.root

    var currentSnackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding(this)
    }
}