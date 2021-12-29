package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfAddEditQuestionnaireQuestionListBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAddEditQuestion
import com.example.quizapp.view.recyclerview.impl.SimpleItemTouchHelper
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfAddEditQuestionnaireQuestionList: BindingBottomSheetDialogFragment<BsdfAddEditQuestionnaireQuestionListBinding>() {

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

    private fun initViews(){
        rvAdapter = RvaAddEditQuestion().apply {
            onItemClick = vmAddEdit::onQuestionItemClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()

            itemTouchHelper = SimpleItemTouchHelper().apply {
                attachToRecyclerView(binding.rv)
                onDrag = vmAddEdit::onQuestionItemDragged
                onSwiped = vmAddEdit::onQuestionItemSwiped
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            btnAdd.onClick(vmAddEdit::onAddQuestionButtonInQuestionListDialogClicked)
        }
    }

    private fun initObservers(){
        vmAddEdit.questionsWithAnswersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }
    }
}