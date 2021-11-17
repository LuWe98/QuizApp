package com.example.quizapp.view.fragments.dialogs.stringupdatedialog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R

enum class DfUpdateStringValueType constructor(
    @DrawableRes val iconRes: Int,
    @StringRes val hintRes: Int,
    @StringRes val titleRes: Int
) {
    QUESTIONNAIRE_TITLE(
        R.drawable.ic_title,
        R.string.title,
        R.string.updateQuestionnaireTitle
    ),
    QUESTIONNAIRE_SUBJECT(
        R.drawable.ic_subject,
        R.string.subject,
        R.string.updateQuestionnaireSubject
    );

    val resultKey get() = when(this) {
        QUESTIONNAIRE_TITLE -> UPDATE_QUESTIONNAIRE_TITLE_RESULT_KEY
        QUESTIONNAIRE_SUBJECT -> UPDATE_QUESTIONNAIRE_SUBJECT_RESULT_KEY
    }

    companion object {
        const val UPDATE_QUESTIONNAIRE_TITLE_RESULT_KEY = "questionnaireTitleResultKey"
        const val UPDATE_QUESTIONNAIRE_SUBJECT_RESULT_KEY = "questionnaireSubjectResultKey"
    }
}