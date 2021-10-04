package com.example.quizapp.view.bindingsuperclasses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.quizapp.utils.BindingUtils.getBinding

abstract class BindingActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding : VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding(this)
    }
}