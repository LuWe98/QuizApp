package com.example.quizapp.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R

//TODO -> String array machen

enum class ListLoadItemType(
    @StringRes val noResultsTitleRes: Int,
    @StringRes val noResultsTextRes: Int,
    @StringRes val noDataTitleRes: Int,
    @StringRes val noDataTextRes: Int,
    @DrawableRes val icon: Int = R.drawable.ic_search_no_results
) {
    REMOTE_QUESTIONNAIRE(
        R.string.noRemoteQuestionnaireResultsFoundTitle,
        R.string.noRemoteQuestionnaireResultsFoundText,
        R.string.noRemoteQuestionnaireDataExistsTitle,
        R.string.noRemoteQuestionnaireDataExistsText
    ),
    LOCAL_QUESTIONNAIRE(
        R.string.noLocalQuestionnaireResultsFoundTitle,
        R.string.noLocalQuestionnaireResultsFoundText,
        R.string.noLocalQuestionnaireDataExistsTitle,
        R.string.noLocalQuestionnaireDataExistsText
    ),
    REMOTE_AUTHOR(
        R.string.noAuthorResultsFoundTitle,
        R.string.noAuthorResultsFoundText,
        R.string.noAuthorDataExistsTitle,
        R.string.noAuthorDataExistsText
    ),
    LOCAL_AUTHOR(
        R.string.noAuthorResultsFoundTitle,
        R.string.noAuthorResultsFoundText,
        R.string.noAuthorDataExistsTitle,
        R.string.noAuthorDataExistsText
    ),
    USER(
        R.string.noUserResultsFoundTitle,
        R.string.noUserResultsFoundText,
        R.string.noUserDataExistsTitle,
        R.string.noUserDataExistsText
    ),
    FACULTY(
        R.string.noFacultyResultsFoundTitle,
        R.string.noFacultyResultsFoundText,
        R.string.noFacultyDataExistsTitle,
        R.string.noFacultyDataExistsText
    ),
    COURSE_OF_STUDIES(
        R.string.noCourseOfStudiesResultsFoundTitle,
        R.string.noCourseOfStudiesResultsFoundText,
        R.string.noCourseOfStudiesDataExistsTitle,
        R.string.noCourseOfStudiesDataExistsText
    ),
    QUESTION(
        R.string.noQuizQuestionResultsFoundTitle,
        R.string.noQuizQuestionResultsFoundText,
        R.string.noQuizQuestionDataExistsTitle,
        R.string.noQuizQuestionDataExistsText
    )
}