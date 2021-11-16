package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.databinding.FragmentAddQuestionnaireNewBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.onClick
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
import com.example.quizapp.viewmodel.VmAddEditNew
import com.example.quizapp.viewmodel.VmAddEditNew.FragmentAddEditEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentTesting : BindingFragment<FragmentAddQuestionnaireNewBinding>() {

    private val vmAddEdit: VmAddEditNew by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        initObservers()
    }

    private fun initClickListeners() {
        binding.apply {
            cosDropDown.onClick(vmAddEdit::onCourseOfStudiesButtonClicked)

        }
    }

    private fun initObservers(){
        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { _, bundle ->
            bundle.getStringArray(BsdfCourseOfStudiesSelection.SELECTED_COURSE_OF_STUDIES_KEY)?.let { courseOfStudiesIds ->
                vmAddEdit.setCoursesOfStudiesIds(courseOfStudiesIds.toList())
            }
        }

        vmAddEdit.coursesOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                cosDropDown.text = it.map(CourseOfStudies::abbreviation).reduceOrNull { acc, abbr -> "$acc, $abbr" } ?: ""
            }
        }

        vmAddEdit.fragmentAddEditEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToCourseOfStudiesSelector -> navigator.navigateToCourseOfStudiesSelection(event.courseOfStudiesIds)
            }
        }
    }
}