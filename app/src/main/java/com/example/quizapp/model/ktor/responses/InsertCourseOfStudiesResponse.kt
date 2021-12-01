package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.serialization.Serializable

@Serializable
data class InsertCourseOfStudiesResponse(
    val responseType: InsertCourseOfStudiesResponseType
) {
    enum class  InsertCourseOfStudiesResponseType(
        @StringRes val messageRes: Int
    ) {
        SUCCESSFUL(R.string.successfullySavedCourseOfStudies),
        ABBREVIATION_ALREADY_USED(R.string.errorCourseOfStudiesAbbreviationAlreadyUsed),
        NOT_ACKNOWLEDGED(R.string.errorCouldNotSaveCourseOfStudies)
    }
}
