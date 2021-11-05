package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfLanguageSelectionBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBsdfMenu
import com.example.quizapp.viewmodel.VmSettingsLanguageSelection
import com.example.quizapp.viewmodel.VmSettingsLanguageSelection.LanguageSelectionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfLanguageSelection : BindingBottomSheetDialogFragment<BsdfLanguageSelectionBinding>() {

    private val vmLanguage: VmSettingsLanguageSelection by viewModels()

    private lateinit var rvAdapter: RvaBsdfMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserver()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaBsdfMenu().apply {
            onItemClicked = vmLanguage::onItemSelected
            selectionPredicate = { it.id == vmLanguage.currentLanguage.ordinal }
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }

        rvAdapter.submitList(MenuItemDataModel.languageOptionsMenu)
    }

    private fun initObserver(){
        vmLanguage.languageSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){
                is OnLanguageSelectedEvent -> {
                    navigator.popBackStack()
                    if(event.recreateActivity) {
                        requireActivity().recreate()
                    }
                }
            }
        }
    }
}