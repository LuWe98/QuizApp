package com.example.quizapp.view.fragments.quizscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizOverviewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.flowext.awareCollect
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionWithAnswersQuiz
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.*
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FragmentQuizOverview : BindingFragment<FragmentQuizOverviewBinding>(), PopupMenu.OnMenuItemClickListener {

    val viewModel: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private lateinit var rvAdapter: RvaQuestionWithAnswersQuiz

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initClickListener()
        initObservers()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaQuestionWithAnswersQuiz(viewModel).apply {
            onItemClick = { position, questionId, card ->
                //navigator.navigateToQuizContainerScreenWithQuestionCardClick(position, questionId, card)
                navigator.navigateToQuizContainerScreen(position)
            }
        }

        binding.rv.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
        }
    }

    private fun initClickListener() {
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            fabStart.onClick(viewModel::onFabClicked)
            btnMoreOptions.onClick(viewModel::onMoreOptionsItemClicked)
        }
    }

    private fun initObservers() {
        viewModel.apply {
            questionnaireLiveData.observe(viewLifecycleOwner) {
                binding.apply {
                    tvTitle.text = it.title
                    tvAuthor.text = it.authorInfo.userName
                    tvCourseOfStudies.text = it.courseOfStudies
                    tvSubject.text = it.subject
                }
            }

            questionsWithAnswersLiveData.observe(viewLifecycleOwner) { questionsWithAnswers ->
                rvAdapter.submitList(questionsWithAnswers) {
                    binding.rv.scheduleLayoutAnimation()
                }
            }

            completeQuestionnaireLiveData.observe(viewLifecycleOwner) {
                binding.tvQuestionsAnswered.text = "${it.answeredQuestionsAmount} / ${it.questionsAmount}"
                onShouldDisplaySolutionChanged(shouldDisplaySolution, it)
            }

            answeredQuestionsPercentageLiveData.observe(viewLifecycleOwner) { percentage ->
                if (percentage != 100) {
                    setShouldDisplaySolution(false)
                }
                binding.progress.setProgressWithAnimation(percentage)
            }


            shouldDisplaySolutionLiveData.observe(viewLifecycleOwner) { shouldDisplay ->
                completeQuestionnaire?.let {
                    onShouldDisplaySolutionChanged(shouldDisplay, it)
                }
            }

            fragmentEventChannelFlow.awareCollect(viewLifecycleOwner) { event ->
                when (event) {
                    is ShowUndoDeleteGivenAnswersSnackBack -> {
                        showSnackBar(R.string.answersDeleted, viewToAttachTo = binding.coordRoot, actionTextRes = R.string.undo) {
                            onUndoDeleteGivenAnswersClick(event)
                        }
                    }
                    ShowPopupMenu -> {
                        PopupMenu(requireContext(), binding.btnMoreOptions).apply {
                            inflate(R.menu.quiz_popup_menu)
                            setOnMenuItemClickListener(this@FragmentQuizOverview)
                            menu.findItem(R.id.menu_item_allow_show_answers).isChecked = viewModel.shouldDisplaySolution
                            show()
                        }
                    }
                    ShowCompleteAllAnswersToast -> showToast(R.string.pleaseAnswerAllQuestionsText)
                    NavigateToQuizScreen -> navigator.navigateToQuizContainerScreen()
                }
            }
        }
    }

    private fun onShouldDisplaySolutionChanged(shouldDisplay: Boolean, completeCompleteQuestionnaire: CompleteQuestionnaireJunction) {
        val cqp = completeCompleteQuestionnaire.correctQuestionsPercentage
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

            rv.updateAllViewHolders()
        }
    }

    override fun onMenuItemClick(item: MenuItem?) = item?.let {
        when (item.itemId) {
            R.id.menu_item_delete_given_answers -> viewModel.onClearGivenAnswersClicked()
            R.id.menu_item_allow_show_answers -> viewModel.onShowSolutionClick()
            R.id.menu_item_hide_completed_questions -> {

            }
            R.id.menu_item_shuffle_questions -> {

            }
        }
        true
    } ?: false

}


//    collectWhenStarted(questionsWithAnswersFlow) {
//               rvAdapter.submitList(it){
//                    binding.rv.scheduleLayoutAnimation()
//                }
//            }
//
//            collectWhenStarted(questionnaireFlow) {
//                binding.apply {
//                    questionnaireTitle.text = it.title
//                    authorName.text = it.author
//                    courseOfStudiesText.text = it.courseOfStudies
//                    subjectName.text = it.subject
//                }
//            }
//
//            collectWhenStarted(completeQuestionnaireStateFlowOpen) {
//                binding.questionsAnsweredText.text = "${it.answeredQuestionsAmount} / ${it.questionsAmount}"
//                onShouldDisplaySolutionChanged(shouldDisplaySolution, it)
//            }
//
//
//            collectWhenStarted(answeredQuestionsPercentageFlow) { percentage ->
//                if (percentage != 100) {
//                    setShouldDisplaySolution(false)
//                }
//                binding.progress.setProgressWithAnimation(percentage)
//            }