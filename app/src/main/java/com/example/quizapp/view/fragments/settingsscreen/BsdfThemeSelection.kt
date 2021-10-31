package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfThemeSelectionBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.flowext.awareCollect
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.model.menudatamodels.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBsdfMenu
import com.example.quizapp.viewmodel.VmSettingsThemeSelection
import com.example.quizapp.viewmodel.VmSettingsThemeSelection.ThemeSelectionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfThemeSelection : BindingBottomSheetDialogFragment<BsdfThemeSelectionBinding>() {

    private val vmTheme: VmSettingsThemeSelection by viewModels()

    private lateinit var rvAdapter: RvaBsdfMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserver()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaBsdfMenu().apply {
            onItemClicked = vmTheme::onItemSelected
            selectionPredicate = { it.id == vmTheme.currentTheme }
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }

        rvAdapter.submitList(MenuItemDataModel.themeOptionsMenu)
    }

    private fun initObserver(){
        vmTheme.themeSelectionEventChannelFlow.awareCollect(viewLifecycleOwner) { event ->
            when(event){
                is OnThemeSelectedEvent -> {
                    navigator.popBackStack()
                    if(event.recreateActivity) {
                        AppCompatDelegate.setDefaultNightMode(event.newTheme)
                    }
                }
            }
        }
    }
}