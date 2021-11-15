package com.example.quizapp.view.fragments.settingsscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfShuffleTypeSelectionBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaIntIdMenu
import com.example.quizapp.view.recyclerview.adapters.RvaStringIdMenu
import com.example.quizapp.viewmodel.VmSettingsQuestionShufflingSelection
import com.example.quizapp.viewmodel.VmSettingsQuestionShufflingSelection.QuestionOrderingSelectionEvent.OnQuestionOrderingSelectedEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfShuffleTypeSelection : BindingBottomSheetDialogFragment<BsdfShuffleTypeSelectionBinding>() {

    private val vmShuffling: VmSettingsQuestionShufflingSelection by viewModels()

    private lateinit var rvAdapter: RvaStringIdMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserver()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaStringIdMenu().apply {
            onItemClicked = vmShuffling::onItemSelected
            selectionPredicate = { it.id == vmShuffling.currentQuestionShuffleType.name }
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }

        rvAdapter.submitList(MenuItemDataModel.shuffleQuestionsOptionsMenu)
    }

    private fun initObserver(){
        vmShuffling.questionShufflingSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){
                OnQuestionOrderingSelectedEvent -> navigator.popBackStack()
            }
        }
    }
}