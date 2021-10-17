package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.quizapp.utils.BindingUtils.getBinding

abstract class BindingActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding : VB

    val rootView get() = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding(this)
    }
}