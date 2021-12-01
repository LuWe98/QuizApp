package com.example.quizapp.view.fragments.dialogs.authorselection.remote

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfAuthorSelectionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAuthorSelectionRemote
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection
import com.example.quizapp.viewmodel.VmRemoteAuthorSelection.RemoteAuthorSelectionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfRemoteAuthorSelection: BindingBottomSheetDialogFragment<BsdfAuthorSelectionBinding>() {

    companion object {
        const val AUTHOR_SELECTION_RESULT_KEY = "remoteAuthorSelectionResultKey"
    }

    private val vmAuthor: VmRemoteAuthorSelection by viewModels()

    private lateinit var rvAdapter: RvaAuthorSelectionRemote

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initObservers()
        initListeners()
    }

    private fun initViews(){
        binding.etSearchQuery.setText(vmAuthor.searchQuery)

        rvAdapter = RvaAuthorSelectionRemote().apply {
            onItemClicked = vmAuthor::onAuthorClicked
            selectionPredicate = vmAuthor::isAuthorSelected
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
            etSearchQuery.onTextChanged(vmAuthor::onSearchQueryChanged)
            btnConfirm.onClick(vmAuthor::onConfirmButtonClicked)
            btnSearch.onClick(vmAuthor::onDeleteSearchQueryClicked)
        }
    }

    private fun initObservers(){
        vmAuthor.selectedAuthorsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }

        vmAuthor.filteredPagedDataStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitData(it)
        }

        vmAuthor.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmAuthor.userCreatorSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is SendResultEvent -> {
                    setFragmentResult(AUTHOR_SELECTION_RESULT_KEY, Bundle().apply {
                        putParcelableArray(AUTHOR_SELECTION_RESULT_KEY, event.selectedAuthors)
                    })
                    navigator.popBackStack()
                }
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}