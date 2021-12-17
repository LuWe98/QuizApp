package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddEditQuestionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAddEditAnswer
import com.example.quizapp.viewmodel.VmAddEditQuestion
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentAddEditQuestionEvent.*
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddEditQuestion: BindingFragment<FragmentAddEditQuestionBinding>() {

    private val vmAddEditQuestionnaire: VmAddEditQuestionnaire by hiltNavDestinationViewModels(R.id.fragmentAddEditQuestionnaire)

    private val vmAddEditQuestion: VmAddEditQuestion by viewModels()

    private lateinit var rvAdapter: RvaAddEditAnswer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialZAxisAnimationForReceiver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        rvAdapter = RvaAddEditAnswer(vmAddEditQuestion).apply {
            onItemClick = vmAddEditQuestion::onAnswerClicked
            onCheckButtonClicked = vmAddEditQuestion::onAnswerCheckClicked
            onDeleteButtonClick = vmAddEditQuestion::onAnswerDeleteClicked
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            disableChangeAnimation()
            addCustomItemTouchHelperCallBack().apply {
                onDrag = vmAddEditQuestion::onAnswerItemDragged
                onSwiped = vmAddEditQuestion::onAnswerItemSwiped
            }
        }

        binding.apply {
            etQuestion.setText(vmAddEditQuestion.questionText)

        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAddEditQuestion::onBackButtonClicked)
            btnSave.onClick(vmAddEditQuestion::onSaveButtonClicked)
            btnAddQuestion.onClick(vmAddEditQuestion::onAddAnswerButtonClicked)
            etQuestion.onTextChanged(vmAddEditQuestion::onQuestionTextChanged)
            btnQuestionType.onClick(vmAddEditQuestion::onChangeQuestionTypeClicked)
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmAddEditQuestion::onAnswerTextUpdateResultReceived)

        vmAddEditQuestion.answersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmAddEditQuestion.isQuestionMultipleChoiceStateFlow.collectWhenStarted(viewLifecycleOwner) {
            updateQuestionTypeIcon(it)
        }

        vmAddEditQuestion.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes, anchorView = binding.btnSave)
                is ShowAnswerDeletedSnackBar -> {
                    showSnackBar(
                        textRes = R.string.answerDeleted,
                        anchorView = binding.btnSave,
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmAddEditQuestion.onUndoDeleteAnswerClicked(event) }
                    )
                }
                is SaveQuestionWithAnswersEvent -> {
                    vmAddEditQuestionnaire.onQuestionWithAnswerUpdated(event.questionPosition, event.questionWithAnswers)
                }
            }
        }
    }


    private fun updateQuestionTypeIcon(isMultipleChoice: Boolean) {
        binding.btnQuestionType.apply {
            if (tag == isMultipleChoice) return
            tag = isMultipleChoice
            clearAnimation()
            animate().scaleX(0f)
                .scaleY(0f)
                .setInterpolator(AccelerateInterpolator())
                .setDuration(150)
                .withEndAction {
                    setImageDrawable(if (isMultipleChoice) R.drawable.ic_check_circle else R.drawable.ic_radio_button)
                    animate().scaleY(1f)
                        .scaleX(1f)
                        .setInterpolator(DecelerateInterpolator())
                        .setDuration(150)
                        .start()
                }.start()
        }
    }
}