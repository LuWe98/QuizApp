package com.example.quizapp.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R

enum class ListLoadItemType(
    @StringRes val noResultsTitleRes: Int,
    @StringRes val noResultsTextRes: Int,
    @StringRes val noDataTitleRes: Int,
    @StringRes val noDataTextRes: Int,
    @DrawableRes val icon: Int = R.drawable.ic_search_no_results
) {
    REMOTE_QUESTIONNAIRE(
        R.string.noResults,
        R.string.tryAnotherSearchQuery,
        R.string.noResults,
        R.string.noRemoteQuestionnaireDataExistsText
    ),
    LOCAL_QUESTIONNAIRE(
        R.string.noResults,
        R.string.tryAnotherSearchQuery,
        R.string.noResults,
        R.string.noLocalQuestionnaireDataExistsText
    ),
    REMOTE_AUTHOR(
        R.string.noResults,
        R.string.tryAnotherSearchQuery,
        R.string.noResults,
        R.string.tryAnotherSearchQuery
    ),
    LOCAL_AUTHOR(
        R.string.noResults,
        R.string.tryAnotherSearchQuery,
        R.string.noResults,
        R.string.tryAnotherSearchQuery
    ),
    USER(
        R.string.noResults,
        R.string.tryAnotherSearchQuery,
        R.string.noResults,
        R.string.tryAnotherSearchQuery
    ),
    FACULTY(
        R.string.noResults,
        R.string.noFacultyResultsFoundText,
        R.string.noResults,
        R.string.noFacultyDataExistsText
    ),
    COURSE_OF_STUDIES(
        R.string.noResults,
        R.string.noCourseOfStudiesResultsFoundText,
        R.string.noResults,
        R.string.noCourseOfStudiesDataExistsText
    ),
    QUESTION(
        R.string.noResults,
        R.string.noQuizQuestionResultsFoundText,
        R.string.noResults,
        R.string.noQuizQuestionDataExistsText
    )
}