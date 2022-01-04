package com.example.quizapp.view.dispatcher.fragmentresult.requests

import android.os.Parcelable
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.ConfirmationResult.LoadCsvFileConfirmationResult
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.ConfirmationResult.LogoutConfirmationResult
import kotlinx.parcelize.Parcelize

sealed class ConfirmationRequestType(
    @StringRes val titleRes: Int,
    @StringRes val textRes: Int,
    @StringRes val positiveButtonRes: Int = R.string.confirm,
    @StringRes val negativeButtonRes: Int = R.string.cancel,
    val responseProvider: (Boolean) -> ConfirmationResult
): Parcelable {

    @Parcelize
    data class DeleteUserConfirmationRequest(val user: User): ConfirmationRequestType(
        titleRes = R.string.deletionConfirmationTile,
        textRes = R.string.warningUserDeletion,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = { ConfirmationResult.DeleteUserConfirmationResult(it, user) }
    )

    @Parcelize
    data class DeleteFacultyConfirmationRequest(val faculty: Faculty): ConfirmationRequestType(
        titleRes = R.string.deletionConfirmationTile,
        textRes = R.string.warningFacultyDeletion,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = { ConfirmationResult.DeleteFacultyConfirmationResult(it, faculty) }
    )

    @Parcelize
    data class DeleteCourseOfStudiesConfirmationRequest(val courseOfStudies: CourseOfStudies): ConfirmationRequestType(
        titleRes = R.string.deletionConfirmationTile,
        textRes = R.string.warningCourseOfStudiesDeletion,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = { ConfirmationResult.DeleteCourseOfStudiesResult(it, courseOfStudies) }
    )

    @Parcelize
    object LogoutConfirmationRequest: ConfirmationRequestType(
        titleRes = R.string.logoutWarningTitle,
        textRes = R.string.logoutWarning,
        positiveButtonRes = R.string.logout,
        negativeButtonRes = R.string.cancel,
        responseProvider = ::LogoutConfirmationResult
    )

    @Parcelize
    object LoadCsvFileConfirmationRequest: ConfirmationRequestType(
        textRes = R.string.confirmationLoadCsvFileData,
        titleRes = R.string.csvLoadConfirmation,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = ::LoadCsvFileConfirmationResult
    )
}