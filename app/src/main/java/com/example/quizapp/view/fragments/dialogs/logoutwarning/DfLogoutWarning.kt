package com.example.quizapp.view.fragments.dialogs.logoutwarning

import android.os.Bundle
import android.view.View
import com.example.quizapp.databinding.DfLogoutWarningBinding
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@AndroidEntryPoint
class DfLogoutWarning: BindingDialogFragment<DfLogoutWarningBinding>() {

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnLogout.onClick {
                launch(IO) {
                    preferencesRepository.clearPreferenceData()
                }
            }
        }
    }
}