package com.example.quizapp.view.bindingsuperclasses

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.quizapp.extensions.setLocale
import com.example.quizapp.utils.BindingUtils.getBinding
import com.google.android.material.snackbar.Snackbar

abstract class BindingActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding : VB

    val rootView get() = binding.root

    var currentSnackBar: Snackbar? = null

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.setLocale())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding(this)
    }
}