package com.example.quizapp.view.fragments.addquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionWithAnswersAddQuestionnaire
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAddEdit
import com.example.quizapp.viewmodel.VmAddEdit.FragmentAddQuestionnaireEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddQuestionnaire : BindingFragment<FragmentAddQuestionnaireBinding>() {

    private val vmAdd : VmAddEdit by hiltNavDestinationViewModels(R.id.add_nav_graph)

    private lateinit var rvAdapter: RvaQuestionWithAnswersAddQuestionnaire

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListener()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            editTextName.setText(vmAdd.qTitle)
            editTextCourseOfStudies.setText(vmAdd.qCourseOfStudies)
            editTextSubject.setText(vmAdd.qSubject)
        }

        rvAdapter = RvaQuestionWithAnswersAddQuestionnaire().apply {
            onItemClick = navigator::navigateToEditQuestionScreen
        }

        binding.rv.apply {
            adapter = rvAdapter
            setHasFixedSize(true)
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(requireContext())
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
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
        binding.editTextCourseOfStudies.onTextChanged(vmAdd::onQuestionnaireCourseOfStudiesTextChanged)
        binding.editTextSubject.onTextChanged(vmAdd::onQuestionnaireSubjectTextChanged)
    }

    private fun initObservers(){
        vmAdd.questionsWithAnswersLiveData.observe(viewLifecycleOwner) {
            //binding.noDataLayout.isVisible = it.isEmpty()
            rvAdapter.submitList(it)
        }

        vmAdd.fragmentAddQuestionnaireEventChannelFlow.collect(lifecycleScope){ event ->
            when(event){
                is ShowQuestionDeletedSuccessFullySnackBar -> {
                    showSnackBar(R.string.questionDeleted, viewToAttachTo = bindingActivity.rootView, actionTextRes = R.string.undo) {
                        vmAdd.onUndoDeleteQuestionClicked(event)
                    }
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