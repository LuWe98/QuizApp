package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentSettingsBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.viewmodel.VmSettings
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSettings : BindingFragment<FragmentSettingsBinding>() {

    private val vmSettings: VmSettings by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vmSettings::onBackButtonClicked)

            adminLayout.apply {
                btnAdminUser.onClick(vmSettings::onGoToManageUsersClicked)
                btnAdminCourseOfStudies.onClick(vmSettings::onGoToManageCoursesOfStudiesClicked)
                btnAdminFaculty.onClick(vmSettings::onGoToManageFacultiesClicked)
            }

            preferencesLayout.apply {
                btnTheme.onClick(vmSettings::onThemeButtonClicked)
                btnLanguage.onClick(vmSettings::onLanguageButtonClicked)
                btnShuffleType.onClick(vmSettings::onShuffleTypeButtonClicked)
                btnPreferredCos.onClick(vmSettings::onPreferredCourseOfStudiesButtonClicked)
            }

            userLayout.apply {
                btnLogout.onClick(vmSettings::onLogoutClicked)
                btnChangePassword.onClick(vmSettings::onChangePasswordCardClicked)
            }

            synchronizationLayout.apply {
                btnSyncUserData.onClick(vmSettings::syncUserDataClicked)
                btnSyncQuestionnaires.onClick(vmSettings::onSyncQuestionnairesClicked)
                btnSyncCosAndFaculties.onClick(vmSettings::onSyncCosAndFacultiesClicked)
            }
        }
    }


    private fun initObservers() {

        setFragmentResultEventListener(vmSettings::onCourseOfStudiesSelectionResultReceived)

        setFragmentResultEventListener(vmSettings::onLogoutConfirmationResultReceived)

        setFragmentResultEventListener(vmSettings::onLanguageSelectionResultReceived)

        setFragmentResultEventListener(vmSettings::onThemeSelectionResultReceived)

        setFragmentResultEventListener(vmSettings::onShuffleTypeSelectionResultReceived)

        vmSettings.userNameFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.userLayout.btnUserName.text = it ?: "-"
        }

        vmSettings.userRoleFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                adminLayout.root.isVisible = it == Role.ADMIN
                userLayout.btnRole.text = it?.name ?: "-"
            }
        }

        vmSettings.themeNameResFlow.collectWhenStarted(viewLifecycleOwner) { stringRes ->
            binding.preferencesLayout.btnTheme.text = stringRes?.let(::getString) ?: "-"
        }

        vmSettings.languageNameResFlow.collectWhenStarted(viewLifecycleOwner) { stringRes ->
            binding.preferencesLayout.btnLanguage.text = stringRes?.let(::getString) ?: "-"
        }

        vmSettings.shuffleTypeNameResFlow.collectWhenStarted(viewLifecycleOwner) { stringRes ->
            binding.preferencesLayout.btnShuffleType.text = stringRes?.let(::getString) ?: "-"
        }

        vmSettings.preferredCoursesOfStudiesFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.preferencesLayout.btnPreferredCos.text =
                if(it.size > 2) it.size.toString() else it.reduceOrNull { acc, s -> "$acc, $s" } ?: "-"
        }

        vmSettings.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                LogoutEvent -> quizActivity.logoutUser()
                is ShowMessageSnackBarEvent -> showSnackBar(textRes = event.messageRes)
                RecreateActivityEvent -> requireActivity().recreate()
            }
        }
    }
}