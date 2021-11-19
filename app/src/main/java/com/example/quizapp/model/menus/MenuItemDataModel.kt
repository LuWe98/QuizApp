package com.example.quizapp.model.menus

import androidx.appcompat.app.AppCompatDelegate
import com.example.quizapp.R
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.QuizAppLanguage
import com.example.quizapp.view.fragments.dialogs.moreoptions.BsdfQuestionnaireMoreOptionsArgs

object MenuItemDataModel {

    const val EDIT_QUESTIONNAIRE_ITEM_ID = 0
    const val SHARE_QUESTIONNAIRE_ITEM_ID = 2
    const val PUBLISH_QUESTIONNAIRE_ITEM_ID = 3
    const val DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID = 4
    const val DELETE_CREATED_QUESTIONNAIRE_ITEM_ID = 5
    const val DELETE_CACHED_QUESTIONNAIRE_ITEM_ID = 6
    const val COPY_QUESTIONNAIRE_ITEM_ID = 7
    const val DELETE_USER_ITEM_ID = 8
    const val CHANGE_USER_ROLE_ITEM_ID = 9

    fun getQuestionnaireMoreOptionsMenu(args: BsdfQuestionnaireMoreOptionsArgs, user: User) : List<MenuIntIdItem> {
        return if(args.questionnaire.authorInfo.userId == user.id) {
            createdQuestionnaireMoreOptionsMenu.apply {
                if(user.role == Role.USER){
                    removeAt(createdQuestionnaireMoreOptionsMenu.indexOfFirst {
                        it.id == PUBLISH_QUESTIONNAIRE_ITEM_ID
                    })
                } else if(args.questionnaire.visibility == QuestionnaireVisibility.PUBLIC) {
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
        MenuIntIdItem(
            id = EDIT_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_edit,
            titleRes = R.string.edit
        ),
        MenuIntIdItem(
            id = SHARE_QUESTIONNAIRE_ITEM_ID,
            iconRes = R.drawable.ic_share,
            titleRes = R.string.shareQuestionnaireWithUser
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
            titleRes = R.string.deleteQuestionnaire
        )
    )


    private val cachedQuestionnaireMoreOptionsMenu get() = listOf(
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
            titleRes = R.string.deleteQuestionnaire
        )
    )


    val userMoreOptionsMenu get() = listOf(
        MenuIntIdItem(
          id = CHANGE_USER_ROLE_ITEM_ID,
          iconRes = R.drawable.ic_role_badge,
          titleRes = R.string.changeUserRole
        ),
        MenuIntIdItem(
            id = DELETE_USER_ITEM_ID,
            iconRes = R.drawable.ic_delete,
            titleRes = R.string.deleteUser
        )
    )
    /*
MenuItem(
        id = BROWSER_USER_QUESTIONNAIRES_ITEM_ID,
        iconRes = R.drawable.ic_question,
        titleRes = R.string.browseUsersQuestionnaires
    ),
 */


    val themeOptionsMenu get() = listOf(
        MenuIntIdItem(
            id = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            iconRes = R.drawable.ic_settings,
            titleRes = R.string.systemDefault
        ),
        MenuIntIdItem(
            id = AppCompatDelegate.MODE_NIGHT_YES,
            iconRes = R.drawable.ic_dark_mode_alt,
            titleRes = R.string.dark
        ),
        MenuIntIdItem(
            id = AppCompatDelegate.MODE_NIGHT_NO,
            iconRes = R.drawable.ic_light_mode,
            titleRes = R.string.light
        ),
    )



    val languageOptionsMenu get() = listOf(
        MenuStringIdItem(
            id = QuizAppLanguage.ENGLISH.name,
            iconRes = R.drawable.ic_language,
            titleRes = R.string.english
        ),
        MenuStringIdItem(
            id = QuizAppLanguage.GERMAN.name,
            iconRes = R.drawable.ic_language,
            titleRes = R.string.german
        ),
    )

    val shuffleQuestionsOptionsMenu get() = listOf(
        MenuStringIdItem(
            id = QuestionnaireShuffleType.NONE.name,
            iconRes = R.drawable.ic_cross,
            titleRes = R.string.shuffleTypeNone
        ),
        MenuStringIdItem(
            id = QuestionnaireShuffleType.SHUFFLED_QUESTIONS.name,
            iconRes = R.drawable.ic_shuffle_new,
            titleRes = R.string.shuffleTypeQuestions
        ),
        MenuStringIdItem(
            id = QuestionnaireShuffleType.SHUFFLED_ANSWERS.name,
            iconRes = R.drawable.ic_shuffle_new,
            titleRes = R.string.shuffleTypeAnswers
        ),
        MenuStringIdItem(
            id = QuestionnaireShuffleType.SHUFFLED_QUESTIONS_AND_ANSWERS.name,
            iconRes = R.drawable.ic_shuffle_new,
            titleRes = R.string.shuffleTypeQuestionsAndAnswers
        ),
    )
}