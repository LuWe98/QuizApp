package com.example.quizapp.ui.fragments.bindingfragmentsuperclasses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.quizapp.utils.BindingUtils.getBinding

abstract class BindingActivity<VM : ViewBinding> : AppCompatActivity() {

    lateinit var binding : VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getBinding()
    }
}