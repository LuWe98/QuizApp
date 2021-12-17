package com.example.quizapp.view.fragments.resultdispatcher.requests

import android.os.Parcelable
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.ConfirmationResult.*
import kotlinx.parcelize.Parcelize

sealed class ConfirmationRequestType(
    @StringRes val titleRes: Int,
    @StringRes val textRes: Int,
    @StringRes val positiveButtonRes: Int = R.string.confirm,
    @StringRes val negativeButtonRes: Int = R.string.cancel
): Parcelable {

    @Parcelize
    data class DeleteUserConfirmationRequest(val user: User): ConfirmationRequestType(
        titleRes = R.string.deletionConfirmationTile,
        textRes = R.string.warningUserDeletion
    )

    @Parcelize
    data class DeleteFacultyConfirmationRequest(val faculty: Faculty): ConfirmationRequestType(
        titleRes = R.string.deletionConfirmationTile,
        textRes = R.string.warningFacultyDeletion
    )

    @Parcelize
    data class DeleteCourseOfStudiesConfirmationRequest(val courseOfStudies: CourseOfStudies): ConfirmationRequestType(
        titleRes = R.string.deletionConfirmationTile,
        textRes = R.string.warningCourseOfStudiesDeletion
    )

    @Parcelize
    object LogoutConfirmationRequest: ConfirmationRequestType(
        titleRes = R.string.logoutWarningTitle,
        textRes = R.string.logoutWarning,
        positiveButtonRes = R.string.logout
    )

    @Parcelize
    object LoadCsvFileConfirmationRequest: ConfirmationRequestType(
        textRes = R.string.confirmationLoadCsvFileData,
        titleRes = R.string.csvLoadConfirmation
    )

    fun generateFragmentResponse(confirmed: Boolean) = when(this) {
        is DeleteCourseOfStudiesConfirmationRequest -> DeleteCourseOfStudiesResult(confirmed, courseOfStudies)
        is DeleteFacultyConfirmationRequest -> DeleteFacultyConfirmationResult(confirmed, faculty)
        is DeleteUserConfirmationRequest -> DeleteUserConfirmationResult(confirmed, user)
        LoadCsvFileConfirmationRequest -> LoadCsvFileConfirmationResult(confirmed)
        LogoutConfirmationRequest -> LogoutConfirmationResult(confirmed)
    }
}