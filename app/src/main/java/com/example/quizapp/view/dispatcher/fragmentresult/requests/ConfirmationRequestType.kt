package com.example.quizapp.view.dispatcher.fragmentresult.requests

import android.os.Parcelable
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.ConfirmationResult.*
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
        titleRes = R.string.confirmDeletionTitle,
        textRes = R.string.confirmUserDeletionText,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = { ConfirmationResult.DeleteUserConfirmationResult(it, user) }
    )

    @Parcelize
    data class DeleteFacultyConfirmationRequest(val faculty: Faculty): ConfirmationRequestType(
        titleRes = R.string.confirmDeletionTitle,
        textRes = R.string.confirmFacultyDeletionText,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = { ConfirmationResult.DeleteFacultyConfirmationResult(it, faculty) }
    )

    @Parcelize
    data class DeleteCourseOfStudiesConfirmationRequest(val courseOfStudies: CourseOfStudies): ConfirmationRequestType(
        titleRes = R.string.confirmDeletionTitle,
        textRes = R.string.confirmCourseOfStudiesDeletionText,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = { ConfirmationResult.DeleteCourseOfStudiesResult(it, courseOfStudies) }
    )

    @Parcelize
    object LogoutConfirmationRequest: ConfirmationRequestType(
        titleRes = R.string.confirmLogoutTitle,
        textRes = R.string.confirmLogoutText,
        positiveButtonRes = R.string.logout,
        negativeButtonRes = R.string.cancel,
        responseProvider = ::LogoutConfirmationResult
    )

    @Parcelize
    object DeleteAccountConfirmationRequest: ConfirmationRequestType(
        titleRes = R.string.confirmAccountDeletionTitle,
        textRes = R.string.confirmAccountDeletionText,
        positiveButtonRes = R.string.delete,
        negativeButtonRes = R.string.cancel,
        responseProvider = ::DeleteAccountConfirmationResult
    )

    @Parcelize
    object LoadCsvFileConfirmationRequest: ConfirmationRequestType(
        textRes = R.string.confirmCsvLoadText,
        titleRes = R.string.confirmCsvLoadTitle,
        positiveButtonRes = R.string.confirm,
        negativeButtonRes = R.string.cancel,
        responseProvider = ::LoadCsvFileConfirmationResult
    )
}