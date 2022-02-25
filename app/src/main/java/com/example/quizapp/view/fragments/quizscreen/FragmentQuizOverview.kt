package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizOverviewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentQuizOverview : BindingFragment<FragmentQuizOverviewBinding>(), PopupMenu.OnMenuItemClickListener {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vmQuiz::onBackButtonClicked)
            btnMoreOptions.onClick(vmQuiz::onMoreOptionsItemClicked)
            statisticsCard.btnListQuestions.onClick(vmQuiz::onShowQuestionListDialogClicked)
            btnStartQuiz.onClick(vmQuiz::onStartButtonClicked)
        }
    }

    private fun initObservers() {
        vmQuiz.completeQuestionnaireFlow.collectWhenStarted(viewLifecycleOwner) { completeQuestionnaire ->
            binding.generalInfoCard.apply {
                cosCard.text = completeQuestionnaire.courseOfStudiesAbbreviations
                facultyCard.text = completeQuestionnaire.facultiesAbbreviations
            }
        }

        vmQuiz.questionnaireFlow.collectWhenStarted(viewLifecycleOwner) { questionnaire ->
            binding.apply {
                tvTitle.text = questionnaire.title
                generalInfoCard.apply {
                    authorCard.text = questionnaire.authorInfo.userName
                    subjectCard.text = questionnaire.subject
                    lastUpdatedCard.text = questionnaire.timeStampAsDate
                }
            }
        }

        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                val visibilityToggle = it.areAllQuestionsAnswered && it.hasQuestions

                tvQuestionsAnswered.text = it.answeredQuestionsPercentage.toString()
                ivResultIcon.isVisible = visibilityToggle
                ivResultIcon.setImageDrawable(if (it.correctQuestionsPercentage == 100) R.drawable.ic_check else R.drawable.ic_cross)
                ivResultIcon.setDrawableTintWithRes(if (it.correctQuestionsPercentage == 100) R.color.hfuBrightGreen else R.color.red)

                tvQuestionsAnswered.isVisible = !visibilityToggle
                tvQuestionsAnsweredLabel.isVisible = !visibilityToggle
                tvQuestionsAnsweredPercentage.isVisible = !visibilityToggle

                statisticsCard.apply {
                    allQuestions.setProgressWithAnimation(if(it.hasQuestions) 100 else 0, 350)
                    allQuestionsNumber.text = it.questionsAmount.toString()
                    answeredQuestions.setProgressWithAnimation(it.answeredQuestionsPercentage, (it.answeredQuestionsPercentage * 3.5f).toLong())
                    answeredQuestionsAmount.text = it.answeredQuestionsAmount.toString()

                    if (it.areAllQuestionsAnswered) {
                        progress.progress = 0
                        progressCorrect.setProgressWithAnimation(it.correctQuestionsPercentage, 350)
                        progressIncorrect.setProgressWithAnimation(it.incorrectQuestionsPercentage, 350)

                        correctQuestions.setProgressWithAnimation(it.correctQuestionsPercentage, (it.correctQuestionsPercentage * 3.5f).toLong())
                        correctQuestionsAmount.text = it.correctQuestionsAmount.toString()
                        incorrectQuestions.setProgressWithAnimation(it.incorrectQuestionsPercentage, (it.incorrectQuestionsPercentage * 3.5f).toLong())
                        wrongQuestionsAmount.text = it.incorrectQuestionsAmount.toString()
                    } else {
                        progress.setProgressWithAnimation(it.answeredQuestionsPercentage, (it.answeredQuestionsPercentage * 3.5f).toLong())
                        progressCorrect.setProgressWithAnimation(0, 350)
                        progressIncorrect.setProgressWithAnimation(0, 350)

                        correctQuestions.setProgressWithAnimation(0)
                        correctQuestionsAmount.text = "0"
                        incorrectQuestions.setProgressWithAnimation(0)
                        wrongQuestionsAmount.text = "0"
                    }
                }
            }
        }

        vmQuiz.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowUndoDeleteGivenAnswersSnackBack -> {
                    showSnackBar(R.string.answersDeleted, actionTextRes = R.string.undo) {
                        vmQuiz.onUndoDeleteGivenAnswersClick(event)
                    }
                }
                ShowPopupMenu -> {
                    PopupMenu(requireContext(), binding.btnMoreOptions).apply {
                        inflate(R.menu.quiz_popup_menu)
                        setOnMenuItemClickListener(this@FragmentQuizOverview)
                        menu.apply {
                            findItem(R.id.menu_item_quiz_shuffle_type_none).isChecked = vmQuiz.shuffleType == NONE
                            findItem(R.id.menu_item_quiz_shuffle_type_questions).isChecked = vmQuiz.shuffleType == SHUFFLED_QUESTIONS
                            findItem(R.id.menu_item_quiz_shuffle_type_answers).isChecked = vmQuiz.shuffleType == SHUFFLED_ANSWERS
                            findItem(R.id.menu_item_quiz_shuffle_type_questions_and_answers).isChecked = vmQuiz.shuffleType == SHUFFLED_QUESTIONS_AND_ANSWERS
                        }
                        show()
                    }
                }
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?) = item?.let {
        when (it.itemId) {
            R.id.menu_item_quiz_delete_given_answers -> vmQuiz.onMenuItemClearGivenAnswersClicked()
            R.id.menu_item_quiz_show_solutions -> vmQuiz.onMenuItemShowSolutionClicked()
            R.id.menu_item_quiz_shuffle_type_none -> vmQuiz.onMenuItemOrderSelected(NONE)
            R.id.menu_item_quiz_shuffle_type_questions -> vmQuiz.onMenuItemOrderSelected(SHUFFLED_QUESTIONS)
            R.id.menu_item_quiz_shuffle_type_answers -> vmQuiz.onMenuItemOrderSelected(SHUFFLED_ANSWERS)
            R.id.menu_item_quiz_shuffle_type_questions_and_answers -> vmQuiz.onMenuItemOrderSelected(SHUFFLED_QUESTIONS_AND_ANSWERS)
        }
        true
    } ?: false
}