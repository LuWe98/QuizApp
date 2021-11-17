package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddEditQuestionnaireNewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionAddEdit
import com.example.quizapp.viewmodel.VmAddEditNew
import com.example.quizapp.viewmodel.VmAddEditNew.FragmentAddEditEvent.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.pow
import kotlin.math.sqrt

@AndroidEntryPoint
class FragmentTesting : BindingFragment<FragmentAddEditQuestionnaireNewBinding>() {

    private val vmAddEdit: VmAddEditNew by viewModels()

    private lateinit var rvAdapter: RvaQuestionAddEdit

    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initBottomSheet()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            editTextName.setText(vmAddEdit.questionnaireTitle)
            editTextSubject.setText(vmAddEdit.questionnaireSubject)
        }

        rvAdapter = RvaQuestionAddEdit().apply {
            onItemClick = { pos, item ->

            }
        }

        binding.bottomSheet.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheet.root).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = 130.dp
            skipCollapsed = true
            bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(sheet: View, slideOffset: Float) = onBottomSheetSlide(slideOffset)
            }

            addBottomSheetCallback(bottomSheetCallback)
        }
    }

    private fun onBottomSheetSlide(slideOffset: Float){
        binding.bottomSheet.apply {
            rv.alpha = slideOffset.pow(2) + 0.1f

            (2.dp * slideOffset).let { newElevation ->
                sheetHeader.elevation = newElevation
                btnAdd.elevation = newElevation
            }

            btnAdd.apply {
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    verticalBias = slideOffset
                }
                scaleX = 1 + slideOffset / 3.5f
                scaleY = 1 + slideOffset / 3.5f
            }

            btnCollapse.rotation = 180 + slideOffset * 180
        }
    }



    private fun toggleBottomSheet() {
        if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initListeners() {
        binding.apply {
            cosDropDown.onClick(vmAddEdit::onCourseOfStudiesButtonClicked)
            editTextName.onTextChanged(vmAddEdit::onTitleTextChanged)
            editTextSubject.onTextChanged(vmAddEdit::onSubjectTextChanged)
        }
    }

    private fun initObservers(){
        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { _, bundle ->
            bundle.getStringArray(BsdfCourseOfStudiesSelection.SELECTED_COURSE_OF_STUDIES_KEY)?.let(vmAddEdit::onFragmentResultReceived)
        }

        vmAddEdit.coursesOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.cosDropDown.text = it.map(CourseOfStudies::abbreviation).reduceOrNull { acc, abbr -> "$acc, $abbr" } ?: ""
        }

        vmAddEdit.questionsWithAnswersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.bottomSheet.tvQuestionsAmount.text = it.size.toString()
            rvAdapter.submitList(it)
        }

        vmAddEdit.fragmentAddEditEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToCourseOfStudiesSelector -> navigator.navigateToCourseOfStudiesSelection(event.courseOfStudiesIds)
            }
        }
    }
}