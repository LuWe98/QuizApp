package com.example.quizapp.view.fragments.addquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddQuestionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.recyclerview.adapters.RvaAnswerEditQuestion
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAddEdit
import com.example.quizapp.viewmodel.VmAddEditQuestion
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentEditQuestionEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddQuestion : BindingFragment<FragmentAddQuestionBinding>() {

    private val vmAdd : VmAddEdit by hiltNavGraphViewModels(R.id.add_nav_graph)
    private val vmEditQuestion : VmAddEditQuestion by viewModels()

    private lateinit var rvAdapter : RvaAnswerEditQuestion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.questionEditText.setText(vmEditQuestion.questionTitle)
        binding.isMultipleChoiceSwitch.isChecked = vmEditQuestion.isMultipleChoice

        rvAdapter = RvaAnswerEditQuestion(vmEditQuestion).apply {
            onItemClick = vmEditQuestion::onAnswerItemClicked
            onDeleteButtonClick = vmEditQuestion::onAnswerItemDeleteButtonClicked
            onAnswerTextChanged = vmEditQuestion::onAnswerItemTextChanged
        }

        binding.rv.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            addCustomItemTouchHelperCallBack().apply {
                onSwiped = vmEditQuestion::onAnswerItemSwiped
                onDrag = rvAdapter::moveItem
                onDragReleased = { vmEditQuestion.onAnswerItemDragReleased(rvAdapter.currentList) }
            }
        }
    }

    private fun initListeners() {
        binding.buttonBack.onClick(navigator::popBackStack)
        binding.buttonAdd.onClick(vmEditQuestion::onAddAnswerButtonClicked)
        binding.fabConfirm.onClick(vmEditQuestion::onFabConfirmClicked)
        binding.isMultipleChoiceSwitch.onCheckedChange(vmEditQuestion::onSwitchChanged)
        binding.questionEditText.onTextChanged(vmEditQuestion::onQuestionEditTextChanged)
    }

    private fun initObservers(){
        vmEditQuestion.answersLiveData.observe(viewLifecycleOwner) {
            //binding.noDataLayout.isVisible = it.isEmpty()
            rvAdapter.submitList(it)
        }

        vmEditQuestion.fragmentEditQuestionEventChannelFlow.collect(lifecycleScope) { event ->
            when(event){
                is ShowAnswerDeletedSuccessFullySnackBar -> {
                    showSnackBar(R.string.answerDeleted, viewToAttachTo = bindingActivity.rootView, actionTextRes = R.string.undo) {
                        vmEditQuestion.onUndoDeleteAnswerClicked(event)
                    }
                }
                is ShowSelectAtLeastOneCorrectAnswerToast -> {
                    showToast(R.string.errorSelectOneCorrectAnswer)
                }
                is ShowSomeAnswersAreEmptyToast -> {
                    showToast(R.string.errorAnswersDoNotHaveText)
                }
                is SendUpdateRequestToVmAdd -> {
                    vmAdd.onSaveSpecificQuestionClicked(event)
                    navigator.popBackStack()
                }
            }
        }
    }
}