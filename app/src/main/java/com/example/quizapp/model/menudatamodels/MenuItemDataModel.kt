package com.example.quizapp.model.menudatamodels

import com.example.quizapp.R
import com.example.quizapp.model.mongodb.documents.user.Role

object MenuItemDataModel {

    const val EDIT_QUESTIONNAIRE_ITEM_ID = 0
    const val SHARE_QUESTIONNAIRE_ITEM_ID = 1
    const val UPLOAD_QUESTIONNAIRE_ITEM_ID = 3
    const val PUBLISH_QUESTIONNAIRE_ITEM_ID = 4
    const val DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID = 5
    const val DELETE_CREATED_QUESTIONNAIRE_ITEM_ID = 6
    const val UPLOAD_GIVEN_ANSWERS_ITEM_ID = 7
    const val DELETE_CACHED_QUESTIONNAIRE_ITEM_ID = 8
    const val COPY_QUESTIONNAIRE_ITEM_ID = 9

    fun getQuestionnaireMoreOptionsMenu(isOwnerOfQuestionnaire: Boolean, userRole: Role) : List<MenuItem> {
        return if(isOwnerOfQuestionnaire) {
            createdQuestionnaireMoreOptionsMenu.apply {
                if(userRole == Role.USER){ removeAt(createdQuestionnaireMoreOptionsMenu.indexOfFirst { it.id == PUBLISH_QUESTIONNAIRE_ITEM_ID }) }
            }
        } else {
            cachedQuestionnaireMoreOptionsMenu
        }
    }

    private val createdQuestionnaireMoreOptionsMenu get() = mutableListOf(
        MenuItem(
            id = EDIT_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_edit,
            titleRes = R.string.edit
        ),
        MenuItem(
            id = SHARE_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_share,
            titleRes = R.string.shareQuestionnaireWithUser
        ),
        MenuItem(
            id = COPY_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_copy,
            titleRes = R.string.copyQuestionnaire
        ),
        MenuItem(
            id = UPLOAD_GIVEN_ANSWERS_ITEM_ID,
            iconRes = R.drawable.ic_cloud_upload,
            titleRes = R.string.uploadAnswers
        ),
        MenuItem(
            id = UPLOAD_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_cloud_upload,
            titleRes = R.string.uploadQuestionnaire
        ),
        MenuItem(
            id = PUBLISH_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_publish,
            titleRes = R.string.publishQuestionnaire
        ),
        MenuItem(
            id = DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_delete_answers,
            titleRes = R.string.deleteGivenAnswers
        ),
        MenuItem(
            id = DELETE_CREATED_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_delete,
            titleRes = R.string.deleteQuestionnaire
        )
    )


    private val cachedQuestionnaireMoreOptionsMenu get() = mutableListOf(
        MenuItem(
            id = UPLOAD_GIVEN_ANSWERS_ITEM_ID,
            iconRes = R.drawable.ic_cloud_upload,
            titleRes = R.string.uploadAnswers
        ),
        MenuItem(
            id = COPY_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_copy,
            titleRes = R.string.copyQuestionnaire
        ),
        MenuItem(
            id = DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_delete_answers,
            titleRes = R.string.deleteGivenAnswers
        ),
        MenuItem(
            id = DELETE_CREATED_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_delete,
            titleRes = R.string.deleteQuestionnaire
        )
    )
}