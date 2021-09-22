package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizQuestionBinding
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.updateAllViewHolders
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.view.recyclerview.adapters.RvaAnswerQuiz
import com.example.quizapp.view.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentQuizQuestion : BindingFragment<FragmentQuizQuestionBinding>() {

    val questionId : Long by lazy { requireArguments().getLong(QUESTION_ID_KEY) }
    val isMultipleChoice : Boolean by lazy { requireArguments().getBoolean(QUESTION_TYPE_KEY) }

    private val vmQuiz : VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer : VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var rvaAdapter : RvaAnswerQuiz


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvaAdapter = RvaAnswerQuiz(vmQuiz, vmContainer, isMultipleChoice).apply {
            onItemClick = vmContainer::onAnswerItemClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvaAdapter
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun initObservers(){
        vmQuiz.getQuestionWithAnswersLiveData(questionId).observe(viewLifecycleOwner) {
            binding.tvQuestion.text = it.question.text
            rvaAdapter.submitList(it.answers)
        }

//        vmQuiz.shouldDisplaySolutionLiveData.observe(viewLifecycleOwner) {
//            binding.rv.updateAllViewHolders()
//        }

        vmContainer.questionIdLiveData(questionId).observe(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }
    }

    companion object {
        private const val QUESTION_ID_KEY = "questionIdKey"
        private const val QUESTION_TYPE_KEY = "questionTypeKey"

        fun newInstance(question : Question) = FragmentQuizQuestion().apply {
            arguments = Bundle().apply {
                putLong(QUESTION_ID_KEY, question.id)
                putBoolean(QUESTION_TYPE_KEY, question.isMultipleChoice)
            }
        }
    }
}