package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizQuestionsContainerBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab.LazyQuestionTab
import com.example.quizapp.view.recyclerview.adapters.RvaLazyQuestionTabsLayout
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizContainerEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentQuizQuestionsContainer : BindingFragment<FragmentQuizQuestionsContainerBinding>(), PopupMenu.OnMenuItemClickListener {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer: VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var vpaAdapter: VpaQuiz

    private lateinit var lazyQuestionTabAdapter: RvaLazyQuestionTabsLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initViews()
        initClickListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            bottomView.isVisible = !vmContainer.isShowSolutionScreen
            viewPager.apply {
                setPageTransformer(FadeOutPageTransformer())
                onPageSelected { position ->
                    vmContainer.onViewPagerPageSelected(position)
                    binding.btnQuestionType.changeIconOnCondition(R.drawable.ic_check_circle, R.drawable.ic_radio_button) {
                        vpaAdapter.createFragment(position).isMultipleChoice
                    }
                }
            }

            lazyTabLayout.apply {
                disableChangeAnimation()
                setHasFixedSize(true)
                lazyQuestionTabAdapter = RvaLazyQuestionTabsLayout(binding.lazyTabLayout, vmContainer.isShowSolutionScreen) { questionId ->
                    if (vmContainer.isShowSolutionScreen) {
                        vmQuiz.completeQuestionnaire.isQuestionAnsweredCorrectly(questionId)
                    } else {
                        vmQuiz.completeQuestionnaire.isQuestionAnswered(questionId)
                    }
                }.apply {
                    onItemClicked = {
                        binding.viewPager.setCurrentItem(it, false)
                    }
                }
                adapter = lazyQuestionTabAdapter
                attachToViewPager(binding.viewPager)
            }
        }
    }

    private fun initViewPager(questions: List<Question>, reset: Boolean = false) {
        if (reset) {
            val indexToSelect = questions.indexOfFirst {
                it.id == vpaAdapter.createFragment(binding.viewPager.currentItem).questionId
            }
            vmContainer.onViewPagerPageSelected(indexToSelect)
        }
        vpaAdapter = VpaQuiz(this, questions)

        binding.viewPager.apply {
            adapter = vpaAdapter
            initLazyQuestionTabs(questions)
            setCurrentItem(vmContainer.lastAdapterPosition, false)
        }
    }

    private fun initLazyQuestionTabs(questions: List<Question>) {
        lazyQuestionTabAdapter = RvaLazyQuestionTabsLayout(binding.lazyTabLayout, vmContainer.isShowSolutionScreen) { questionId ->
            if (vmContainer.isShowSolutionScreen) {
                vmQuiz.completeQuestionnaire.isQuestionAnsweredCorrectly(questionId)
            } else {
                vmQuiz.completeQuestionnaire.isQuestionAnswered(questionId)
            }
        }.apply {
            onItemClicked = {
                binding.viewPager.setCurrentItem(it, false)
            }
        }

        binding.lazyTabLayout.apply {
            adapter = lazyQuestionTabAdapter
            attachToViewPagerAndPopulate(binding.viewPager) { index ->
                LazyQuestionTab(questions[index].id)
            }
        }
    }

    private fun initClickListeners() {
        binding.apply {
            btnBack.onClick(vmContainer::onBackButtonClicked)
            btnMoreOptions.onClick(vmContainer::onMoreOptionsClicked)
            btnShuffle.onClick(vmContainer::onShuffleButtonClicked)
            btnQuestionType.onClick(vmContainer::onQuestionTypeInfoButtonClicked)
            btnSubmit.onClick { vmContainer.onSubmitButtonClicked(vmQuiz.completeQuestionnaire.areAllQuestionsAnswered) }
        }
    }

    private fun initObservers() {
        vmQuiz.questionsCombinedStateFlow.collectWhenStarted(viewLifecycleOwner) {
            initViewPager(it)
        }

        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                answeredQuestionsProgress.setProgressWithAnimation(it.answeredQuestionsPercentage, 200)
                tvAnsweredQuestions.text = getString(R.string.outOfAnswered, it.answeredQuestionsAmount.toString(), it.questionsAmount.toString())
                tvAnswered.text = getString(R.string.outOf, it.answeredQuestionsAmount.toString(), it.questionsAmount.toString())
            }

            changeSubmitButtonVisibility(it.areAllQuestionsAnswered)
            binding.lazyTabLayout.updateAllViewHolders()
        }

        vmQuiz.shuffleTypeStateFlow.collectWhenStarted(viewLifecycleOwner) { shuffleType ->
            binding.btnShuffle.apply {
                if (tag == (shuffleType == NONE)) return@collectWhenStarted
                tag = shuffleType == NONE
                clearAnimation()
                animate()
                    .scaleX(if (shuffleType != NONE) 1f else 0f)
                    .scaleY(if (shuffleType != NONE) 1f else 0f)
                    .setInterpolator(AccelerateInterpolator())
                    .setDuration(300)
                    .start()
            }
        }

        vmContainer.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is SelectDifferentPage -> binding.viewPager.currentItem = event.newPosition
                ShowMoreOptionsPopUpMenuEvent -> {
                    PopupMenu(requireContext(), binding.btnMoreOptions).apply {
                        inflate(R.menu.quiz_container_popup_menu)
                        setOnMenuItemClickListener(this@FragmentQuizQuestionsContainer)
                        menu.apply {
                            findItem(R.id.menu_item_quiz_container_shuffle_type_none).isChecked = vmQuiz.shuffleType == NONE
                            findItem(R.id.menu_item_quiz_container_shuffle_type_questions).isChecked = vmQuiz.shuffleType == SHUFFLED_QUESTIONS
                            findItem(R.id.menu_item_quiz_container_shuffle_type_answers).isChecked = vmQuiz.shuffleType == SHUFFLED_ANSWERS
                            findItem(R.id.menu_item_quiz_container_shuffle_type_questions_and_answers).isChecked = vmQuiz.shuffleType == SHUFFLED_QUESTIONS_AND_ANSWERS
                        }
                        show()
                    }
                }
                is ShowUndoDeleteGivenAnswersSnackBack -> {
                    showSnackBar(R.string.answersDeleted, anchorView = binding.bottomView, actionTextRes = R.string.undo) {
                        vmContainer.onUndoDeleteGivenAnswersClick(event)
                    }
                }
                is ShowQuestionTypeInfoSnackBarEvent -> {
                    val textRes = if (vpaAdapter.createFragment(binding.viewPager.currentItem).isMultipleChoice) R.string.multipleChoiceQuestionInfo
                    else R.string.singleChoiceQuestionInfo
                    showSnackBar(textRes, anchorView = binding.bottomView)
                }
                is ShowMessageSnackBarEvent -> showSnackBar(event.messageRes, anchorView = binding.bottomView)
                ResetViewPagerEvent -> initViewPager(vmQuiz.questionsShuffled, true)
            }
        }
    }

    private fun changeSubmitButtonVisibility(isEverythingAnswered: Boolean) = binding.btnSubmit.apply {
        val showSubmitButton = isEverythingAnswered && !vmContainer.isShowSolutionScreen
        if ((showSubmitButton && !isVisible) || (!showSubmitButton && isVisible)) {
            animate()
                .alpha(if (showSubmitButton) 1f else 0f)
                .withEndAction {
                    if (!showSubmitButton) {
                        isVisible = false
                        isClickable = false
                        isFocusable = false
                    }
                }.withStartAction {
                    if (showSubmitButton) {
                        isVisible = true
                        isClickable = true
                        isFocusable = true

                    }
                }.setDuration(500).start()
        }
    }

    override fun onMenuItemClick(item: MenuItem?) = item?.let {
        when (item.itemId) {
            R.id.menu_item_quiz_container_delete_given_answers -> vmContainer.onMenuItemClearGivenAnswersClicked(vmQuiz.completeQuestionnaire)
            R.id.menu_item_quiz_container_shuffle_type_none -> vmContainer.onMenuItemOrderSelected(NONE)
            R.id.menu_item_quiz_container_shuffle_type_questions -> vmContainer.onMenuItemOrderSelected(SHUFFLED_QUESTIONS)
            R.id.menu_item_quiz_container_shuffle_type_answers -> vmContainer.onMenuItemOrderSelected(SHUFFLED_ANSWERS)
            R.id.menu_item_quiz_container_shuffle_type_questions_and_answers -> vmContainer.onMenuItemOrderSelected(SHUFFLED_QUESTIONS_AND_ANSWERS)
        }
        true
    } ?: false
}