package com.example.quizapp.model.datastore

import androidx.annotation.StringRes
import com.example.quizapp.R

enum class QuestionnaireShuffleType(@StringRes val textRes: Int ) {
    NONE(R.string.shuffleTypeNone),
    SHUFFLED_QUESTIONS(R.string.shuffleTypeQuestions),
    SHUFFLED_ANSWERS(R.string.shuffleTypeAnswers),
    SHUFFLED_QUESTIONS_AND_ANSWERS(R.string.shuffleTypeQuestionsAndAnswersAbbr)
}