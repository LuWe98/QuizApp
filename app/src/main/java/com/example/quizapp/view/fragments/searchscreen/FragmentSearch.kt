package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import com.example.quizapp.databinding.FragmentSearchBinding
import com.example.quizapp.extensions.showKeyboard
import com.example.quizapp.view.fragments.bindingfragmentsuperclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearch : BindingFragment<FragmentSearchBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
    }

    private fun initViews(){
        binding.apply {
            searchQueryEditText.requestFocus()
            showKeyboard(searchQueryEditText)
        }
    }

    private fun initClickListeners(){
        binding.apply {
            buttonBack.setOnClickListener {
                navigator.popBackStack()
            }
        }
    }
}