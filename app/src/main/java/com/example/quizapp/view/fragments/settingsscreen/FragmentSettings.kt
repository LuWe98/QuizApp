package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentSettingsBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
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
            btnShuffleType.onClick(navigator::navigateToShuffleTypeSelection)
            btnPreferredCos.onClick(vmSettings::onPreferredCourseOfStudiesButtonClicked)
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
        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { _, bundle ->
            bundle.getStringArray(BsdfCourseOfStudiesSelection.SELECTED_COURSE_OF_STUDIES_KEY)?.let { courseOfStudiesIds ->
                vmSettings.onCourseOfStudiesUpdateTriggered(courseOfStudiesIds.toList())
            }
        }

        vmSettings.userNameFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.userLayout.btnUserName.text = it ?: "-"
        }

        vmSettings.userRoleFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.userLayout.btnRole.text = it?.name ?: "-"
            binding.adminLayout.root.isVisible = it == Role.ADMIN
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
            binding.preferencesLayout.btnPreferredCos.text = if(it.size > 2) it.size.toString()
                else it.reduceOrNull { acc, s -> "$acc, $s"  } ?: "-"
        }

        vmSettings.fragmentSettingsEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                NavigateToLoginScreen -> navigator.navigateToLoginScreen()
                OnLogoutClickedEvent -> navigator.navigateToLogoutWarningScreen()
                NavigateToAdminScreen -> navigator.navigateToAdminPage()
                is ShowMessageSnackBarEvent -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    showSnackBar(
                        textRes = event.messageRes,
                        anchorView = bindingActivity.binding.root.findViewById(R.id.bottomAppBar)
                    )
                }
                is NavigateToCourseOfStudiesSelectionScreen -> navigator.navigateToCourseOfStudiesSelection(event.courseOfStudiesIds)
            }
        }
    }
}