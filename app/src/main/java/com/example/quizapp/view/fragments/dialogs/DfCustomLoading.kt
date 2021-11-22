package com.example.quizapp.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import com.example.quizapp.databinding.DialogCustomLoadingBinding
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfCustomLoading: BindingDialogFragment<DialogCustomLoadingBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    /*

        launch {
            showDialog<DfCustomLoading>(tag = "test").apply {
                isCancelable = false
            }
            delay(4000)
            findDialog<DfCustomLoading>("test")?.let {
                it.dismiss()
            }
        }
     */



}