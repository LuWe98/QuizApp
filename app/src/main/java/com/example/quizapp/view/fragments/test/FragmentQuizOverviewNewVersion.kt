package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import com.example.quizapp.databinding.FragmentAddQuestionnaireNewBinding
import com.example.quizapp.extensions.log
import com.example.quizapp.extensions.onClick
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

//TODO -> Nicht nur einen Studiengang zulassen, sondern mehrere
//TODO -> Ein Fragebogen hat dann einfach eine liste von CoursesOfStudies
//TODO -> Zudem hat er eine Liste von zugehörigen Faculties


@AndroidEntryPoint
class FragmentQuizOverviewNewVersion : BindingFragment<FragmentAddQuestionnaireNewBinding>() {

    companion object {
        const val FRAGMENT_QUIZ_RESULT_KEY = "fragmentQuizResultKey"
        const val SELECTED_COURSE_OF_STUDIES_KEY = "selectedCourseOfStudiesKey"
        const val SELECTED_FACULTY_KEY = "selectedFacultyKey"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.apply {
            cosDropDown.onClick {
                navigator.navigateToCourseOfStudiesSelection(emptyList())
            }

            facultyDropDown.onClick {
                navigator.navigateToFacultySelection()
            }

            setFragmentResultListener(FRAGMENT_QUIZ_RESULT_KEY) { key, bundle ->
                if(key == FRAGMENT_QUIZ_RESULT_KEY) {
                    val cos = bundle.getStringArray(SELECTED_COURSE_OF_STUDIES_KEY)
                    val faculty = bundle.getString(SELECTED_FACULTY_KEY)

                    log("COS: ${cos?.map { it.plus(", ") }}")
                    log("Faculty $faculty")
                }
            }
        }
    }
}