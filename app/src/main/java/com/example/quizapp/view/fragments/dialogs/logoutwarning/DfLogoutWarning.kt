package com.example.quizapp.view.fragments.dialogs.logoutwarning

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.DfLogoutWarningBinding
import com.example.quizapp.extensions.activityViewModels
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.viewmodel.VmQuizActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfLogoutWarning: BindingDialogFragment<DfLogoutWarningBinding>() {

    private val vmQuizActivity: VmQuizActivity by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnLogout.onClick(vmQuizActivity::onLogoutConfirmed)
        }
    }
}