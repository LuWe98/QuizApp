package com.example.quizapp.view.fragments.homescreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfLocalQuestionnaireFilterSelectionBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.extensions.setImageDrawable
import com.example.quizapp.extensions.setSelectionTypeListener
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection.LocalQuestionnaireFilterSelectionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfLocalQuestionnaireFilterSelection: BindingBottomSheetDialogFragment<BsdfLocalQuestionnaireFilterSelectionBinding>() {

    companion object {
        const val LOCAL_QUESTIONNAIRE_FILTER_RESULT_KEY = "localQuestionnaireFilterResultKey"
    }

    private val vmFilter: VmLocalQuestionnaireFilterSelection by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){

    }

    private fun initListeners(){
        binding.apply {
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
        }
    }

    private fun initObservers(){
        setSelectionTypeListener(vmFilter::onOrderByUpdateReceived)

        vmFilter.selectedOrderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                tvOrderBy.setText(it.textRes)
                ivOrderBy.setImageDrawable(it.iconRes)
            }
        }

        vmFilter.selectedOrderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.apply {
                tvOrderAscending.setText(if(ascending) R.string.ascending else R.string.descending)
                ivOrderAscending.clearAnimation()
                ivOrderAscending.animate()
                    .rotation(if(ascending) 0f else 180f)
                    .setDuration(300)
                    .start()
            }
        }

        vmFilter.localQuestionnaireFilterSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToOrderBySelection -> navigator.navigateToSelectionDialog(SelectionType.LocalQuestionnaireOrderBySelection(event.orderBy))
                is ApplySelectionEvent -> {
                    setFragmentResult(LOCAL_QUESTIONNAIRE_FILTER_RESULT_KEY, Bundle().apply {

                    })
                    navigator.popBackStack()
                }
            }
        }
    }
}