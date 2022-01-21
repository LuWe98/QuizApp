package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfAddEditQuestionnaireQuestionListBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaAddEditQuestion
import com.example.quizapp.view.recyclerview.impl.SimpleItemTouchHelper
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.AddEditQuestionnaireQuestionListEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfAddEditQuestionnaireQuestionList : BindingBottomSheetDialogFragment<BsdfAddEditQuestionnaireQuestionListBinding>() {

    private val vmAddEdit: VmAddEditQuestionnaire by hiltNavDestinationViewModels(R.id.fragmentAddEditQuestionnaire)

    private lateinit var rvAdapter: RvaAddEditQuestion

    private lateinit var itemTouchHelper: SimpleItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.etSearchQuery.setText(vmAddEdit.questionSearchQuery)

        itemTouchHelper = SimpleItemTouchHelper(false).apply {
            attachToRecyclerView(binding.rv)
            onDrag = vmAddEdit::onQuestionItemDragged
            onSwiped = vmAddEdit::onQuestionItemSwiped
            onDragReleased = vmAddEdit::onQuestionItemDragReleased
        }

        rvAdapter = RvaAddEditQuestion().apply {
            onItemClick = vmAddEdit::onQuestionItemClicked
            onItemLongClicked = vmAddEdit::onQuestionLongClicked
            onDragHandleTouched = itemTouchHelper::startDrag
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun initListeners() {
        binding.apply {
            btnAdd.onClick(vmAddEdit::onAddQuestionButtonInQuestionListDialogClicked)
            btnCollapse.onClick(vmAddEdit::onBackButtonClicked)
            etSearchQuery.onTextChanged(vmAddEdit::onSearchQueryChanged)
            btnSearch.onClick(vmAddEdit::onDeleteSearchQueryClicked)
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAddEdit::onQuestionMoreOptionsSelectionResultReceived)

        vmAddEdit.filteredQuestionsWithAnswersFlow.collectWhenStarted(viewLifecycleOwner) {
            it.adjustVisibilities(
                binding.rv,
                binding.dataAvailability,
                ListLoadItemType.QUESTION
            )
            rvAdapter.submitList(it.data)
        }

        vmAddEdit.questionsWithAnswersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.tvQuestionsAmount.text = it.size.toString()
        }

        vmAddEdit.questionSearchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmAddEdit.questionListEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowQuestionDeletedSnackBarEvent -> {
                    showSnackBar(
                        viewToAttachTo = dialog!!.window!!.decorView,
                        textRes = R.string.questionDeleted,
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmAddEdit.onUndoDeleteQuestionClicked(event) }
                    )
                }
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}