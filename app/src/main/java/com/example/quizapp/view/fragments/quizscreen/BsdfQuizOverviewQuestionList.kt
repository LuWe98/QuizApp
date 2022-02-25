package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfQuizOverviewQuestionListBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ListLoadItemType
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaQuestionQuiz
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuiz.QuizOverviewQuestionListEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfQuizOverviewQuestionList: BindingBottomSheetDialogFragment<BsdfQuizOverviewQuestionListBinding>() {

    private val vmQuiz: VmQuiz  by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private lateinit var rvAdapter: RvaQuestionQuiz

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.etSearchQuery.setText(vmQuiz.questionSearchQuery)

        rvAdapter = RvaQuestionQuiz(vmQuiz).apply {
            onItemClick = vmQuiz::onQuestionItemClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun initListeners() {
        binding.apply {
            btnCollapse.onClick(vmQuiz::onBackButtonClicked)
            etSearchQuery.onTextChanged(vmQuiz::onQuestionSearchQueryChanged)
            btnSearch.onClick(vmQuiz::onClearSearchQueryClicked)
        }
    }

    private fun initObservers() {
        vmQuiz.questionsWithAnswersFilteredFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.tvQuestionsAmount.text = it.data.size.toString()
            it.adjustVisibilities(
                binding.rv,
                binding.dataAvailability,
                ListLoadItemType.QUESTION
            )
            rvAdapter.submitList(it.data)
        }

        vmQuiz.questionSearchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmQuiz.questionListEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                ClearQuestionQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}