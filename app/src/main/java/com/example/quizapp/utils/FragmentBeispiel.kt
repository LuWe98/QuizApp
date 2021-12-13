package com.example.quizapp.utils

import android.os.Bundle
import android.view.View
import com.example.quizapp.databinding.ActivityQuizBinding
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.view.bindingsuperclasses.BindingFragment

class FragmentBeispiel: BindingBottomSheetDialogFragment<ActivityQuizBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}