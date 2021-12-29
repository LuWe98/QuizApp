package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddEditQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.utils.CsvDocumentFilePicker
import com.example.quizapp.view.fragments.resultdispatcher.setFragmentResultEventListener
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAddEditQuestion
import com.example.quizapp.view.recyclerview.impl.SimpleItemTouchHelper
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.AddEditQuestionnaireEvent.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.pow

@AndroidEntryPoint
class FragmentAddEditQuestionnaire : BindingFragment<FragmentAddEditQuestionnaireBinding>(), PopupMenu.OnMenuItemClickListener {

    @Inject
    lateinit var picker: CsvDocumentFilePicker

    private val vmAddEdit: VmAddEditQuestionnaire by hiltNavDestinationViewModels(R.id.fragmentAddEditQuestionnaire)

    private lateinit var rvAdapter: RvaAddEditQuestion

    private lateinit var itemTouchHelper: SimpleItemTouchHelper

    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<ConstraintLayout>
    private lateinit var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialZAxisAnimationForReceiver()
    }

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

            itemTouchHelper = SimpleItemTouchHelper().apply {
                attachToRecyclerView(binding.bottomSheet.rv)
                onDrag = vmAddEdit::onQuestionItemDragged
                onSwiped = vmAddEdit::onQuestionItemSwiped
            }
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheet.root).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
            peekHeight = 150.dp
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
            rv.alpha = slideOffset.pow(2)

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
            tvSave.onClick(vmAddEdit::onSaveButtonClicked)
            btnBack.onClick(vmAddEdit::onBackButtonClicked)
            btnMoreOptions.onClick(vmAddEdit::onMoreOptionsClicked)

            infoCard.apply {
                addChipCos.onClick(vmAddEdit::onCourseOfStudiesButtonClicked)
                addLayout.onClick(vmAddEdit::onCourseOfStudiesButtonClicked)
                titleCard.onClick(vmAddEdit::onTitleCardClicked)
                subjectCard.onClick(vmAddEdit::onSubjectCardClicked)
                publishLayout.onClick(vmAddEdit::onPublishCardClicked)
                btnAdd.onClick(vmAddEdit::onAddQuestionButtonClicked)

                questionCard.onClick(vmAddEdit::onQuestionCardClicked)
            }

            questionDistributionCard.apply {
                btnAddQuestion.onClick(vmAddEdit::onAddQuestionButtonClicked)
                btnListQuestions.onClick(vmAddEdit::onQuestionCardClicked)
            }

            bottomSheet.apply {
                btnAdd.onClick(vmAddEdit::onAddQuestionButtonClicked)
                btnCollapse.onClick(::toggleBottomSheet)
                sheetHeader.onClick(::toggleBottomSheet)
            }
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAddEdit::onCourseOfStudiesSelectionResultReceived)

        setFragmentResultEventListener(vmAddEdit::onTitleUpdateResultReceived)

        setFragmentResultEventListener(vmAddEdit::onSubjectUpdateResultReceived)

        setFragmentResultEventListener(vmAddEdit::onCsvLoadingConfirmationResultReceived)

        vmAddEdit.coursesOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.chipGroupCos.apply {
                isVisible = it.isNotEmpty()

                setUpChipsForChipGroup(
                    it,
                    CourseOfStudies::abbreviation,
                    vmAddEdit::onCosChipClicked
                ) { cos -> showToast(cos.name) }
            }
        }

        vmAddEdit.questionnaireTitleStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.titleCard.text = if (it.isBlank()) "-" else it
        }

        vmAddEdit.questionnaireSubjectStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.subjectCard.text = if (it.isBlank()) "-" else it
        }

        vmAddEdit.publishQuestionStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.infoCard.checkBox.isChecked = it
        }

        vmAddEdit.userRoleFlow.collectWhenStarted(viewLifecycleOwner) { role ->
            binding.infoCard.publishLayout.isVisible = role != Role.USER
        }


        vmAddEdit.questionsWithAnswersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)

            binding.apply {
                bottomSheet.tvQuestionsAmount.text = it.size.toString()

                questionDistributionCard.apply {
                    allQuestions.setProgressWithAnimation(if (it.isEmpty()) 0 else 100)
                    allQuestionsNumber.text = it.size.toString()
                    infoCard.tvQuestionsAmount.text = it.size.toString()

                    val multipleChoice = it.count(QuestionWithAnswers::question / Question::isMultipleChoice)
                    progressMultipleChoice.setProgressWithAnimation((multipleChoice * 100f / it.size).toInt())
                    tvMultipleChoiceAmount.text = multipleChoice.toString()

                    val singleChoice = it.size - multipleChoice
                    progressSingleChoice.setProgressWithAnimation((singleChoice * 100f / it.size).toInt())
                    tvSingleChoiceAmount.text = singleChoice.toString()
                }
            }
        }

        vmAddEdit.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowMessageSnackBarEvent -> showSnackBar(event.messageRes)
                is ShowMessageSnackBarWithStringEvent -> showSnackBar(event.message)
                is ShowQuestionDeletedSnackBarEvent -> showSnackBar(
                    textRes = R.string.questionDeleted,
                    anchorView = binding.btnSave,
                    actionTextRes = R.string.undo,
                    actionClickEvent = { vmAddEdit.onUndoDeleteQuestionClicked(event) }
                )
                ShowPopupMenu -> {
                    PopupMenu(requireContext(), binding.btnMoreOptions).apply {
                        inflate(R.menu.add_edit_questionnaire_popup_menu)
                        setOnMenuItemClickListener(this@FragmentAddEditQuestionnaire)
                        show()
                    }
                }
                StartCsvDocumentFilePicker -> picker.startFilePicker(
                    vmAddEdit::onValidCsvFileSelected,
                    vmAddEdit::onCsvFilePickerResultReceived
                )
            }
        }
    }

    //TODO -> Clicks implementieren
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return item?.let {
            when (it.itemId) {
                R.id.menu_item_add_edit_questionnaire_load_csv -> vmAddEdit.onLoadCsvFilePopupMenuItemClicked()
            }
            true
        } ?: false
    }
}