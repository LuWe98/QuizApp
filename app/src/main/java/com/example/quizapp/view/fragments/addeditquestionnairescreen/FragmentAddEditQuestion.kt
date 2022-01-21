package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddEditQuestionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaAddEditAnswer
import com.example.quizapp.view.recyclerview.impl.SimpleItemTouchHelper
import com.example.quizapp.viewmodel.VmAddEditQuestion
import com.example.quizapp.viewmodel.VmAddEditQuestion.FragmentAddEditQuestionEvent.*
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import dagger.hilt.android.AndroidEntryPoint

//TODO -> Antworten schön anzeigen und add edit Answer DialogFragment einbauen
//TODO -> Long Click auf answer einbauen

@AndroidEntryPoint
class FragmentAddEditQuestion: BindingFragment<FragmentAddEditQuestionBinding>() {

    private val vmAddEditQuestionnaire: VmAddEditQuestionnaire by hiltNavDestinationViewModels(R.id.fragmentAddEditQuestionnaire)

    private val vmAddEditQuestion: VmAddEditQuestion by viewModels()

    private lateinit var rvAdapter: RvaAddEditAnswer

    private lateinit var itemTouchHelper: SimpleItemTouchHelper

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
        binding.pageTitle.setText(vmAddEditQuestion.pageTitleRes)

        itemTouchHelper = SimpleItemTouchHelper(false).apply {
            attachToRecyclerView(binding.answersCard.rv)
            onDrag = vmAddEditQuestion::onAnswerItemDragged
            onSwiped = vmAddEditQuestion::onAnswerItemSwiped
        }

        rvAdapter = RvaAddEditAnswer(vmAddEditQuestion).apply {
            onItemClick = vmAddEditQuestion::onAnswerClicked
            onItemLongClicked = vmAddEditQuestion::onAnswerLongClicked
            onDragHandleTouched = itemTouchHelper::startDrag
        }

        binding.answersCard.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            disableChangeAnimation()
        }

        binding.apply {
            questionTextCard.onClick(vmAddEditQuestion::onQuestionTextCardClicked)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAddEditQuestion::onBackButtonClicked)
            tvSave.onClick(vmAddEditQuestion::onSaveButtonClicked)
            multipleChoiceCard.onClick(vmAddEditQuestion::onChangeQuestionTypeClicked)
            answersCard.apply {
                btnClearAnswers.onClick(vmAddEditQuestion::onClearAnswersButtonClicked)
                btnAddAnswer.onClick(vmAddEditQuestion::onAddAnswerButtonClicked)
            }
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmAddEditQuestion::onQuestionTextUpdateReceived)

        setFragmentResultEventListener(vmAddEditQuestion::onAnswerMoreOptionsSelectionResultReceived)

        setFragmentResultEventListener(vmAddEditQuestion::onAddEditAnswerResultReceived)

        vmAddEditQuestion.answersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it) {
                binding.answersCard.rv.apply {
                    isVisible = it.isNotEmpty()
                    requestLayout()
                }
            }
        }

        vmAddEditQuestion.isQuestionMultipleChoiceStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.checkBox.isChecked = it
        }

        vmAddEditQuestion.questionTextStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.questionTextCard.text = if(it.isNotBlank()) it else "-"
        }

        vmAddEditQuestion.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
                is SaveQuestionWithAnswersEvent -> vmAddEditQuestionnaire.onQuestionWithAnswerUpdated(event.questionPosition, event.questionWithAnswers)
                is ShowAnswerDeletedSnackBar -> {
                    showSnackBar(
                        textRes = R.string.answerDeleted,
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmAddEditQuestion.onUndoDeleteAnswerClicked(event) }
                    )
                }
            }
        }
    }
}