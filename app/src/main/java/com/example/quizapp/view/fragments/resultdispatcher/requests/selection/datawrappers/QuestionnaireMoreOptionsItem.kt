package com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionTypeItemMarker
import kotlinx.parcelize.Parcelize

@Parcelize
enum class QuestionnaireMoreOptionsItem(
    @StringRes override val textRes: Int,
    @DrawableRes override val iconRes: Int
) : SelectionTypeItemMarker<QuestionnaireMoreOptionsItem> {

    EDIT(
        iconRes = R.drawable.ic_edit,
        textRes = R.string.edit
    ),
    SHARE(
        iconRes = R.drawable.ic_share,
        textRes = R.string.share
    ),
    COPY(
        iconRes = R.drawable.ic_copy,
        textRes = R.string.copy
    ),
    PUBLISH(
        iconRes = R.drawable.ic_publish,
        textRes = R.string.setQuestionnaireToPublic
    ),
    UN_PUBLISH(
        iconRes = R.drawable.ic_un_publish,
        textRes = R.string.setQuestionnaireToPrivate
    ),
    DELETE_ANSWERS(
        iconRes = R.drawable.ic_delete_answers,
        textRes = R.string.deleteGivenAnswers
    ),
    DELETE(
        iconRes = R.drawable.ic_delete,
        textRes = R.string.delete
    );

    companion object {
        fun getMenuList(questionnaire: Questionnaire, user: User) = when (questionnaire.authorInfo.userId) {
            user.id -> {
                when {
                    user.role == Role.USER -> listOf(
                        EDIT,
                        SHARE,
                        COPY,
                        DELETE_ANSWERS,
                        DELETE
                    )
                    questionnaire.visibility == QuestionnaireVisibility.PUBLIC -> listOf(
                        EDIT,
                        SHARE,
                        COPY,
                        PUBLISH,
                        DELETE_ANSWERS,
                        DELETE
                    )
                    else -> listOf(
                        EDIT,
                        SHARE,
                        COPY,
                        UN_PUBLISH,
                        DELETE_ANSWERS,
                        DELETE
                    )
                }
            }
            else -> listOf(
                COPY,
                DELETE_ANSWERS,
                DELETE
            )
        }
    }
}