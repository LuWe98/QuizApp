package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizOverviewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionQuiz
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.*
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.pow

@AndroidEntryPoint
class FragmentQuizOverview : BindingFragment<FragmentQuizOverviewBinding>(), PopupMenu.OnMenuItemClickListener {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private lateinit var rvAdapter: RvaQuestionQuiz

    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initRecyclerView()
        initBottomSheet()
        initClickListener()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaQuestionQuiz(vmQuiz).apply {
            onItemClick = { position, questionId, card ->
                vmQuiz.onQuestionItemClicked(position, questionId, card)
            }
        }

        binding.bottomSheet.rv.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheet.root).apply {
            onBottomSheetSlide(if (vmQuiz.bottomSheetState == BottomSheetBehavior.STATE_COLLAPSED) 0f else 1f)
            state = vmQuiz.bottomSheetState
            peekHeight = 75.dp
            skipCollapsed = true
            bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) = vmQuiz.onBottomSheetStateUpdated(newState)
                override fun onSlide(sheet: View, slideOffset: Float) = onBottomSheetSlide(slideOffset)
            }

            addBottomSheetCallback(bottomSheetCallback)
        }
    }

    private fun onBottomSheetSlide(slideOffset: Float) {
        binding.bottomSheet.apply {
            rv.alpha = slideOffset.pow(2)
            //+ 0.1f

            (2.dp * slideOffset).let { newElevation ->
                sheetHeader.elevation = newElevation
                btnStart.elevation = newElevation
            }

            btnStart.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = slideOffset
                }
                scaleX = 1 + slideOffset / 3.5f
                scaleY = 1 + slideOffset / 3.5f
            }

            btnCollapse.rotation = 180 + slideOffset * 180
        }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initClickListener() {
        binding.apply {
            btnBack.onClick(vmQuiz::onBackButtonClicked)
            bottomSheet.btnStart.onClick(vmQuiz::onStartButtonClicked)
            btnMoreOptions.onClick(vmQuiz::onMoreOptionsItemClicked)
            bottomSheet.sheetHeader.onClick(this@FragmentQuizOverview::toggleBottomSheet)
            bottomSheet.btnCollapse.onClick(this@FragmentQuizOverview::toggleBottomSheet)
        }
    }

    private fun initObservers() {
        vmQuiz.completeQuestionnaireFlow.collectWhenStarted(viewLifecycleOwner) { completeQuestionnaire ->
            binding.generalInfoCard.apply {
                cosCard.text = completeQuestionnaire.courseOfStudiesAbbreviations
                facultyCard.text = completeQuestionnaire.facultiesAbbreviations
            }
        }


        //TODO -> noch durchmischeln lassen, da es noch nüt get -> Positionen der Fragen werden nicht richtig angezeigt
        vmQuiz.questionsWithAnswersFlow.collectWhenStarted(viewLifecycleOwner) { questionsWithAnswers ->
            rvAdapter.submitList(questionsWithAnswers){
//                rvAdapter.notifyDataSetChanged()
            }
            binding.bottomSheet.tvQuestionsAmount.text = questionsWithAnswers.size.toString()
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
                ivResultIcon.setImageDrawable(if (it.correctQuestionsPercentage > 80) R.drawable.ic_check else R.drawable.ic_cross)
                ivResultIcon.setDrawableTintWithRes(if (it.correctQuestionsPercentage > 80) R.color.green else R.color.red)

                tvQuestionsAnswered.isVisible = !visibilityToggle
                tvQuestionsAnsweredLabel.isVisible = !visibilityToggle
                tvQuestionsAnsweredPercentage.isVisible = !visibilityToggle

                progress.setProgressWithAnimation(it.answeredQuestionsPercentage, (it.answeredQuestionsPercentage * 3.5f).toLong())
                progressCorrect.setProgressWithAnimation(if (it.areAllQuestionsAnswered) it.correctQuestionsPercentage else 0, 350)
                progressIncorrect.setProgressWithAnimation(if (it.areAllQuestionsAnswered) it.incorrectQuestionsPercentage else 0, 350)


                statisticsCard.apply {
                    allQuestions.setProgressWithAnimation(if(it.hasQuestions) 100 else 0, 350)
                    allQuestionsNumber.text = it.questionsAmount.toString()
                    answeredQuestions.setProgressWithAnimation(it.answeredQuestionsPercentage, (it.answeredQuestionsPercentage * 3.5f).toLong())
                    answeredQuestionsAmount.text = it.answeredQuestionsAmount.toString()

                    if (it.areAllQuestionsAnswered) {
                        correctQuestions.setProgressWithAnimation(it.correctQuestionsPercentage, (it.correctQuestionsPercentage * 3.5f).toLong())
                        correctQuestionsAmount.text = it.correctQuestionsAmount.toString()
                        incorrectQuestions.setProgressWithAnimation(it.incorrectQuestionsPercentage, (it.incorrectQuestionsPercentage * 3.5f).toLong())
                        wrongQuestionsAmount.text = it.incorrectQuestionsAmount.toString()
                    } else {
                        correctQuestions.setProgressWithAnimation(0)
                        correctQuestionsAmount.text = "0"
                        incorrectQuestions.setProgressWithAnimation(0)
                        wrongQuestionsAmount.text = "0"
                    }
                }
            }
        }

        vmQuiz.areAllQuestionsAnsweredStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.bottomSheet.rv.updateAllViewHolders()
        }


        vmQuiz.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowUndoDeleteGivenAnswersSnackBack -> {
                    showSnackBar(R.string.answersDeleted, anchorView = binding.bottomSheet.root, actionTextRes = R.string.undo) {
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
                is ShowMessageSnackBar -> showSnackBar(event.messageRes, anchorView = binding.bottomSheet.root)
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

    override fun onDestroyView() {
        bottomSheetBehaviour.removeBottomSheetCallback(bottomSheetCallback)
        super.onDestroyView()
    }
}