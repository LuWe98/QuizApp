package com.example.quizapp.ui.fragments.addquestionnairescreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAddQuestionnaireBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.recyclerview.adapters.RvaQuestionWithAnswersAddQuestionnaire
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmAdd
import com.example.quizapp.viewmodel.VmAdd.*
import com.example.quizapp.viewmodel.VmAdd.FragmentAddQuestionnaireEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAddQuestionnaire : BindingFragment<FragmentAddQuestionnaireBinding>() {

    private val vmAdd : VmAdd by hiltNavDestinationViewModels(R.id.add_nav_graph)

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
        binding.buttonBack.setOnClickListener {
            navigator.popBackStack()
        }

        binding.fabSave.setOnClickListener {
            vmAdd.onFabSaveClicked()
        }

        binding.buttonAdd.setOnClickListener {
            vmAdd.onAddQuestionButtonClicked()
        }

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
                    showSnackBar(R.string.questionDeleted, viewToAttachTo = requireActivity().window.decorView, actionTextRes = R.string.undo) {
                        vmAdd.onUndoDeleteQuestionClicked(event)
                    }
                }
                is ShowQuestionDoesNotHaveTitleToast -> {
                    showToast("Question at position ${event.position} does not have a title!")
                }
                is ShowQuestionHasNoAnswersToast -> {
                    showToast("Question at position ${event.position} doesn't have any answers!")
                }
                NavigateBackEvent -> navigator.popBackStack()
            }
        }
    }
}