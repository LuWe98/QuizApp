package com.example.quizapp.ui.fragments.quizscreen

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizQuestionsContainerBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import com.example.quizapp.viewpager.adapter.VpaQuiz
import com.example.quizapp.viewpager.pagetransformer.FadeOutPageTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentQuizQuestionsContainer : BindingFragment<FragmentQuizQuestionsContainerBinding>() {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer: VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var vpaAdapter: VpaQuiz

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        initClickListeners()
        initObservers()
    }

    @SuppressLint("SetTextI18n")
    private fun initViewPager() {
        vpaAdapter = VpaQuiz(this, vmQuiz.completeQuestionnaire?.questions ?: emptyList())

        binding.apply {
            viewPager.apply {
                adapter = vpaAdapter
                registerOnPageChangeCallback(pageChangeCallBack)
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
                viewPager.apply {
                    if (currentItem != 0) {
                        currentItem -= 1
                    }
                }
            }

            nextQuestionButton.setOnClickListener {
                viewPager.apply {
                    if (currentItem != vpaAdapter.itemCount - 1) {
                        currentItem += 1
                    }
                }
            }

            checkResultsButton.setOnClickListener {
                vmQuiz.setShouldDisplaySolution(true)
                navigator.popBackStack()
            }

            showSolutionButton.setOnClickListener {
                vpaAdapter.createFragment(viewPager.currentItem).questionId.let { id ->
                    vmContainer.addOrRemoveQuestionToDisplaySolution(id)
                    changeShowSolutionButtonTint(vmContainer.isQuestionIdInsideShouldDisplayList(id))
                }
            }
        }
    }

    private fun initObservers() {
        vmQuiz.allQuestionsAnsweredLiveData.observe(viewLifecycleOwner) { allAnswered ->
            binding.checkResultsButton.isVisible = allAnswered
        }
    }

    private val pageChangeCallBack: ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        @SuppressLint("SetTextI18n")
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            vmContainer.lastAdapterPosition = position
            vpaAdapter.createFragment(position).let { questionFragment ->
                binding.apply {
                    progressIndicator.setProgressWithAnimation(((position + 1) * 100f / vpaAdapter.itemCount).toInt())
                    progressText.text = "${position + 1} / ${vpaAdapter.itemCount}"
                    changeShowSolutionButtonTint(vmContainer.isQuestionIdInsideShouldDisplayList(questionFragment.questionId))
                    questionFragment.questionType.let { isMultipleChoice ->
                        questionTypeTv.text = getString(if (isMultipleChoice) R.string.multipleChoice else R.string.singleChoice)
                        questionTypeIcon.setImageDrawable(getDrawable(if (isMultipleChoice) R.drawable.ic_check_circle else R.drawable.ic_radio_button))
                    }
                }
            }
        }
    }

    private fun changeShowSolutionButtonTint(isSelected : Boolean){
        binding.apply {
            if (vmQuiz.shouldDisplaySolution) {
                //showSolutionButton.backgroundTintList = ColorStateList.valueOf(getThemeColor(R.attr.colorAccent))
                showSolutionButton.setDrawableTint(getThemeColor(R.attr.colorPrimary))
            } else {
                //showSolutionButton.backgroundTintList = ColorStateList.valueOf(if (isSelected) R.attr.colorAccent else R.attr.colorControlActivated)
                showSolutionButton.setDrawableTint(getThemeColor(if (isSelected) R.attr.colorPrimary else R.attr.colorControlActivated))
            }
        }
    }
}