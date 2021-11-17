package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizQuestionsContainerBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.datastore.QuestionnaireShuffleType.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaLazyQuestionTabsLayout
import com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab.LazyQuestionTab
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.*
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizContainerEvent.*
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.util.date.*
import java.util.*

@AndroidEntryPoint
class FragmentQuizQuestionsContainer : BindingFragment<FragmentQuizQuestionsContainerBinding>(), PopupMenu.OnMenuItemClickListener {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer: VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var vpaAdapter: VpaQuiz
    private lateinit var rvaLazyQuestionTabs: RvaLazyQuestionTabsLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomView.isVisible = !vmContainer.isShowSolutionScreen
        initVpaAdapter()
        initViewPager()
        initClickListeners()
        initObservers()
    }

    private fun initVpaAdapter(reset: Boolean = false) {
        if (reset) {
            val indexToSelect = vmQuiz.questionList.indexOfFirst {
                it.id == vpaAdapter.createFragment(binding.viewPager.currentItem).questionId
            }

            vpaAdapter = VpaQuiz(this, vmQuiz.questionList).apply {
                binding.viewPager.adapter = this
                vmContainer.lastAdapterPosition = indexToSelect
            }
        } else {
            vpaAdapter = VpaQuiz(this, vmQuiz.questionList)
        }
    }

    private fun initViewPager() {
        binding.apply {
            viewPager.apply {
                adapter = vpaAdapter
                onPageSelected(this@FragmentQuizQuestionsContainer::onPageSelected)
                setPageTransformer(FadeOutPageTransformer())
            }

            initLazyQuestionTabs()

            viewPager.setCurrentItem(vmContainer.lastAdapterPosition, false)
        }
    }

    private fun resetViewPager() {
        initVpaAdapter(true)
        initViewPager()
    }

    private fun initLazyQuestionTabs() {
        rvaLazyQuestionTabs = RvaLazyQuestionTabsLayout(binding.lazyTabLayout, vmContainer.isShowSolutionScreen) { questionId ->
            if (vmContainer.isShowSolutionScreen) {
                vmQuiz.completeQuestionnaire?.isQuestionAnsweredCorrectly(questionId) ?: false
            } else {
                vmQuiz.completeQuestionnaire?.isQuestionAnswered(questionId) ?: false
            }
        }.apply {
            onItemClicked = {
                binding.viewPager.setCurrentItem(it, false)
            }
        }

        binding.lazyTabLayout.apply {
            disableChangeAnimation()
            setHasFixedSize(true)
            adapter = rvaLazyQuestionTabs
            attachToViewPager(binding.viewPager) { index ->
                LazyQuestionTab(vmQuiz.questionList[index].id)
            }
        }
    }

    private fun initClickListeners() {
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            btnMoreOptions.onClick(vmContainer::onMoreOptionsClicked)
            btnSubmit.onClick { vmContainer.onSubmitButtonClicked(vmQuiz.completeQuestionnaire?.areAllQuestionsAnswered) }
            btnShuffle.onClick {
                vmQuiz.updateShuffleTypeSeed()
                resetViewPager()
            }
            btnQuestionType.onClick(vmContainer::onQuestionTypeInfoButtonClicked)
        }
    }

    private fun initObservers() {
        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                answeredQuestionsProgress.setProgressWithAnimation(it.answeredQuestionsPercentage, 200)
                tvAnsweredQuestions.text = getString(R.string.outOfAnswered, it.answeredQuestionsAmount.toString(), it.questionsAmount.toString())
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

        vmContainer.fragmentEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is SelectDifferentPage -> binding.viewPager.currentItem = event.newPosition
                OnSubmitButtonClickedEvent -> navigator.navigateToQuizResultScreen()
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
                is MenuItemOrderSelectedEvent -> {
                    launch {
                        if (vmQuiz.shuffleType == event.shuffleType) return@launch
                        vmQuiz.onMenuItemOrderSelected(event.shuffleType)
                        showSnackBar(R.string.shuffleTypeChanged, anchorView = binding.bottomView)
                        resetViewPager()
                    }
                }
                is ShowQuestionTypeInfoSnackBarEvent -> {
                    val textRes = if (vpaAdapter.createFragment(binding.viewPager.currentItem).isMultipleChoice) R.string.multipleChoiceQuestionInfo
                    else R.string.singleChoiceQuestionInfo
                    showSnackBar(textRes, anchorView = binding.bottomView)
                }
            }
        }
    }

    private fun changeSubmitButtonVisibility(isEverythingAnswered: Boolean) {
        binding.btnSubmit.apply {
            if ((isEverythingAnswered && translationY == 0.dp.toFloat()) || (!isEverythingAnswered && translationY == 65.dp.toFloat())) return
            clearAnimation()
            animate().translationY((if (isEverythingAnswered) 0.dp else 65.dp).toFloat())
                .setInterpolator(DecelerateInterpolator())
                .setDuration(350)
                .start()
        }
    }

    private fun onPageSelected(position: Int) {
        vmContainer.onViewPagerPageSelected(position)
        updateQuestionTypeIcon(vpaAdapter.createFragment(position).isMultipleChoice)
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