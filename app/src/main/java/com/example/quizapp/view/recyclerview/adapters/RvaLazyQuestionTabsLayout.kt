package com.example.quizapp.view.recyclerview.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.TabLayoutViewQuestionBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab.LazyQuestionTab
import com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab.LazyQuestionTabLayout
import com.example.quizapp.view.customimplementations.quizscreen.lazyquestiontab.LazyQuestionTabLayoutAdapter

class RvaLazyQuestionTabsLayout(
    private val lazyTabLayout: LazyQuestionTabLayout,
    private val isShowSolutionScreen: Boolean,
    private val tabPredicate: ((String) -> Boolean)
) : LazyQuestionTabLayoutAdapter<LazyQuestionTab, RvaLazyQuestionTabsLayout.ImplementedLazyViewHolder>(generateDiffItemCallback(LazyQuestionTab::questionId)) {

    companion object {
        private const val SCALED_UP_FACTOR = 1.15f
        private const val SCALE_DOWN_ANIM_DURATION = 300L
        private const val SCALE_UP_ANIM_DURATION = 200L
    }

    var onItemClicked: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ImplementedLazyViewHolder(TabLayoutViewQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ImplementedLazyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ImplementedLazyViewHolder(val binding: TabLayoutViewQuestionBinding) : LazyTabLayoutViewHolder<LazyQuestionTab>(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClicked?.invoke(bindingAdapterPosition)
            }
        }

        @SuppressLint("SetTextI18n")
        override fun bind(item: LazyQuestionTab) {
            binding.apply {
                tvNumber.text = "${bindingAdapterPosition + 1}"
                endLine.isVisible = bindingAdapterPosition != itemCount - 1
                startLine.isVisible = bindingAdapterPosition != 0

                tabPredicate(item.questionId).let { predicateCurrentTab ->
                    updateTabBodies(predicateCurrentTab)
                    updateTabLines(predicateCurrentTab)
                    updateSelectedTabIndicator()
                }
            }
        }

        private fun updateTabBodies(predicateCurrent: Boolean) {
            binding.apply {
                val isPositionSelected = bindingAdapterPosition == lazyTabLayout.currentItem

                val tabTextColor: Int
                val tabBackgroundTint: Int
                val tabSelectedStrokeColor: ColorStateList
                val tabSelectedBackgroundColor: ColorStateList

                if (isShowSolutionScreen) {
                    tabTextColor = if (!isPositionSelected) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                    tabBackgroundTint = getColor(if (predicateCurrent) R.color.green else R.color.red)
                    tabSelectedStrokeColor = getColorStateList(tabBackgroundTint)
                    tabSelectedBackgroundColor = getColorStateList(getThemeColor(R.attr.colorOnPrimary))
                } else {
                    tabTextColor = if (isPositionSelected || predicateCurrent) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                    tabBackgroundTint = if (predicateCurrent) getThemeColor(R.attr.colorAccent) else getThemeColor(R.attr.colorOnPrimary)
                    getColorStateList(getThemeColor(R.attr.colorPrimary)).let { colorStateList ->
                        tabSelectedStrokeColor = colorStateList
                        tabSelectedBackgroundColor = colorStateList
                    }
                }

                tvNumber.setTextColor(tabTextColor)
                backgroundView.setBackgroundTint(tabBackgroundTint)
                selectedView.setStrokeColor(tabSelectedStrokeColor)
                selectedView.setCardBackgroundColor(tabSelectedBackgroundColor)

                val scale: Float = if (isPositionSelected) SCALED_UP_FACTOR else 0f
                val alpha: Float = if (isPositionSelected) 1f else 0f

                if (scale != selectedView.scaleX) {
                    selectedView.clearAnimation()
                    selectedView.animate()
                        .scaleX(scale)
                        .scaleY(scale)
                        .alpha(alpha)
                        .setDuration(if (isPositionSelected) SCALE_UP_ANIM_DURATION else SCALE_DOWN_ANIM_DURATION)
                        .setInterpolator(if (isPositionSelected) DecelerateInterpolator() else AccelerateInterpolator())
                        .start()
                }
            }
        }

        private fun updateSelectedTabIndicator() {
            binding.apply {
                if (lazyTabLayout.currentItem == bindingAdapterPosition) {
                    if (selectedView.scaleX == SCALED_UP_FACTOR) return
                    selectedView.clearAnimation()
                    selectedView.animate()
                        .scaleY(SCALED_UP_FACTOR)
                        .scaleX(SCALED_UP_FACTOR)
                        .alpha(1f)
                        .setDuration(SCALE_UP_ANIM_DURATION)
                        .setInterpolator(DecelerateInterpolator())
                        .start()

                    return
                }

                if (selectedView.scaleX == 0f) return
                selectedView.clearAnimation()
                selectedView.animate()
                    .scaleY(0f)
                    .scaleX(0f)
                    .alpha(0f)
                    .setDuration(SCALE_DOWN_ANIM_DURATION)
                    .setInterpolator(AccelerateInterpolator())
                    .start()
            }
        }

        private fun updateTabLines(predicateCurrent: Boolean) {
            binding.apply {
                if (bindingAdapterPosition != 0) {
                    startLine.setBackgroundColor(getTabLineColor(predicateCurrent, bindingAdapterPosition - 1))
                }
                if (bindingAdapterPosition != itemCount - 1) {
                    endLine.setBackgroundColor(getTabLineColor(predicateCurrent, bindingAdapterPosition + 1))
                }
            }
        }

        private fun getTabLineColor(predicateCurrent: Boolean, otherTabPos: Int): Int {
            val predicateOther = tabPredicate(getItem(otherTabPos).questionId)

            return if (isShowSolutionScreen) {
                when {
                    predicateOther && predicateCurrent -> binding.getColor(R.color.green)
                    predicateOther == predicateCurrent -> binding.getColor(R.color.red)
                    else -> binding.getThemeColor(R.attr.colorOnPrimary)
                }
            } else {
                when {
                    predicateOther && predicateCurrent -> binding.getThemeColor(R.attr.colorAccent)
                    else -> binding.getThemeColor(R.attr.colorOnPrimary)
                }
            }
        }
    }
}