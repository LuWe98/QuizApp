package com.example.quizapp.view.fragments.resultdispatcher.requests

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.UpdateStringValueResult
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.UpdateStringValueResult.*
import kotlinx.parcelize.Parcelize


sealed class UpdateStringRequestType constructor(
    @DrawableRes val iconRes: Int,
    @StringRes val hintRes: Int,
    @StringRes val titleRes: Int,
    val resultProvider: (String) -> (UpdateStringValueResult)
): Parcelable {

    abstract val currentStringValue: String

    @Parcelize
    data class UpdateQuestionnaireTitleRequest(override val currentStringValue: String) : UpdateStringRequestType(
        iconRes = R.drawable.ic_title,
        hintRes = R.string.title,
        titleRes = R.string.updateQuestionnaireTitle,
        resultProvider = ::QuestionnaireTitleUpdateResult
    )

    @Parcelize
    data class UpdateQuestionnaireSubjectRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_subject,
        hintRes = R.string.subject,
        titleRes = R.string.updateQuestionnaireSubject,
        resultProvider = ::QuestionnaireSubjectUpdateResult
    )

    @Parcelize
    data class UpdateAddEditQuestionAnswerRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_title,
        hintRes = R.string.answerText,
        titleRes = R.string.updateAnswerText,
        resultProvider = ::AddEditQuestionAnswerTextUpdateResult
    )

    @Parcelize
    data class UpdateFacultyAbbreviationRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_title,
        hintRes = R.string.abbreviation,
        titleRes = R.string.updateFacultyAbbreviation,
        resultProvider = ::AddEditFacultyAbbreviationUpdateResult
    )

    @Parcelize
    data class UpdateFacultyNameRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_faculty,
        hintRes = R.string.name,
        titleRes = R.string.updateFacultyName,
        resultProvider = ::AddEditFacultyNameUpdateResult
    )

    @Parcelize
    data class UpdateCourseOfStudiesAbbreviationRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_title,
        hintRes = R.string.abbreviation,
        titleRes = R.string.updateCourseOfStudiesAbbreviation,
        resultProvider = ::AddEditCourseOfStudiesAbbreviationUpdateResult
    )

    @Parcelize
    data class UpdateCourseOfStudiesNameRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_course_of_studies,
        hintRes = R.string.name,
        titleRes = R.string.updateCourseOfStudiesName,
        resultProvider = ::AddEditCourseOfStudiesNameUpdateResult
    )

    @Parcelize
    data class UpdateUserNameRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_person,
        hintRes = R.string.userName,
        titleRes = R.string.updateUserName,
        resultProvider = ::AddEditUserNameUpdateResult
    )

    @Parcelize
    data class UpdateUserPasswordRequest(override val currentStringValue: String): UpdateStringRequestType(
        iconRes = R.drawable.ic_password,
        hintRes = R.string.password,
        titleRes = R.string.updatePassword,
        resultProvider = ::AddEditUserPasswordUpdateResult
    )
}