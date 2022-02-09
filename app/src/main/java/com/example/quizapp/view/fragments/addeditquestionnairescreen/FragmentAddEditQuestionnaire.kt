package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddEditQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.utils.CsvDocumentFilePicker
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudiesChoice
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.AddEditQuestionnaireEvent.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentAddEditQuestionnaire : BindingFragment<FragmentAddEditQuestionnaireBinding>(), PopupMenu.OnMenuItemClickListener {

    @Inject
    lateinit var picker: CsvDocumentFilePicker

    private val vmAddEdit: VmAddEditQuestionnaire by hiltNavDestinationViewModels(R.id.fragmentAddEditQuestionnaire)

    private lateinit var rvaCos: RvaCourseOfStudiesChoice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initMaterialZAxisAnimationForReceiver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.pageTitle.setText(vmAddEdit.pageTitleRes)
        binding.contentLayout.apply {
            editTextTitle.setText(vmAddEdit.questionnaireTitle)
            editTextSubject.setText(vmAddEdit.questionnaireSubject)
        }

        rvaCos = RvaCourseOfStudiesChoice().apply {
            onDeleteButtonClicked = vmAddEdit::onCourseOfStudiesDeleteButtonClicked
        }

        binding.contentLayout.rvCos.apply {
            adapter = rvaCos
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }
    }

    private fun initListeners() {
        binding.apply {
            tvSave.onClick(vmAddEdit::onSaveButtonClicked)
            btnBack.onClick(vmAddEdit::onBackButtonClicked)
            btnMoreOptions.onClick(vmAddEdit::onMoreOptionsClicked)

            contentLayout.apply {
                //btnClearCos.onClick(vmAddEdit::onClearCourseOfStudiesClicked)
                btnAddCos.onClick(vmAddEdit::onCourseOfStudiesButtonClicked)
                publishLayout.onClick(vmAddEdit::onPublishCardClicked)
                editTextTitle.onTextChanged(vmAddEdit::onTitleUpdated)
                editTextSubject.onTextChanged(vmAddEdit::onSubjectUpdated)
            }

            //TODO -> Question Dialog nochmal Testen aber mit Look von dem jetzigen BottomSheet
            contentLayout.apply {
                btnAddQuestion.onClick(vmAddEdit::onAddQuestionButtonClicked)
                btnListQuestions.onClick(vmAddEdit::onQuestionCardClicked)
            }
        }
    }

    private fun initObservers() {

        setFragmentResultEventListener(vmAddEdit::onCourseOfStudiesSelectionResultReceived)

        setFragmentResultEventListener(vmAddEdit::onCsvLoadingConfirmationResultReceived)

        vmAddEdit.coursesOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rvaCos.submitList(it) {
                binding.contentLayout.tvNoAssigned.isVisible = it.isEmpty()
            }
        }

        vmAddEdit.publishQuestionStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.contentLayout.checkBox.isChecked = it
            binding.contentLayout.publishSwitch.isChecked = it
        }

        vmAddEdit.userRoleFlow.collectWhenStarted(viewLifecycleOwner) { role ->
            binding.contentLayout.publishLayout.isVisible = role != Role.USER
        }


        vmAddEdit.questionsWithAnswersStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                contentLayout.apply {
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

        vmAddEdit.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowMessageSnackBarEvent -> showSnackBar(event.messageRes)
                is ShowMessageSnackBarWithStringEvent -> showSnackBar(event.message)
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
                is SetQuestionnaireSubject -> binding.contentLayout.editTextSubject.setText(event.subject)
                is SetQuestionnaireTitle -> binding.contentLayout.editTextTitle.setText(event.title)
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