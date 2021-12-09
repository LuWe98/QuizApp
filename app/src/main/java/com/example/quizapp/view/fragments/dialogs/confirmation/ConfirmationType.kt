package com.example.quizapp.view.fragments.dialogs.confirmation

import android.os.Parcelable
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import kotlinx.parcelize.Parcelize

sealed class ConfirmationType(
    val resultKey: String,
    @StringRes val titleRes: Int = R.string.deletionConfirmationTile,
    @StringRes val textRes: Int,
    @StringRes val positiveButtonRes: Int = R.string.confirm,
    @StringRes val negativeButtonRes: Int = R.string.cancel
): Parcelable {

    companion object {
        const val CONFIRMATION_RESULT_KEY_DELETE_USER = "deleteUserConfirmationResultKey"
        const val CONFIRMATION_RESULT_KEY_DELETE_FACULTY = "deleteFacultyConfirmationResultKey"
        const val CONFIRMATION_RESULT_KEY_DELETE_COURSE_OF_STUDIES = "deleteCourseOfStudiesResultKey"
        const val CONFIRMATION_RESULT_KEY_LOGOUT = "logoutConfirmationResultKey"
        const val CONFIRMATION_RESULT_KEY_LOAD_CSV_FILE = "loadCsvFileDataConfirmationResultKey"

        inline fun <reified ResultType: ConfirmationType> getResultKeyWithResultType() = when(ResultType::class) {
            DeleteUserConfirmation::class -> CONFIRMATION_RESULT_KEY_DELETE_USER
            DeleteFacultyConfirmation::class -> CONFIRMATION_RESULT_KEY_DELETE_FACULTY
            DeleteCourseOfStudiesConfirmation::class -> CONFIRMATION_RESULT_KEY_DELETE_COURSE_OF_STUDIES
            LogoutConfirmation::class -> CONFIRMATION_RESULT_KEY_LOGOUT
            LoadCsvFileConfirmation::class -> CONFIRMATION_RESULT_KEY_LOAD_CSV_FILE
            else -> throw IllegalStateException("Result key not configured for class '${ResultType::class.simpleName}'")
        }
    }

    @Parcelize
    data class DeleteUserConfirmation(val user: User): ConfirmationType(
        resultKey = CONFIRMATION_RESULT_KEY_DELETE_USER,
        textRes = R.string.warningUserDeletion
    )

    @Parcelize
    data class DeleteFacultyConfirmation(val faculty: Faculty): ConfirmationType(
        resultKey = CONFIRMATION_RESULT_KEY_DELETE_FACULTY,
        textRes = R.string.warningFacultyDeletion
    )

    @Parcelize
    data class DeleteCourseOfStudiesConfirmation(val courseOfStudies: CourseOfStudies): ConfirmationType(
        resultKey = CONFIRMATION_RESULT_KEY_DELETE_COURSE_OF_STUDIES,
        textRes = R.string.warningCourseOfStudiesDeletion
    )

    @Parcelize
    object LogoutConfirmation: ConfirmationType(
        resultKey = CONFIRMATION_RESULT_KEY_LOGOUT,
        titleRes = R.string.logoutWarningTitle,
        textRes = R.string.logoutWarning,
        positiveButtonRes = R.string.logout
    )

    @Parcelize
    object LoadCsvFileConfirmation: ConfirmationType(
        resultKey = CONFIRMATION_RESULT_KEY_LOAD_CSV_FILE,
        textRes = R.string.confirmationLoadCsvFileData,
        titleRes = R.string.csvLoadConfirmation
    )
}