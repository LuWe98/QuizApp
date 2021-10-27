package com.example.quizapp.model.menudatamodels

import com.example.quizapp.R
import com.example.quizapp.model.mongodb.documents.questionnaire.QuestionnaireVisibility
import com.example.quizapp.model.mongodb.documents.user.Role
import com.example.quizapp.model.mongodb.documents.user.User
import com.example.quizapp.view.fragments.dialogs.BsdfQuestionnaireMoreOptionsArgs

object MenuItemDataModel {

    const val EDIT_QUESTIONNAIRE_ITEM_ID = 0
    const val SHARE_QUESTIONNAIRE_ITEM_ID = 1
    const val UPLOAD_QUESTIONNAIRE_ITEM_ID = 3
    const val PUBLISH_QUESTIONNAIRE_ITEM_ID = 4
    const val DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID = 5
    const val DELETE_CREATED_QUESTIONNAIRE_ITEM_ID = 6
    const val DELETE_CACHED_QUESTIONNAIRE_ITEM_ID = 8
    const val COPY_QUESTIONNAIRE_ITEM_ID = 9
    const val DELETE_USER_ITEM_ID = 10
    const val CHANGE_USER_ROLE_ITEM_ID = 11
    const val BROWSER_USER_QUESTIONNAIRES_ITEM_ID = 12

    fun getQuestionnaireMoreOptionsMenu(args: BsdfQuestionnaireMoreOptionsArgs, user: User) : List<MenuItem> {
        return if(args.questionnaire.authorInfo.userId == user.id) {
            createdQuestionnaireMoreOptionsMenu.apply {
                if(user.role == Role.USER){
                    removeAt(createdQuestionnaireMoreOptionsMenu.indexOfFirst {
                        it.id == PUBLISH_QUESTIONNAIRE_ITEM_ID
                    })
                } else if(args.questionnaire.questionnaireVisibility == QuestionnaireVisibility.PUBLIC) {
                    val index = createdQuestionnaireMoreOptionsMenu.indexOfFirst { it.id == PUBLISH_QUESTIONNAIRE_ITEM_ID }
                    val item = removeAt(index)
                    add(index, item.copy(iconRes = R.drawable.ic_un_publish, titleRes = R.string.setQuestionnaireToPrivate))
                }
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
            titleRes = R.string.copy
        ),
        MenuItem(
            id = UPLOAD_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_cloud_upload,
            titleRes = R.string.uploadQuestionnaire
        ),
        MenuItem(
            id = PUBLISH_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_publish,
            titleRes = R.string.setQuestionnaireToPublic
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
            id = COPY_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_copy,
            titleRes = R.string.copy
        ),
        MenuItem(
            id = DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_delete_answers,
            titleRes = R.string.deleteGivenAnswers
        ),
        MenuItem(
            id = DELETE_CACHED_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_delete,
            titleRes = R.string.deleteQuestionnaire
        )
    )


    /*
    MenuItem(
            id = BROWSER_USER_QUESTIONNAIRES_ITEM_ID,
            iconRes = R.drawable.ic_question,
            titleRes = R.string.browseUsersQuestionnaires
        ),
     */

    val userMoreOptionsMenu get() = mutableListOf(
        MenuItem(
          id = CHANGE_USER_ROLE_ITEM_ID,
          iconRes = R.drawable.ic_role_badge,
          titleRes = R.string.changeUserRole
        ),
        MenuItem(
            id = DELETE_USER_ITEM_ID,
            iconRes = R.drawable.ic_delete,
            titleRes = R.string.deleteUser
        )
    )
}