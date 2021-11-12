package com.example.quizapp.view.fragments.quizscreen

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.CustomTabLayoutViewBinding
import com.example.quizapp.databinding.FragmentQuizQuestionsContainerBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer.FragmentQuizOverviewEvent.*
import com.example.quizapp.view.viewpager.adapter.VpaQuiz
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class FragmentQuizQuestionsContainer : BindingFragment<FragmentQuizQuestionsContainerBinding>() {

    private val vmQuiz: VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer: VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var vpaAdapter: VpaQuiz

    //TODO -> Das über ARGS Übergeben, dass man von dem result und overview SCREEN wieder hier daufkommen kann

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
        initObservers()
    }

    private fun initViews() {
        binding.bottomView.isVisible = !vmContainer.isShowSolutionScreen


        vpaAdapter = VpaQuiz(this, vmQuiz.completeQuestionnaire?.allQuestions ?: emptyList())

        binding.apply {
            viewPager.apply {
                adapter = vpaAdapter
                onPageSelected(this@FragmentQuizQuestionsContainer::onPageSelected)
                setPageTransformer(FadeOutPageTransformer())
            }

            tabLayout.attachToViewPager(viewPager) { tab, index ->
                CustomTabLayoutViewBinding.inflate(layoutInflater).let { tabBinding ->
                    tabBinding.tvNumber.text = (index + 1).toString()
                    tabBinding.endLine.isVisible = index != vpaAdapter.itemCount - 1
                    tabBinding.startLine.isVisible = index != 0
                    tab.customView = tabBinding.root.apply {
                        onClick { viewPager.setCurrentItem(index, false) }
                    }
                }
            }

            viewPager.setCurrentItem(vmContainer.lastAdapterPosition, false)
        }
    }

    private fun initClickListeners() {
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            btnMoreOptions.onClick(vmContainer::onMoreOptionsClicked)
            btnSubmit.onClick { vmContainer.onSubmitButtonClicked(vmQuiz.completeQuestionnaire?.areAllQuestionsAnswered) }
        }
    }

    private fun initObservers() {
        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) { statistics ->
            binding.apply {
                answeredQuestionsProgress.setProgressWithAnimation(statistics.answeredQuestionsPercentage, 200)
                tvAnsweredQuestions.text = getString(R.string.outOfAnswered, statistics.answeredQuestionsAmount.toString(), statistics.questionsAmount.toString())
            }

            changeSubmitButtonVisibility(statistics.areAllQuestionsAnswered)
            updateTabLines()
        }

        vmContainer.fragmentEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is SelectDifferentPage -> binding.viewPager.currentItem = event.newPosition
                OnSubmitButtonClickedEvent -> navigator.navigateToQuizResultScreen()
                ShowMoreOptionsPopUpMenu -> {
                    //TODO -> Popup Zeigen
                    showToast("SHOW POPUP")
                }
            }
        }
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
        updateTabs(position)
    }

    private fun updateQuestionTypeIcon(isMultipleChoice: Boolean) {
        binding.ivQuestionType.apply {
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
        launch(IO) {
            tabList.apply {
                updateTabBodies(position, this)
                updateTabLines(this)
            }
        }
    }

    private suspend fun updateTabBodies(position: Int = 0, tabList: List<Pair<CustomTabLayoutViewBinding, () -> Boolean>> = this.tabList) =
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
                tabSelectedBackgroundColor = getColorStateList(getColor(defaultBackgroundColor))
            } else {
                tabTextColor = if (isPositionSelected || predicate()) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                tabBackgroundTint = if (predicate()) getThemeColor(R.attr.colorAccent) else getColor(defaultBackgroundColor)
                getColorStateList(getThemeColor(R.attr.colorPrimary)).let { colorStateList ->
                    tabSelectedStrokeColor = colorStateList
                    tabSelectedBackgroundColor = colorStateList
                }
            }

            tabBinding.apply {
                withContext(Main) {
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
        }

    private suspend fun updateTabLines(tabList: List<Pair<CustomTabLayoutViewBinding, () -> Boolean>> = this.tabList) =
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

                withContext(Main) {
                    tabBindingLeft.endLine.setBackgroundColor(lineColor)
                    tabBindingRight.startLine.setBackgroundColor(lineColor)
                }
            }
        }
}