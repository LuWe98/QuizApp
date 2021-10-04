package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentSettingsBinding
import com.example.quizapp.extensions.collect
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSettings : BindingFragment<FragmentSettingsBinding>() {

    private val viewModel : VmSettings by viewModels()

    private lateinit var rvAdapter : RvaSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaSettings().apply {
            onItemClicked = {
                when(it.id) {
                    SettingsModel.ITEM_USER_LOGOUT_ID -> {
                        viewModel.onLogoutClicked()
                    }
                }
            }
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    private fun initObservers(){
        SettingsModel.settingsListLiveData.observe(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        viewModel.fragmentSettingsEventChannelFlow.collect(lifecycleScope){ event ->
            when(event){
                VmSettings.FragmentSettingsEvent.NavigateToLoginScreen -> navigator.navigateToLoginScreen()
            }
        }
    }
}