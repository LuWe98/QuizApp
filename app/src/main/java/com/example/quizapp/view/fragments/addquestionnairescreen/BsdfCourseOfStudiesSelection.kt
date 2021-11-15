package com.example.quizapp.view.fragments.addquestionnairescreen

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.viewpager2.widget.ViewPager2
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfCourseOfStudiesSelectionBinding
import com.example.quizapp.databinding.TabLayoutViewFacultyBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFullScreenBottomSheetDialogFragment
import com.example.quizapp.view.fragments.test.FragmentQuizOverviewNewVersion
import com.example.quizapp.view.viewpager.adapter.VpaCourseOfStudiesSelection
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.*
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

@AndroidEntryPoint
class BsdfCourseOfStudiesSelection : BindingFullScreenBottomSheetDialogFragment<BsdfCourseOfStudiesSelectionBinding>() {

    private val vmCos: VmCourseOfStudiesSelection by hiltNavDestinationViewModels(R.id.bsdfCourseOfStudiesSelection)

    private lateinit var vpAdapter: VpaCourseOfStudiesSelection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
        initObservers()
    }

    private fun initViews() {
        val facultyList = runBlocking { vmCos.facultyFlow.first() }
        vpAdapter = VpaCourseOfStudiesSelection(this, facultyList)

        binding.viewPager.apply {
            adapter = vpAdapter
            setPageTransformer(FadeOutPageTransformer())

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    updateTabs(position)
                }
            })
        }

        binding.tabLayout.attachToViewPager(binding.viewPager) { tab, index ->
            TabLayoutViewFacultyBinding.inflate(layoutInflater).apply {
                tvText.text = facultyList[index].abbreviation
                tab.customView = root
                tab.view.onClick {
                    binding.viewPager.setCurrentItem(index, false)
                }
            }
        }
    }

    private fun updateTabs(newPosition: Int){
        binding.tabLayout.forEachTab { tab, i ->
            val factors = if (i == newPosition) 1f else 0f
            val duration = if (i == newPosition) 350L else 150L

            TabLayoutViewFacultyBinding.bind(tab.customView!!).let { tabBinding ->
                val textColor = if(i == newPosition) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                tabBinding.tvText.setTextColor(textColor)

                tabBinding.selectedView.animate()
                    .scaleX(factors)
                    .scaleY(factors)
                    .alpha(factors)
                    .setDuration(duration)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun initClickListeners(){
        binding.apply {
            btnConfirm.onClick(vmCos::onConfirmButtonClicked)
        }
    }

    private fun initObservers() {
        vmCos.courseOfStudiesSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ConfirmationEvent -> {
                    setFragmentResult(FragmentQuizOverviewNewVersion.FRAGMENT_QUIZ_RESULT_KEY, Bundle().apply {
                        putString(FragmentQuizOverviewNewVersion.SELECTED_FACULTY_KEY, "Faculty Test 2")
                        putStringArray(FragmentQuizOverviewNewVersion.SELECTED_COURSE_OF_STUDIES_KEY, event.selectedCoursesOfStudiesIds.toTypedArray())
                    })
                    navigator.popBackStack()
                }
                ItemClickedEvent -> {
                    log("CLICKED!")
                }
            }
        }
    }
}