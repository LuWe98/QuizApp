package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.R
import com.example.quizapp.databinding.*
import com.example.quizapp.extensions.*
import com.example.quizapp.utils.DiffCallbackUtil
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Flow
import kotlin.system.measureTimeMillis

@AndroidEntryPoint
class FragmentQuizOverviewNewVersion : BindingFragment<FragmentQuizQuestionsContainerNewBinding>() {

    private lateinit var vpaAdapter: VpaQuizTest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
    }

    private fun initViewPager() {
        vpaAdapter = VpaQuizTest(this, 50)

        binding.apply {
            viewPager.apply {
                adapter = vpaAdapter
                setPageTransformer(FadeOutPageTransformer())
                onPageSelected(this@FragmentQuizOverviewNewVersion::updateTabs)

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
        }
    }


    private fun updateTabs(selectedPosition: Int = 0) = binding.tabLayout.forEachTab { tab, index ->
        val tabBinding = CustomTabLayoutViewBinding.bind(tab.customView!!)

        val isPositionSelected = selectedPosition == index
        val isAnswered = vpaAdapter.createFragment(index).isAnswered
        //        val strokeColor = if (isAnswered) getColorStateList(getThemeColor(R.attr.colorAccent)) else getColorStateListWithRes(defaultBackgroundColor)
        val strokeColor = if (isAnswered) getColorStateList(getThemeColor(R.attr.colorAccent)) else getColorStateListWithRes(defaultBackgroundColor)
        val textColor: Int = if (isPositionSelected || isAnswered) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)

        val scale: Float = if (isPositionSelected) 1f else 0f
        val alpha: Float = if (isPositionSelected) 1f else 0f
        val duration: Long = if (isPositionSelected) 400 else 300

        checkLine(tabBinding, index)
        tabBinding.tvNumber.setTextColor(textColor)
        tabBinding.backGroundView.setStrokeColor(strokeColor)
        tabBinding.backGroundView.setCardBackgroundColor(strokeColor)
        tabBinding.selectedView.animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(alpha)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun checkLine(tabBindingLeft: CustomTabLayoutViewBinding, index: Int) {
        if (index == vpaAdapter.itemCount - 1) return

        val tabBindingRight = CustomTabLayoutViewBinding.bind(binding.tabLayout.getCustomViewAt(index + 1)!!)
        val isQuestionAnsweredLeft = vpaAdapter.createFragment(index).isAnswered
        val isQuestionAnsweredRight = vpaAdapter.createFragment(index + 1).isAnswered

        val lineColor = if (isQuestionAnsweredLeft && isQuestionAnsweredRight) getThemeColor(R.attr.colorAccent) else getColor(defaultBackgroundColor)
        tabBindingLeft.endLine.setBackgroundColor(lineColor)
        tabBindingRight.startLine.setBackgroundColor(lineColor)
    }


    class VpaQuizTest(fragment: Fragment, size: Int) : FragmentStateAdapter(fragment) {

        private val fragments = Array(size) {
            FragmentQuizQuestionNewVersion.newInstance()
        }

        override fun getItemCount() = fragments.size

        override fun createFragment(position: Int) = fragments[position]
    }
}