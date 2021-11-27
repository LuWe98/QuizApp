package com.example.quizapp.view.fragments.dialogs.courseofstudiesselection

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.setFragmentResult
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfCourseOfStudiesSelectionBinding
import com.example.quizapp.databinding.TabLayoutViewFacultyBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.viewpager.adapter.VpaCourseOfStudiesSelection
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection.CourseOfStudiesSelectionEvent.ConfirmationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class BsdfCourseOfStudiesSelection : BindingBottomSheetDialogFragment<BsdfCourseOfStudiesSelectionBinding>() {

    companion object {
        const val COURSE_OF_STUDIES_RESULT_KEY = "courseOfStudiesResultKey"
    }

    private val vmCos: VmCourseOfStudiesSelection by hiltNavDestinationViewModels(R.id.bsdfCourseOfStudiesSelection)

    private lateinit var vpAdapter: VpaCourseOfStudiesSelection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initClickListeners()
        initObservers()
    }

    private fun initViews() {
        binding.etSearchQuery.setText(vmCos.searchQuery)

        val facultyList = runBlocking(IO) { vmCos.facultyFlow.first() }
        vpAdapter = VpaCourseOfStudiesSelection(this, facultyList)

        binding.viewPager.apply {
            adapter = vpAdapter
            setPageTransformer(FadeOutPageTransformer())
            onPageSelected { position ->
                updateTabs(position)
                changeTitleWithAnimation(facultyList[position])
            }
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

    private fun changeTitleWithAnimation(faculty: Faculty){
        binding.apply {
            val duration = if(tvTitle.text.isEmpty()) 0L else 150L
            tvTitle.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction {
                    tvTitle.text = faculty.name
                    tvTitle.animate()
                        .alpha(1f)
                        .setDuration(duration)
                        .start()
                }.start()
        }
    }

    private fun updateTabs(newPosition: Int){
        binding.tabLayout.forEachTab { tab, i ->
            val factors = if (i == newPosition) 1f else 0f
            val duration = if (i == newPosition) 300L else 150L

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
            etSearchQuery.onTextChanged(vmCos::onSearchQueryChanged)
            btnSearch.onClick(vmCos::onClearSearchQueryClicked)
        }
    }

    private fun initObservers() {
        vmCos.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner){
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmCos.courseOfStudiesSelectionEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ConfirmationEvent -> {
                    setFragmentResult(COURSE_OF_STUDIES_RESULT_KEY, Bundle().apply {
                        putStringArray(COURSE_OF_STUDIES_RESULT_KEY, event.courseOfStudiesIds)
                    })
                    navigator.popBackStack()
                }
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}