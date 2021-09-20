package com.example.quizapp.ui.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentSettingsBinding
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSettings : BindingFragment<FragmentSettingsBinding>() {

    private lateinit var rvAdapter : RvaSettings

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaSettings().apply {

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
    }
}