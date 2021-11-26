package com.example.quizapp.view.fragments.dialogs.stringupdatedialog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R

enum class UpdateStringType constructor(
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


    val resultKey
        get() = when (this) {
            QUESTIONNAIRE_TITLE -> UPDATE_QUESTIONNAIRE_TITLE_RESULT_KEY
            QUESTIONNAIRE_SUBJECT -> UPDATE_QUESTIONNAIRE_SUBJECT_RESULT_KEY
            ADD_EDIT_ANSWER_TEXT -> UPDATE_ADD_EDIT_ANSWER_TEXT
            FACULTY_ABBREVIATION -> UPDATE_FACULTY_ABBREVIATION_RESULT_KEY
            FACULTY_NAME -> UPDATE_FACULTY_NAME_RESULT_KEY
            COURSE_OF_STUDIES_ABBREVIATION -> UPDATE_COURSE_OF_STUDIES_ABBREVIATION_RESULT_KEY
            COURSE_OF_STUDIES_NAME -> UPDATE_COURSE_OF_STUDIES_NAME_RESULT_KEY
            USER_NAME -> UPDATE_USER_NAME_RESULT_KEY
            USER_PASSWORD -> UPDATE_USER_PASSWORD_RESULT_LEY
        }

    companion object {
        const val UPDATE_QUESTIONNAIRE_TITLE_RESULT_KEY = "questionnaireTitleResultKey"
        const val UPDATE_QUESTIONNAIRE_SUBJECT_RESULT_KEY = "questionnaireSubjectResultKey"
        const val UPDATE_ADD_EDIT_ANSWER_TEXT = "updateAddEditAnswerTextResultKey"
        const val UPDATE_FACULTY_ABBREVIATION_RESULT_KEY = "facultyAbbreviationResultKey"
        const val UPDATE_FACULTY_NAME_RESULT_KEY = "facultyNameResultKey"
        const val UPDATE_COURSE_OF_STUDIES_ABBREVIATION_RESULT_KEY = "courseOfStudiesAbbreviationResultKey"
        const val UPDATE_COURSE_OF_STUDIES_NAME_RESULT_KEY = "courseOfStudiesNameResultKey"
        const val UPDATE_USER_NAME_RESULT_KEY = "userNameResultKey"
        const val UPDATE_USER_PASSWORD_RESULT_LEY = "userPasswordResultKey"
    }
}