package com.example.quizapp.view.fragments.resultdispatcher.requests

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.fragments.resultdispatcher.UpdateStringValueResult
import com.example.quizapp.view.fragments.resultdispatcher.UpdateStringValueResult.*

enum class UpdateStringRequestType constructor(
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
    ),
    ADD_EDIT_ANSWER_TEXT(
        R.drawable.ic_title,
        R.string.answerText,
        R.string.updateAnswerText
    ),
    FACULTY_ABBREVIATION(
        R.drawable.ic_title,
        R.string.abbreviation,
        R.string.updateFacultyAbbreviation
    ),
    FACULTY_NAME(
        R.drawable.ic_faculty,
        R.string.name,
        R.string.updateFacultyName
    ),
    COURSE_OF_STUDIES_ABBREVIATION(
        R.drawable.ic_title,
        R.string.abbreviation,
        R.string.updateCourseOfStudiesAbbreviation
    ),
    COURSE_OF_STUDIES_NAME(
        R.drawable.ic_course_of_studies,
        R.string.name,
        R.string.updateCourseOfStudiesName
    ),
    USER_NAME(
        R.drawable.ic_person,
        R.string.userName,
        R.string.updateUserName
    ),
    USER_PASSWORD(
        R.drawable.ic_password,
        R.string.password,
        R.string.updatePassword
    );

    fun generateFragmentResult(newString: String): UpdateStringValueResult = when(this) {
        QUESTIONNAIRE_TITLE -> QuestionnaireTitleUpdateResult(newString)
        QUESTIONNAIRE_SUBJECT -> QuestionnaireSubjectUpdateResult(newString)
        ADD_EDIT_ANSWER_TEXT -> AddEditQuestionAnswerTextUpdateResult(newString)
        FACULTY_ABBREVIATION -> AddEditFacultyAbbreviationUpdateResult(newString)
        FACULTY_NAME -> AddEditFacultyNameUpdateResult(newString)
        COURSE_OF_STUDIES_ABBREVIATION -> AddEditCourseOfStudiesAbbreviationUpdateResult(newString)
        COURSE_OF_STUDIES_NAME -> AddEditCourseOfStudiesNameUpdateResult(newString)
        USER_NAME -> AddEditUserNameUpdateResult(newString)
        USER_PASSWORD -> AddEditUserPasswordUpdateResult(newString)
    }
}