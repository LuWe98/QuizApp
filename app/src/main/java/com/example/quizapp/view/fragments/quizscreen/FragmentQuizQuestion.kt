package com.example.quizapp.view.fragments.quizscreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizQuestionBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType.SHUFFLED_ANSWERS
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType.SHUFFLED_QUESTIONS_AND_ANSWERS
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAnswerQuiz
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizQuestionsContainer
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class FragmentQuizQuestion : BindingFragment<FragmentQuizQuestionBinding>() {

    companion object {
        private const val QUESTION_ID_KEY = "questionIdKey"
        private const val QUESTION_TYPE_KEY = "questionTypeKey"

        fun newInstance(question : Question) = FragmentQuizQuestion().apply {
            arguments = Bundle().apply {
                putString(QUESTION_ID_KEY, question.id)
                putBoolean(QUESTION_TYPE_KEY, question.isMultipleChoice)
            }
        }
    }

    val questionId : String by lazy { arguments?.getString(QUESTION_ID_KEY)!! }

    val isMultipleChoice : Boolean by lazy { arguments?.getBoolean(QUESTION_TYPE_KEY)!! }

    private val vmQuiz : VmQuiz by hiltNavGraphViewModels(R.id.quiz_nav_graph)

    private val vmContainer : VmQuizQuestionsContainer by hiltNavDestinationViewModels(R.id.fragmentQuizContainer)

    private lateinit var rvaAdapter : RvaAnswerQuiz

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvaAdapter = RvaAnswerQuiz(isMultipleChoice, vmContainer.isShowSolutionScreen).apply {
            onItemClick = { selectedAnswerId ->
                vmQuiz.onAnswerItemClicked(selectedAnswerId, questionId)
            }
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvaAdapter
            disableChangeAnimation()
        }
    }

    private fun initObservers(){
        vmQuiz.getQuestionWithAnswersFlow(questionId).collectWhenStarted(viewLifecycleOwner) {
            binding.tvQuestion.text = it.question.questionText

            when(vmQuiz.shuffleType){
                SHUFFLED_ANSWERS, SHUFFLED_QUESTIONS_AND_ANSWERS -> it.answers.shuffled(Random(vmQuiz.shuffleSeed / it.shuffleSeedAdjusted))
                else -> it.answers
            }.let(rvaAdapter::submitList)
        }
    }
}