package com.example.quizapp.view.fragments.quizscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizOverviewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionQuiz
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.*
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.pow

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FragmentQuizOverview : BindingFragment<FragmentQuizOverviewBinding>(), PopupMenu.OnMenuItemClickListener {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private lateinit var rvAdapter: RvaQuestionQuiz
    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<FrameLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initBottomSheet()
        initClickListener()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaQuestionQuiz(vmQuiz).apply {
            onItemClick = { position, questionId, card ->
                //navigator.navigateToQuizContainerScreenWithQuestionCardClick(position, questionId, card)
                navigator.navigateToQuizContainerScreen(position)
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
        val btnMargin = resources.getDimension(R.dimen.grid_8)
        val baseElevation = 2.dp
        val bottomSheetHeaderHeight = 70.dp
        val btnStartDefaultMarginBottom = 12.5.dp

        bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheet.root).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = 70.dp
            skipCollapsed = true
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(sheet: View, slideOffset: Float) {
                    binding.bottomSheet.apply {
//                        root.setPadding(0, (4.dp * (1 - slideOffset)).toInt(), 0, 0)

                        coordRoot.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            height = (((resources.displayMetrics.heightPixels - bottomSheetHeaderHeight) * slideOffset) + bottomSheetHeaderHeight).toInt()
                        }

                        rv.alpha = slideOffset.pow(2)

                        (baseElevation * slideOffset).let { newElevation ->
                            sheetHeader.elevation = newElevation
                            btnStart.elevation = newElevation
                        }

                        btnStart.updateLayoutParams<ViewGroup.MarginLayoutParams>{
                            bottomMargin = btnStartDefaultMarginBottom + (btnMargin * slideOffset).toInt()
                        }

                        btnStart.apply {
                            scaleX = 1 + slideOffset / 3.5f
                            scaleY = 1 + slideOffset / 3.5f
                        }

                        btnCollapse.rotation = 180 + slideOffset * 180
                    }
                }
            })
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
            btnBack.onClick(navigator::popBackStack)
            bottomSheet.btnStart.onClick(vmQuiz::onFabClicked)
            btnMoreOptions.onClick(vmQuiz::onMoreOptionsItemClicked)
            bottomSheet.sheetHeader.onClick(this@FragmentQuizOverview::toggleBottomSheet)
            bottomSheet.btnCollapse.onClick(this@FragmentQuizOverview::toggleBottomSheet)
        }
    }

    private fun initObservers() {
        vmQuiz.completeQuestionnaireStateFlow.collectWhenStarted(viewLifecycleOwner) { completeQuestionnaire ->
            if (completeQuestionnaire == null) return@collectWhenStarted

            binding.generalInfoCard.apply {
                tvCourseOfStudies.text = completeQuestionnaire.courseOfStudies?.name
                tvFaculty.text = completeQuestionnaire.faculty?.name
            }

            onShouldDisplaySolutionChanged(vmQuiz.shouldDisplaySolution, completeQuestionnaire)
        }


        vmQuiz.questionsWithAnswersFlow.collectWhenStarted(viewLifecycleOwner) { questionsWithAnswers ->
            rvAdapter.submitList(questionsWithAnswers)
            binding.bottomSheet.tvQuestionsAmount.text = questionsWithAnswers.size.toString()
        }


        vmQuiz.questionnaireFlow.collectWhenStarted(viewLifecycleOwner) { questionnaire ->
            binding.apply {
                tvTitle.text = questionnaire.title
                generalInfoCard.apply {
                    tvAuthor.text = questionnaire.authorInfo.userName
                    tvSubject.text = questionnaire.subject
                    tvLastUpdated.text = questionnaire.timeStampAsDate
                }
            }
        }


        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) {
            if (it.answeredQuestionsPercentage != 100) {
                vmQuiz.setShouldDisplaySolution(false)
            }

            binding.apply {
                tvQuestionsAnswered.text = it.answeredQuestionsPercentage.toString()
                ivResultIcon.isVisible = it.areAllQuestionsCorrectlyAnswered
                tvQuestionsAnswered.isVisible = !it.areAllQuestionsCorrectlyAnswered
                tvQuestionsAnsweredLabel.isVisible = !it.areAllQuestionsCorrectlyAnswered
                tvQuestionsAnsweredPercentage.isVisible = !it.areAllQuestionsCorrectlyAnswered

                progress.setProgressWithAnimation(it.answeredQuestionsPercentage, (it.answeredQuestionsPercentage * 3.5f).toLong())
                progressCorrect.setProgressWithAnimation(if (it.areAllQuestionsAnswered) it.correctQuestionsPercentage else 0, 350)
                progressIncorrect.setProgressWithAnimation(if (it.areAllQuestionsAnswered) it.incorrectQuestionsPercentage else 0, 350)
            }

            binding.statisticsCard.apply {
                allQuestions.setProgressWithAnimation(100, 350)
                allQuestionsNumber.text = it.questionsAmount.toString()
                answeredQuestions.setProgressWithAnimation(it.answeredQuestionsPercentage, (it.answeredQuestionsPercentage * 3.5f).toLong())
                answeredQuestionsAmount.text = it.answeredQuestionsAmount.toString()
                correctQuestions.setProgressWithAnimation(it.correctQuestionsPercentage, (it.correctQuestionsPercentage * 3.5f).toLong())
                correctQuestionsAmount.text = it.correctQuestionsAmount.toString()
                incorrectQuestions.setProgressWithAnimation(it.incorrectQuestionsPercentage, (it.incorrectQuestionsPercentage * 3.5f).toLong())
                wrongQuestionsAmount.text = it.incorrectQuestionsAmount.toString()
            }
        }


        vmQuiz.shouldDisplaySolutionStateFlow.collectWhenStarted(viewLifecycleOwner) { shouldDisplay ->
            vmQuiz.completeQuestionnaire?.let {
                onShouldDisplaySolutionChanged(shouldDisplay, it)
            }
        }


        vmQuiz.fragmentEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
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
                        //menu.findItem(R.id.menu_item_show_solutions).isChecked = vmQuiz.shouldDisplaySolution
                        show()
                    }
                }
                is NavigateToQuizScreen -> navigator.navigateToQuizContainerScreen(isShowSolutionScreen = event.isShowSolutionScreen)
                is ShowMessageSnackBar -> showSnackBar(event.messageRes, anchorView = binding.bottomSheet.root)
            }
        }
    }


    private fun onShouldDisplaySolutionChanged(shouldDisplay: Boolean, completeQuestionnaire: CompleteQuestionnaire) {
        return
        val cqp = completeQuestionnaire.correctQuestionsPercentage
        binding.apply {
            progressCorrect.setProgressWithAnimation(if (shouldDisplay) cqp else 0)
            progressIncorrect.setProgressWithAnimation(if (shouldDisplay) 100 - cqp else 0)

            tvQuestionsAnswered.isVisible = !shouldDisplay
            ivResultIcon.isVisible = shouldDisplay

            if (shouldDisplay) {
                val isEverythingCorrect = cqp == 100
                ivResultIcon.setImageDrawable(if (isEverythingCorrect) R.drawable.ic_check else R.drawable.ic_cross)
                ivResultIcon.setDrawableTintWithRes(if (isEverythingCorrect) R.color.green else R.color.red)
            }

            bottomSheet.rv.updateAllViewHolders()
        }
    }

    override fun onMenuItemClick(item: MenuItem?) = item?.let {
        when (item.itemId) {
            R.id.menu_item_delete_given_answers -> vmQuiz.onMenuItemClearGivenAnswersClicked()
            R.id.menu_item_show_solutions -> vmQuiz.onMenuItemShowSolutionClicked()
            R.id.menu_item_shuffle_questions -> vmQuiz.onMenuItemShuffleClicked()
            else -> return@let false
        }
        true
    } ?: false

}