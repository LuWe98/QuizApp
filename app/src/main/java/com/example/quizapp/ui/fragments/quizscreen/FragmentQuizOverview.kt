package com.example.quizapp.ui.fragments.quizscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizOverviewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.room.junctions.QuestionnaireWithQuestionsAndAnswers
import com.example.quizapp.recyclerview.adapters.RvaQuestionWithAnswersQuiz
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
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
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun initClickListener() {
        binding.buttonBack.setOnClickListener {
            navigator.popBackStack()
        }

        binding.fab.setOnClickListener {
            navigator.navigateToQuizContainerScreen()
        }

        binding.buttonMoreOptions.setOnClickListener {
            viewModel.onMoreOptionsItemClicked()
        }

        binding.fabCheckResults.setOnClickListener {
            viewModel.onCheckResultsClick()
        }
    }

    private fun initObservers() {
        viewModel.questionnaireLiveData.observe(viewLifecycleOwner) {
            binding.apply {
                questionnaireTitle.text = it.title
                authorName.text = it.author
                courseOfStudiesText.text = it.courseOfStudies
                subjectName.text = it.subject
            }
        }

        viewModel.questionsWithAnswersLiveData.observe(viewLifecycleOwner) { questionsWithAnswers ->
            rvAdapter.submitList(questionsWithAnswers) {
                binding.rv.scheduleLayoutAnimation()
            }
        }

        viewModel.completeQuestionnaireLiveData.observe(viewLifecycleOwner) {
            binding.questionsAnsweredText.text = "${it.answeredQuestionsAmount} / ${it.questionsAmount}"
            onShouldDisplaySolutionChanged(viewModel.shouldDisplaySolution, it)
        }

        viewModel.answeredQuestionsPercentageLiveData.observe(viewLifecycleOwner) { percentage ->
            if (percentage != 100) {
                viewModel.setShouldDisplaySolution(false)
            }
            binding.progress.setProgressWithAnimation(percentage)
        }

        viewModel.shouldDisplaySolutionLiveData.observe(viewLifecycleOwner) { shouldDisplay ->
            viewModel.completeQuestionnaire?.let {
                onShouldDisplaySolutionChanged(shouldDisplay, it)
            }
        }

        viewModel.fragmentEventChannelFlow.collect(lifecycleScope) { event ->
            when (event) {
                ShowCompleteAllAnswersToast -> {
                    showToast(R.string.pleaseAnswerAllQuestionsText)
                }
                ShowPopupMenu -> {
                    PopupMenu(requireContext(), binding.buttonMoreOptions).apply {
                        inflate(R.menu.quiz_popup_menu)
                        setOnMenuItemClickListener(this@FragmentQuizOverview)
                        show()
                    }
                }
                is ShowUndoDeleteGivenAnswersSnackBack -> {
                    showSnackBar(R.string.answersDeleted, viewToAttachTo = binding.contentContainer, actionTextRes = R.string.undo) {
                        viewModel.onUndoDeleteGivenAnswersClick(event)
                    }
                }
            }
        }
    }

    private fun onShouldDisplaySolutionChanged(shouldDisplay: Boolean, completeQuestionnaire: QuestionnaireWithQuestionsAndAnswers) {
        val cqp = completeQuestionnaire.correctQuestionsPercentage
        binding.apply {
            progressCorrect.setProgressWithAnimation(if (shouldDisplay) cqp else 0)
            progressIncorrect.setProgressWithAnimation(if (shouldDisplay) 100 - cqp else 0)
            fabCheckResults.setDrawableTint(getThemeColor(if (shouldDisplay) R.attr.colorPrimary else R.attr.colorControlActivated))

            questionsAnsweredText.isVisible = !shouldDisplay
            resultIcon.isVisible = shouldDisplay

            if(shouldDisplay){
                val isEverythingCorrect = cqp == 100
                resultIcon.setImageDrawable(if (isEverythingCorrect) R.drawable.ic_check else R.drawable.ic_cross)
                resultIcon.setDrawableTintWithRes(if (isEverythingCorrect) R.color.green else R.color.red)
            }

            rv.updateAllViewHolders()
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if(item == null) return false
        return when(item.itemId){
            R.id.menu_item_delete_given_answers -> {
                viewModel.onClearGivenAnswersClicked()
                return true
            }
            R.id.menu_item_allow_show_answers_directly -> {
                return true
            }
            R.id.menu_item_hide_completed_questions -> {
                return true
            }
            else -> false
        }
    }
}