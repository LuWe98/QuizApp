package com.example.quizapp.view.fragments.dialogs.facultyselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfFacultySelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaFacultySelection
import com.example.quizapp.viewmodel.VmFacultySelection
import com.example.quizapp.viewmodel.VmFacultySelection.FacultySelectionEvent.ClearSearchQueryEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfFacultySelection: BindingBottomSheetDialogFragment<BsdfFacultySelectionBinding>() {

    private val vmFaculty: VmFacultySelection by viewModels()

    private lateinit var rvAdapter: RvaFacultySelection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.etSearchQuery.setText(vmFaculty.searchQuery)

        rvAdapter = RvaFacultySelection().apply {
            onItemClicked = vmFaculty::onItemClicked
            selectionPredicate = { vmFaculty.isFacultySelected(it.id) }
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun initListeners(){
        binding.apply {
            btnConfirm.onClick(vmFaculty::onConfirmButtonClicked)
            etSearchQuery.onTextChanged(vmFaculty::onSearchQueryChanged)
            btnSearch.onClick(vmFaculty::onDeleteSearchClicked)
        }
    }

    private fun initObservers(){
        vmFaculty.facultyFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmFaculty.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmFaculty.selectedFacultyIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }

        vmFaculty.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}