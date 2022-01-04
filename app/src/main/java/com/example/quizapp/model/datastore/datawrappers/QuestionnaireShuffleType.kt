package com.example.quizapp.model.datastore.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class QuestionnaireShuffleType(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int = R.drawable.ic_shuffle_new,
    @StringRes val titleRes: Int
): SelectionTypeItemMarker<QuestionnaireShuffleType> {

    NONE(
        textRes = R.string.shuffleTypeNone,
        titleRes = R.string.shuffleTypeNone,
        iconRes = R.drawable.ic_cross
    ),
    SHUFFLED_QUESTIONS(
        textRes = R.string.shuffleTypeQuestions,
        titleRes = R.string.shuffleTypeQuestions
    ),
    SHUFFLED_ANSWERS(
        textRes = R.string.shuffleTypeAnswers,
        titleRes = R.string.shuffleTypeAnswers
    ),
    SHUFFLED_QUESTIONS_AND_ANSWERS(
        textRes = R.string.shuffleTypeQuestionsAndAnswersAbbr,
        titleRes = R.string.shuffleTypeQuestionsAndAnswers
    );

}