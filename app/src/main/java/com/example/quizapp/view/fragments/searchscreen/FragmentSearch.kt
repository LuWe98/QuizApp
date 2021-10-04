package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import com.example.quizapp.databinding.FragmentSearchBinding
import com.example.quizapp.extensions.showKeyboard
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearch : BindingFragment<FragmentSearchBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews(){
        binding.apply {
            etSearchQuery.requestFocus()
            showKeyboard(etSearchQuery)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.setOnClickListener {
                navigator.popBackStack()
            }
        }
    }
}