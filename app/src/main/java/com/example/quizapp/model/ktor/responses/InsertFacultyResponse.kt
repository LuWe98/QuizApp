package com.example.quizapp.model.ktor.responses

import androidx.annotation.StringRes
import com.example.quizapp.R
import kotlinx.serialization.Serializable

@Serializable
data class InsertFacultyResponse(
    val responseType: InsertFacultyResponseType
) {
    enum class  InsertFacultyResponseType(
        @StringRes val messageRes: Int
    ) {
        SUCCESSFUL(R.string.successfullySavedFaculty),
        ABBREVIATION_ALREADY_USED(R.string.errorFacultyAbbreviationAlreadyUsed),
        NAME_ALREADY_USED(R.string.errorFacultyNameAlreadyUsed),
        NOT_ACKNOWLEDGED(R.string.errorCouldNotSaveFaculty)
    }
}
