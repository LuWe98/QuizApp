package com.example.quizapp.ui.fragments.quizscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizQuestionsContainerBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizOverviewEvent.*
import com.example.quizapp.viewpager.adapter.VpaQuiz
import com.example.quizapp.viewpager.pagetransformer.FadeOutPageTransformer
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FragmentQuizQuestionsContainer : BindingFragment<FragmentQuizQuestionsContainerBinding>() {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer: VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var vpaAdapter: VpaQuiz

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
        initObservers()
    }

    private fun initViews() {
        vpaAdapter = VpaQuiz(this, vmQuiz.completeQuestionnaire?.questions ?: emptyList())

        binding.apply {
            viewPager.apply {
                adapter = vpaAdapter
                onPageSelected(onPageSelected)
                setPageTransformer(FadeOutPageTransformer())
                setCurrentItem(vmContainer.lastAdapterPosition, false)
            }

            progressText.text = "${vmContainer.lastAdapterPosition + 1} / ${vpaAdapter.itemCount}"
        }
    }

    private fun initClickListeners() {
        binding.apply {
            buttonBack.setOnClickListener {
                navigator.popBackStack()
            }

            previousQuestionButton.setOnClickListener {
                vmContainer.onSelectPreviousPageButtonClicked()
            }

            nextQuestionButton.setOnClickListener {
                vmContainer.onSelectNextPageButtonClicked(vpaAdapter)
            }

            checkResultsButton.setOnClickListener {
                vmContainer.onCheckResultsButtonClicked()
            }

            showSolutionButton.setOnClickListener {
                vmContainer.onShowSolutionButtonClicked(vpaAdapter)
                //vmQuiz.onCheckResultsClick()
            }
        }
    }

    private fun initObservers() {
        vmQuiz.allQuestionsAnsweredLiveData.observe(viewLifecycleOwner) { allAnswered ->
            binding.checkResultsButton.isVisible = allAnswered
        }

        vmContainer.fragmentEventChannelFlow.collect(lifecycleScope) { event ->
            when(event){
                is SelectDifferentPage -> binding.viewPager.currentItem = event.newPosition
                is ChangeSolutionButtonTint -> changeShowSolutionButtonTint(event.show)
                CheckResultsEvent -> {
                    vmQuiz.setShouldDisplaySolution(true)
                    navigator.popBackStack()
                }
            }
        }
    }

    private val onPageSelected: ((Int) -> (Unit)) = { position ->
        vmContainer.onViewPagerPageSelected(position)

        binding.apply {
            progressText.text = "${position + 1} / ${vpaAdapter.itemCount}"
            progressIndicator.setProgressWithAnimation(((position + 1) * 100f / vpaAdapter.itemCount).toInt())

            vpaAdapter.createFragment(position).let { currentFragment ->
                changeShowSolutionButtonTint(vmContainer.shouldDisplayQuestionSolution(currentFragment.questionId))
                currentFragment.isMultipleChoice.let {
                    questionTypeTv.text = getString(if (it) R.string.multipleChoice else R.string.singleChoice)
                    questionTypeIcon.setImageDrawable(if (it) R.drawable.ic_check_circle else R.drawable.ic_radio_button)
                }
            }
        }
    }

    private fun changeShowSolutionButtonTint(isSelected: Boolean) {
        binding.apply {
            if (vmQuiz.shouldDisplaySolution) {
                showSolutionButton.setDrawableTint(getThemeColor(R.attr.colorPrimary))
            } else {
                showSolutionButton.setDrawableTint(getThemeColor(if (isSelected) R.attr.colorPrimary else R.attr.colorControlActivated))
            }
        }
    }
}