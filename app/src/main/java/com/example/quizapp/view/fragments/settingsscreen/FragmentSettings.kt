package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentSettingsBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.flowext.awareCollect
import com.example.quizapp.extensions.flowext.collect
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmSettings
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSettings : BindingFragment<FragmentSettingsBinding>() {

    private val vmSettings: VmSettings by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.adminLayout.apply {
            btnAdminFunctionality.onClick(vmSettings::onGoToAdminPageClicked)
        }

        binding.preferencesLayout.apply {
            btnTheme.onClick(navigator::navigateToThemeSelection)
            btnLanguage.onClick(navigator::navigateToLanguageSelection)
        }

        binding.userLayout.apply {
            btnLogout.onClick(vmSettings::onLogoutClicked)

            btnChangePassword.onClick {

            }

            btnRole.onClick {

            }

            btnUserName.onClick {

            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener(vmSettings::onRefreshListenerTriggered)
    }


    private fun initObservers() {
        vmSettings.userNameFlow.awareCollect(viewLifecycleOwner) {
            binding.userLayout.btnUserName.text = it ?: ""
        }

        vmSettings.userRoleFlow.awareCollect(viewLifecycleOwner) {
            binding.userLayout.btnRole.text = it?.name ?: ""
            binding.adminLayout.root.isVisible = it == Role.ADMIN
        }

        vmSettings.themeNameResFlow.awareCollect(viewLifecycleOwner) {
            binding.preferencesLayout.btnTheme.text = it?.let { getString(it) } ?: ""
        }

        vmSettings.languageFlow.awareCollect(viewLifecycleOwner) {
            binding.preferencesLayout.btnLanguage.text = it?.let { getString(it.textRes) } ?: ""
        }

        vmSettings.fragmentSettingsEventChannelFlow.collect(lifecycleScope) { event ->
            when (event) {
                NavigateToLoginScreen -> navigator.navigateToLoginScreen()
                OnLogoutClickedEvent -> {
                    showAlertDialog(
                        titleRes = R.string.logoutWarningTitle,
                        textRes = R.string.logoutWarning,
                        positiveButtonRes = R.string.logout,
                        negativeButtonRes = R.string.cancel,
                        positiveButtonClicked = {
                            vmSettings.onLogoutConfirmed()
                        })
                }
                NavigateToAdminScreen -> navigator.navigateToAdminPage()
                is ShowMessageSnackBarEvent -> {
                    showSnackBar(event.messageRes)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }
}