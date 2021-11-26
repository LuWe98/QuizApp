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
        const val DELETE_USER_CONFIRMATION_RESULT_KEY = "deleteUserConfirmationResultKey"
        const val DELETE_FACULTY_CONFIRMATION_RESULT_KEY = "deleteFacultyConfirmationResultKey"
        const val DELETE_COURSE_OF_STUDIES_CONFIRMATION_RESULT_KEY = "deleteCourseOfStudiesResultKey"
        const val LOGOUT_CONFIRMATION_RESULT_KEY = "logoutConfirmationResultKey"

        inline fun <reified ResultType: ConfirmationType> getResultKeyWithResultType() = when(ResultType::class) {
            DeleteUserConfirmation::class -> DELETE_USER_CONFIRMATION_RESULT_KEY
            DeleteFacultyConfirmation::class -> DELETE_FACULTY_CONFIRMATION_RESULT_KEY
            DeleteCourseOfStudiesConfirmation::class -> DELETE_COURSE_OF_STUDIES_CONFIRMATION_RESULT_KEY
            LogoutConfirmation::class -> LOGOUT_CONFIRMATION_RESULT_KEY
            else -> throw IllegalStateException("Result key not configured for class '${ResultType::class.simpleName}")
        }
    }

    @Parcelize
    data class DeleteUserConfirmation(val user: User): ConfirmationType(
        resultKey = DELETE_USER_CONFIRMATION_RESULT_KEY,
        textRes = R.string.warningUserDeletetion
    )

    @Parcelize
    data class DeleteFacultyConfirmation(val faculty: Faculty): ConfirmationType(
        resultKey = DELETE_FACULTY_CONFIRMATION_RESULT_KEY,
        textRes = R.string.warningFacultyDeletetion
    )

    @Parcelize
    data class DeleteCourseOfStudiesConfirmation(val courseOfStudies: CourseOfStudies): ConfirmationType(
        resultKey = DELETE_COURSE_OF_STUDIES_CONFIRMATION_RESULT_KEY,
        textRes = R.string.warningCourseOfStudiesDeletetion
    )

    @Parcelize
    object LogoutConfirmation: ConfirmationType(
        resultKey = LOGOUT_CONFIRMATION_RESULT_KEY,
        titleRes = R.string.logoutWarningTitle,
        textRes = R.string.logoutWarning,
        positiveButtonRes = R.string.logout
    )
}