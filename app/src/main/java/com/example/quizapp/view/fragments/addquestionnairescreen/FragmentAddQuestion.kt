package com.example.quizapp.view.fragments.addquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddQuestionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.flowext.collect
import com.example.quizapp.view.recyclerview.adapters.RvaAnswerAddEdit
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAddEdit
import com.example.quizapp.viewmodel.VmAddEditQuestion
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentEditQuestionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddQuestion : BindingFragment<FragmentAddQuestionBinding>() {

    private val vmAddEdit : VmAddEdit by hiltNavGraphViewModels(R.id.add_nav_graph)
    private val vmAddEditQuestion : VmAddEditQuestion by viewModels()

    private lateinit var rvAdapter : RvaAnswerAddEdit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.questionEditText.setText(vmAddEditQuestion.questionTitle)
        binding.isMultipleChoiceSwitch.isChecked = vmAddEditQuestion.isMultipleChoice

        rvAdapter = RvaAnswerAddEdit(vmAddEditQuestion).apply {
            onItemClick = vmAddEditQuestion::onAnswerItemClicked
            onDeleteButtonClick = vmAddEditQuestion::onAnswerItemDeleteButtonClicked
            onAnswerTextChanged = vmAddEditQuestion::onAnswerItemTextChanged
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
            addCustomItemTouchHelperCallBack().apply {
                onSwiped = vmAddEditQuestion::onAnswerItemSwiped
                onDrag = rvAdapter::moveItem
                onDragReleased = { vmAddEditQuestion.onAnswerItemDragReleased(rvAdapter.currentList) }
            }
        }
    }

    private fun initListeners() {
        binding.buttonBack.onClick(navigator::popBackStack)
        binding.buttonAdd.onClick(vmAddEditQuestion::onAddAnswerButtonClicked)
        binding.fabConfirm.onClick(vmAddEditQuestion::onFabConfirmClicked)
        binding.isMultipleChoiceSwitch.onCheckedChange(vmAddEditQuestion::onSwitchChanged)
        binding.questionEditText.onTextChanged(vmAddEditQuestion::onQuestionEditTextChanged)
    }

    private fun initObservers(){
        vmAddEditQuestion.answersLiveData.observe(viewLifecycleOwner) {
            rvAdapter.submitList(it) {
                //binding.noDataLayout.isVisible = it.isEmpty()
            }
        }

        vmAddEditQuestion.fragmentEditQuestionEventChannelFlow.collect(lifecycleScope) { event ->
            when(event){
                is ShowAnswerDeletedSuccessFullySnackBar -> {
                    showSnackBar(
                        textRes = R.string.answerDeleted,
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmAddEditQuestion.onUndoDeleteAnswerClicked(event) }
                    )
                }
                is ShowSelectAtLeastOneCorrectAnswerToast -> {
                    showToast(R.string.errorSelectOneCorrectAnswer)
                }
                is ShowSomeAnswersAreEmptyToast -> {
                    showToast(R.string.errorAnswersDoNotHaveText)
                }
                is SendUpdateRequestToVmAdd -> {
                    vmAddEdit.onSaveSpecificQuestionClicked(event)
                    navigator.popBackStack()
                }
            }
        }
    }
}