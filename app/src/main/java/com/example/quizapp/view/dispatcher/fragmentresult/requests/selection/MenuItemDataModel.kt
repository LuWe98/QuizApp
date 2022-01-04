package com.example.quizapp.view.dispatcher.fragmentresult.requests.selection

import com.example.quizapp.R
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.view.fragments.dialogs.localquestionnairemoreoptions.BsdfQuestionnaireMoreOptionsArgs

object MenuItemDataModel {

    const val EDIT_QUESTIONNAIRE_ITEM_ID = 0
    const val SHARE_QUESTIONNAIRE_ITEM_ID = 2
    const val PUBLISH_QUESTIONNAIRE_ITEM_ID = 3
    const val DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID = 4
    const val DELETE_CREATED_QUESTIONNAIRE_ITEM_ID = 5
    const val DELETE_CACHED_QUESTIONNAIRE_ITEM_ID = 6
    const val COPY_QUESTIONNAIRE_ITEM_ID = 7


    fun getQuestionnaireMoreOptionsMenu(args: BsdfQuestionnaireMoreOptionsArgs, user: User): List<MenuIntIdItem> {
        return if (args.questionnaire.authorInfo.userId == user.id) {
            questionnaireMoreOptionsMenu.apply {
                if (user.role == Role.USER) {
                    removeAt(questionnaireMoreOptionsMenu.indexOfFirst {
                        it.id == PUBLISH_QUESTIONNAIRE_ITEM_ID
                    })
                } else if (args.questionnaire.visibility == QuestionnaireVisibility.PUBLIC) {
                    val index = questionnaireMoreOptionsMenu.indexOfFirst { it.id == PUBLISH_QUESTIONNAIRE_ITEM_ID }
                    val item = removeAt(index)
                    add(index, item.copy(iconRes = R.drawable.ic_un_publish, titleRes = R.string.setQuestionnaireToPrivate))
                }
            }
        } else {
            cachedQuestionnaireMoreOptionsMenu
        }
    }


    private val questionnaireMoreOptionsMenu
        get() = mutableListOf(
            MenuIntIdItem(
                id = EDIT_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_edit,
                titleRes = R.string.edit
            ),
            MenuIntIdItem(
                id = SHARE_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_share,
                titleRes = R.string.share
            ),
            MenuIntIdItem(
                id = COPY_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_copy,
                titleRes = R.string.copy
            ),
            MenuIntIdItem(
                id = PUBLISH_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_publish,
                titleRes = R.string.setQuestionnaireToPublic
            ),
            MenuIntIdItem(
                id = DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_delete_answers,
                titleRes = R.string.deleteGivenAnswers
            ),
            MenuIntIdItem(
                id = DELETE_CREATED_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_delete,
                titleRes = R.string.delete
            )
        )


    private val cachedQuestionnaireMoreOptionsMenu
        get() = listOf(
            MenuIntIdItem(
                id = COPY_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_copy,
                titleRes = R.string.copy
            ),
            MenuIntIdItem(
                id = DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_delete_answers,
                titleRes = R.string.deleteGivenAnswers
            ),
            MenuIntIdItem(
                id = DELETE_CACHED_QUESTIONNAIRE_ITEM_ID,
                iconRes = R.drawable.ic_delete,
                titleRes = R.string.delete
            )
        )

    val browseQuestionnaireMoreOptionsMenu get() = listOf(
        MenuIntIdItem(
            id = 1,
            iconRes = R.drawable.ic_download,
            titleRes = R.string.download
        )
    )
}