package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import com.example.quizapp.databinding.BsdfBrowseQuestionnaireSearchFilterBinding
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfSearchFilter: BindingBottomSheetDialogFragment<BsdfBrowseQuestionnaireSearchFilterBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()
    }

}