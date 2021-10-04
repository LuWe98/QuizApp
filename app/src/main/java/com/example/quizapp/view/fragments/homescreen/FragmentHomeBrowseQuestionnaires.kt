package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBrowseBinding
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmHome
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHomeBrowseQuestionnaires : BindingFragment<FragmentHomeBrowseBinding>() {

    private val vmHome: VmHome by hiltNavGraphViewModels(R.id.main_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}