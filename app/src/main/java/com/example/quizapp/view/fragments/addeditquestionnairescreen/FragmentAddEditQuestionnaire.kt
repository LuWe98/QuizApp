package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddEditQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.view.recyclerview.adapters.RvaAddEditQuestion
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.FragmentAddEditEvent.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.pow

@AndroidEntryPoint
class FragmentAddEditQuestionnaire : BindingFragment<FragmentAddEditQuestionnaireBinding>() {

    private val vmAddEdit: VmAddEditQuestionnaire by hiltNavDestinationViewModels(R.id.fragmentAddEditQuestionnaire)

    private lateinit var rvAdapter: RvaAddEditQuestion

    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<FrameLayout>
    private lateinit var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initBottomSheet()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.pageTitle.setText(vmAddEdit.pageTitleRes)

        rvAdapter = RvaAddEditQuestion().apply {
            onItemClick = vmAddEdit::onQuestionItemClicked
        }

        binding.bottomSheet.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
            addCustomItemTouchHelperCallBack().apply {
                onDrag = vmAddEdit::onQuestionItemDragged
                onSwiped = vmAddEdit::onQuestionItemSwiped
            }
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheet.root).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = 125.dp
            skipCollapsed = true
            bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(sheet: View, slideOffset: Float) = onBottomSheetSlide(slideOffset)
            }

            addBottomSheetCallback(bottomSheetCallback)
        }
    }

    private fun onBottomSheetSlide(slideOffset: Float) {
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
            btnSave.onClick(vmAddEdit::onSaveButtonClicked)
            btnBack.onClick(navigator::popBackStack)

            infoCard.apply {
                cosDropDown.onClick(vmAddEdit::onCourseOfStudiesButtonClicked)
                titleCard.onClick(vmAddEdit::onTitleCardClicked)
                subjectCard.onClick(vmAddEdit::onSubjectCardClicked)
            }

            bottomSheet.apply {
                btnAdd.onClick(vmAddEdit::onAddQuestionButtonClicked)
                btnCollapse.onClick(this@FragmentAddEditQuestionnaire::toggleBottomSheet)
                sheetHeader.onClick(this@FragmentAddEditQuestionnaire::toggleBottomSheet)
            }
        }
    }

    private fun initObservers() {
        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmAddEdit::onCourseOfStudiesUpdated)
        }

        setUpdateStringTypeListener(UpdateStringType.QUESTIONNAIRE_TITLE, vmAddEdit::onTitleUpdated)

        setUpdateStringTypeListener(UpdateStringType.QUESTIONNAIRE_SUBJECT, vmAddEdit::onSubjectUpdated)


        vmAddEdit.coursesOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.cosDropDown.text = it.map(CourseOfStudies::abbreviation).reduceOrNull { acc, abbr -> "$acc, $abbr" } ?: "-"
        }

        vmAddEdit.questionnaireTitleStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.titleCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.questionnaireSubjectStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.subjectCard.text = if(it.isBlank()) "-" else it
        }

        vmAddEdit.questionsWithAnswersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)

            binding.apply {
                bottomSheet.tvQuestionsAmount.text = it.size.toString()

                questionDistributionCard.apply {
                    allQuestions.setProgressWithAnimation(if (it.isEmpty()) 0 else 100)
                    allQuestionsNumber.text = it.size.toString()

                    val multipleChoice = it.count(QuestionWithAnswers::question / Question::isMultipleChoice)
                    progressMultipleChoice.setProgressWithAnimation((multipleChoice * 100f / it.size).toInt())
                    tvMultipleChoiceAmount.text = multipleChoice.toString()

                    val singleChoice = it.size - multipleChoice
                    progressSingleChoice.setProgressWithAnimation((singleChoice * 100f / it.size).toInt())
                    tvSingleChoiceAmount.text = singleChoice.toString()
                }
            }
        }

        vmAddEdit.fragmentAddEditEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                NavigateBackEvent -> navigator.popBackStack()
                is NavigateToCourseOfStudiesSelector -> navigator.navigateToCourseOfStudiesSelection(event.courseOfStudiesIds)
                is NavigateToUpdateStringDialog -> navigator.navigateToUpdateStringDialog(event.initialValue, event.updateType)
                is NavigateToAddEditQuestionScreenEvent -> navigator.navigateToAddEditQuestionScreen(event.position, event.questionWithAnswers)
                is ShowMessageSnackBarEvent -> showSnackBar(event.messageRes, anchorView = binding.btnSave)
                is ShowQuestionDeletedSnackBarEvent -> showSnackBar(
                    textRes = R.string.questionDeleted,
                    anchorView = binding.btnSave,
                    actionTextRes = R.string.undo,
                    actionClickEvent = { vmAddEdit.onUndoDeleteQuestionClicked(event) }
                )
            }
        }
    }
}