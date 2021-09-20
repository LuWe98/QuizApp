package com.example.quizapp.model.room.junctions

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Embedded
import androidx.room.Relation
import com.example.quizapp.model.room.entities.Question
import com.example.quizapp.model.room.entities.Questionnaire

data class QuestionnaireWithQuestions(
    @Embedded
    var questionnaire: Questionnaire,
    @Relation(entity = Question::class, entityColumn = "questionnaireId", parentColumn = "id")
    var questions: List<Question>
) {

    val questionsAmount: Int get() = questions.size

    //val completedQuestionsPercentage: Int get() = (questions.filter { it. }.size*100f / questionsAmount).toInt()

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<QuestionnaireWithQuestions>(){
            override fun areItemsTheSame(oldItem: QuestionnaireWithQuestions, newItem: QuestionnaireWithQuestions) =
                oldItem.questionnaire.id == newItem.questionnaire.id
            override fun areContentsTheSame(oldItem: QuestionnaireWithQuestions, newItem: QuestionnaireWithQuestions) =
                oldItem == newItem
        }
    }
}