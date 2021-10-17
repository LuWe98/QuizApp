package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentSettingsBinding
import com.example.quizapp.extensions.collect
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.showAlertDialog
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmSettings
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSettings : BindingFragment<FragmentSettingsBinding>() {

    private val viewModel: VmSettings by viewModels()

    private lateinit var rvAdapter: RvaSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaSettings().apply {
            onItemClicked = {
                when (it.id) {
                    SettingsModel.ITEM_USER_LOGOUT_ID -> {
                        viewModel.onLogoutClicked()
                    }
                    SettingsModel.ITEM_ADMIN_PAGE_ID -> {
                        viewModel.onGoToAdminPageClicked()
                    }
                }
            }
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        rvAdapter.submitList(SettingsModel.getSettingsItemList(viewModel.userInfo.role))
    }

    private fun initObservers() {
        viewModel.userInfoFlow.observe(viewLifecycleOwner) {
            rvAdapter.submitList(SettingsModel.getSettingsItemList(it.role))
        }

        viewModel.fragmentSettingsEventChannelFlow.collect(lifecycleScope) { event ->
            when (event) {
                NavigateToLoginScreen -> navigator.navigateToLoginScreen()
                OnLogoutClickedEvent -> {
                    showAlertDialog(R.string.logoutWarningTitle, R.string.logoutWarning, R.string.logout, R.string.cancel, positiveButtonClicked = {
                        viewModel.onLogoutConfirmed()
                    })
                }
                NavigateToAdminScreen -> {
                    navigator.navigateToAdminPage()
                }
            }
        }
    }
}