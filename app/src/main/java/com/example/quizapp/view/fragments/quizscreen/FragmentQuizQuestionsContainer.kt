package com.example.quizapp.view.fragments.quizscreen

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.CustomTabLayoutViewBinding
import com.example.quizapp.databinding.FragmentQuizQuestionsContainerBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.datastore.QuestionnaireShuffleType.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.customimplementations.lazytablayout.ImplementedLazyTabAdapter
import com.example.quizapp.view.customimplementations.lazytablayout.LazyQuestionTab
import com.example.quizapp.view.customimplementations.lazytablayout.LazyTabLayoutMediator
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.*
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizContainerEvent.*
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.util.date.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class FragmentQuizQuestionsContainer : BindingFragment<FragmentQuizQuestionsContainerBinding>(), PopupMenu.OnMenuItemClickListener {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer: VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var vpaAdapter: VpaQuiz

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomView.isVisible = !vmContainer.isShowSolutionScreen
        initVpaAdapter()
        initViewPager()
        initClickListeners()
        initObservers()
    }

    private fun initVpaAdapter(reset: Boolean = false) {
        val questionList = vmQuiz.questionList

        if (reset) {
            val indexToSelect = questionList.indexOfFirst {
                it.id == vpaAdapter.createFragment(binding.viewPager.currentItem).questionId
            }

            vpaAdapter = VpaQuiz(this, questionList).apply {
                binding.viewPager.adapter = this
                vmContainer.lastAdapterPosition = indexToSelect
            }
        } else {
            vpaAdapter = VpaQuiz(this, questionList)
        }
    }

    private fun initViewPager() {
        binding.apply {
            viewPager.apply {
                adapter = vpaAdapter
                onPageSelected(this@FragmentQuizQuestionsContainer::onPageSelected)
                setPageTransformer(FadeOutPageTransformer())
            }

            //initRegularTabLayout()
            initLazyTabLayout()
            viewPager.setCurrentItem(vmContainer.lastAdapterPosition, false)
        }
    }

    private fun initRegularTabLayout() {
        binding.apply {
            tabLayout.attachToViewPager(viewPager) { tab, index ->
                CustomTabLayoutViewBinding.inflate(layoutInflater).apply {
                    tvNumber.text = (index + 1).toString()
                    endLine.isVisible = index != vpaAdapter.itemCount - 1
                    startLine.isVisible = index != 0
                    tab.customView = root.apply { onClick { viewPager.setCurrentItem(index, false) } }
                }
            }
        }
    }

    private fun initLazyTabLayout() {
        binding.lazyTabLayout.apply {
            disableChangeAnimation()
            setHasFixedSize(true)

            setAdapter(ImplementedLazyTabAdapter(this, vmContainer.isShowSolutionScreen) { questionId ->
                if (vmContainer.isShowSolutionScreen) {
                    vmQuiz.completeQuestionnaire?.isQuestionAnsweredCorrectly(questionId) ?: false
                } else {
                    vmQuiz.completeQuestionnaire?.isQuestionAnswered(questionId) ?: false
                }
            }.apply {
                onItemClicked = {
                    binding.viewPager.setCurrentItem(it, false)
                }
            })

            LazyTabLayoutMediator(this, binding.viewPager) { index ->
                LazyQuestionTab(vmQuiz.questionList[index].id)
            }.attach()
        }
    }


    private fun resetViewPager() {
        initVpaAdapter(true)
        initViewPager()
        updateTabs()
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
        }
    }

    private fun initObservers() {
        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                answeredQuestionsProgress.setProgressWithAnimation(it.answeredQuestionsPercentage, 200)
                tvAnsweredQuestions.text = getString(R.string.outOfAnswered, it.answeredQuestionsAmount.toString(), it.questionsAmount.toString())
            }

            changeSubmitButtonVisibility(it.areAllQuestionsAnswered)
            updateTabs()
            binding.lazyTabLayout.updateAllViewHolders()
        }

        vmContainer.fragmentEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is SelectDifferentPage -> binding.viewPager.currentItem = event.newPosition
                OnSubmitButtonClickedEvent -> navigator.navigateToQuizResultScreen()
                ShowMoreOptionsPopUpMenu -> {
                    PopupMenu(requireContext(), binding.btnMoreOptions).apply {
                        inflate(R.menu.quiz_container_popup_menu)
                        setOnMenuItemClickListener(this@FragmentQuizQuestionsContainer)
                        menu.apply {
                            findItem(R.id.menu_item_quiz_container_order_regular).isChecked = vmQuiz.shuffleType == NOT_SHUFFLED
                            findItem(R.id.menu_item_quiz_container_order_shuffle_questions).isChecked = vmQuiz.shuffleType == SHUFFLED_QUESTIONS
                            findItem(R.id.menu_item_quiz_container_order_shuffle_answers).isChecked = vmQuiz.shuffleType == SHUFFLED_ANSWERS
                            findItem(R.id.menu_item_quiz_container_order_shuffle_questions_and_answers).isChecked = vmQuiz.shuffleType == SHUFFLED_QUESTIONS_AND_ANSWERS
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
                        resetViewPager()
                    }
                }
            }
        }

        vmQuiz.shuffleTypeStateFlow.collectWhenStarted(viewLifecycleOwner) { shuffleType ->
            binding.apply {
                if (btnShuffle.tag == (shuffleType == NOT_SHUFFLED)) return@collectWhenStarted
                btnShuffle.tag = shuffleType == NOT_SHUFFLED
                updateShuffleIcon(shuffleType != NOT_SHUFFLED)
            }
        }
    }

    private fun updateShuffleIcon(shuffled: Boolean) {
        binding.btnShuffle.animate()
            .scaleX(if (shuffled) 1f else 0f)
            .scaleY(if (shuffled) 1f else 0f)
            .setInterpolator(AccelerateInterpolator())
            .setDuration(300)
            .start()
    }

    private fun changeSubmitButtonVisibility(isEverythingAnswered: Boolean) {
        binding.btnSubmit.apply {
            if ((isEverythingAnswered && translationY == 0.dp.toFloat()) || (!isEverythingAnswered && translationY == 70.dp.toFloat())) return
            clearAnimation()
            animate().translationY((if (isEverythingAnswered) 0.dp else 70.dp).toFloat())
                .setInterpolator(DecelerateInterpolator())
                .setDuration(350)
                .start()
        }
    }

    private fun onPageSelected(position: Int) {
        vmContainer.onViewPagerPageSelected(position)
        updateQuestionTypeIcon(vpaAdapter.createFragment(position).isMultipleChoice)
        updateTabBodies(position)
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

    private val tabList: List<Pair<CustomTabLayoutViewBinding, () -> Boolean>>
        get() = run {
            mutableListOf<Pair<CustomTabLayoutViewBinding, () -> Boolean>>().apply {
                binding.tabLayout.forEachTab { tab, tabIndex ->
                    val tabBinding = CustomTabLayoutViewBinding.bind(tab.customView!!)
                    val predicate = {
                        if (vmContainer.isShowSolutionScreen) {
                            vmQuiz.completeQuestionnaire?.isQuestionAnsweredCorrectly(vpaAdapter.createFragment(tabIndex).questionId) ?: false
                        } else {
                            vmQuiz.completeQuestionnaire?.isQuestionAnswered(vpaAdapter.createFragment(tabIndex).questionId) ?: false
                        }
                    }
                    add(Pair(tabBinding, predicate))
                }
            }
        }

    private fun updateTabs(position: Int = vmContainer.lastAdapterPosition) {
        tabList.apply {
            updateTabBodies(position, this)
            updateTabLines(this)
        }
    }

    private fun updateTabBodies(position: Int = 0, tabList: List<Pair<CustomTabLayoutViewBinding, () -> Boolean>> = this.tabList) =
        tabList.forEachIndexed { index, (tabBinding, predicate) ->
            val isPositionSelected = position == index

            //defaultBackgroundColor
            val tabTextColor: Int
            val tabBackgroundTint: Int
            val tabSelectedStrokeColor: ColorStateList
            val tabSelectedBackgroundColor: ColorStateList

            if (vmContainer.isShowSolutionScreen) {
                tabTextColor = if (!isPositionSelected) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                tabBackgroundTint = getColor(if (predicate()) R.color.green else R.color.red)
                tabSelectedStrokeColor = getColorStateList(tabBackgroundTint)
                tabSelectedBackgroundColor = getColorStateList(getThemeColor(R.attr.colorOnPrimary))
            } else {
                tabTextColor = if (isPositionSelected || predicate()) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                tabBackgroundTint = if (predicate()) getThemeColor(R.attr.colorAccent) else getColor(defaultBackgroundColor)
                getColorStateList(getThemeColor(R.attr.colorPrimary)).let { colorStateList ->
                    tabSelectedStrokeColor = colorStateList
                    tabSelectedBackgroundColor = colorStateList
                }
            }

            tabBinding.apply {
                tvNumber.setTextColor(tabTextColor)
                backgroundView.setBackgroundTint(tabBackgroundTint)
                selectedView.setStrokeColor(tabSelectedStrokeColor)
                selectedView.setCardBackgroundColor(tabSelectedBackgroundColor)

                val animFactor: Float = if (isPositionSelected) 1f else 0f
                if (animFactor != selectedView.scaleX) {
                    selectedView.clearAnimation()
                    selectedView.animate()
                        .scaleX(animFactor)
                        .scaleY(animFactor)
                        .alpha(animFactor)
                        .setDuration(if (isPositionSelected) 400 else 300)
                        .setInterpolator(if (isPositionSelected) DecelerateInterpolator() else AccelerateInterpolator())
                        .start()
                }
            }
        }

    private fun updateTabLines(tabList: List<Pair<CustomTabLayoutViewBinding, () -> Boolean>> = this.tabList) =
        tabList.forEachIndexed { tabIndex, (tabBindingLeft, predicateLeft) ->
            if (tabIndex != vpaAdapter.itemCount - 1) {
                val (tabBindingRight, predicateRight) = tabList[tabIndex + 1]
                val lineColor = if (vmContainer.isShowSolutionScreen) {
                    when {
                        predicateLeft() && predicateRight() -> getColor(R.color.green)
                        predicateLeft() == predicateRight() -> getColor(R.color.red)
                        else -> getColor(defaultBackgroundColor)
                    }
                } else {
                    when {
                        predicateLeft() && predicateRight() -> getThemeColor(R.attr.colorAccent)
                        else -> getColor(defaultBackgroundColor)
                    }
                }

                tabBindingLeft.endLine.setBackgroundColor(lineColor)
                tabBindingRight.startLine.setBackgroundColor(lineColor)
            }
        }


    override fun onMenuItemClick(item: MenuItem?) = item?.let {
        when (item.itemId) {
            R.id.menu_item_quiz_container_delete_given_answers -> vmContainer.onMenuItemClearGivenAnswersClicked(vmQuiz.completeQuestionnaire)
            R.id.menu_item_quiz_container_order_regular -> vmContainer.onMenuItemOrderSelected(NOT_SHUFFLED)
            R.id.menu_item_quiz_container_order_shuffle_questions -> vmContainer.onMenuItemOrderSelected(SHUFFLED_QUESTIONS)
            R.id.menu_item_quiz_container_order_shuffle_answers -> vmContainer.onMenuItemOrderSelected(SHUFFLED_ANSWERS)
            R.id.menu_item_quiz_container_order_shuffle_questions_and_answers -> vmContainer.onMenuItemOrderSelected(SHUFFLED_QUESTIONS_AND_ANSWERS)
        }
        true
    } ?: false
}