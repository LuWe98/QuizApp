package com.example.quizapp.view.fragments.addeditquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionAddEdit
import com.example.quizapp.viewmodel.VmAddEdit
import com.example.quizapp.viewmodel.VmAddEdit.FragmentAddQuestionnaireEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddQuestionnaire : BindingFragment<FragmentAddQuestionnaireBinding>() {

    private val vmAdd : VmAddEdit by hiltNavDestinationViewModels(R.id.add_nav_graph)

    private lateinit var rvAdapter: RvaQuestionAddEdit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListener()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            pageTitle.setText(vmAdd.providePageTitle())
            editTextName.setText(vmAdd.qTitle)
            editTextCourseOfStudies.setText(vmAdd.qCoursesOfStudies.map(CourseOfStudies::abbreviation).reduce { acc, abbr -> "$acc, $abbr" })
            editTextSubject.setText(vmAdd.qSubject)
        }

        rvAdapter = RvaQuestionAddEdit().apply {
            onItemClick = navigator::navigateToEditQuestionScreen
        }

        binding.rv.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
            addCustomItemTouchHelperCallBack().apply {
                onSwiped = vmAdd::onQuestionItemSwiped
                onDrag = rvAdapter::moveItem
                onDragReleased = { vmAdd.onQuestionItemDragReleased(rvAdapter.currentList) }
            }
        }
    }

    private fun initClickListener(){
        binding.buttonBack.onClick(navigator::popBackStack)
        binding.fabSave.onClick(vmAdd::onFabSaveClicked)
        binding.buttonAdd.onClick(vmAdd::onAddQuestionButtonClicked)

        binding.editTextName.onTextChanged(vmAdd::onQuestionnaireTitleTextChanged)
        binding.editTextSubject.onTextChanged(vmAdd::onQuestionnaireSubjectTextChanged)
    }

    private fun initObservers(){
        vmAdd.questionsWithAnswersLiveData.observe(viewLifecycleOwner) {
            //binding.noDataLayout.isVisible = it.isEmpty()
            rvAdapter.submitList(it)
        }

        vmAdd.fragmentAddQuestionnaireEventChannelFlow.collectWhenStarted(viewLifecycleOwner){ event ->
            when(event){
                is ShowQuestionDeletedSuccessFullySnackBar -> {
                    showSnackBar(
                        textRes = R.string.questionDeleted,
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmAdd.onUndoDeleteQuestionClicked(event) }
                    )
                }
                is ShowQuestionDoesNotHaveTitleToast -> {
                    showToast(getString(R.string.errorQuestionDoesNotHaveTitle, event.position.toString()))
                }
                is ShowQuestionHasNoAnswersToast -> {
                    showToast(getString(R.string.errorQuestionsHasNoAnswers, event.position.toString()))
                }
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}