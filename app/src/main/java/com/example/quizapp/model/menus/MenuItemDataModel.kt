package com.example.quizapp.model.menus

import androidx.appcompat.app.AppCompatDelegate
import com.example.quizapp.R
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.view.fragments.dialogs.BsdfQuestionnaireMoreOptionsArgs
import com.example.quizapp.model.datastore.QuizAppLanguage

object MenuItemDataModel {

    const val EDIT_QUESTIONNAIRE_ITEM_ID = 0
    const val SHARE_QUESTIONNAIRE_ITEM_ID = 1
    const val PUBLISH_QUESTIONNAIRE_ITEM_ID = 3
    const val DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID = 4
    const val DELETE_CREATED_QUESTIONNAIRE_ITEM_ID = 5
    const val DELETE_CACHED_QUESTIONNAIRE_ITEM_ID = 6
    const val COPY_QUESTIONNAIRE_ITEM_ID = 7
    const val DELETE_USER_ITEM_ID = 8
    const val CHANGE_USER_ROLE_ITEM_ID = 9

    fun getQuestionnaireMoreOptionsMenu(args: BsdfQuestionnaireMoreOptionsArgs, user: User) : List<MenuItem> {
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


    private val cachedQuestionnaireMoreOptionsMenu get() = listOf(
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


    val userMoreOptionsMenu get() = listOf(
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
    /*
MenuItem(
        id = BROWSER_USER_QUESTIONNAIRES_ITEM_ID,
        iconRes = R.drawable.ic_question,
        titleRes = R.string.browseUsersQuestionnaires
    ),
 */


    val themeOptionsMenu get() = listOf(
        MenuItem(
            id = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            iconRes = R.drawable.ic_settings,
            titleRes = R.string.systemDefault
        ),
        MenuItem(
            id = AppCompatDelegate.MODE_NIGHT_YES,
            iconRes = R.drawable.ic_dark_mode_alt,
            titleRes = R.string.dark
        ),
        MenuItem(
            id = AppCompatDelegate.MODE_NIGHT_NO,
            iconRes = R.drawable.ic_light_mode,
            titleRes = R.string.light
        ),
    )



    val languageOptionsMenu get() = listOf(
        MenuItem(
            id = QuizAppLanguage.ENGLISH.ordinal,
            iconRes = R.drawable.ic_language,
            titleRes = R.string.english
        ),
        MenuItem(
            id = QuizAppLanguage.GERMAN.ordinal,
            iconRes = R.drawable.ic_language,
            titleRes = R.string.german
        ),
    )

    val shuffleQuestionsOptionsMenu get() = listOf(
        MenuItem(
            id = QuestionnaireShuffleType.NONE.ordinal,
            iconRes = R.drawable.ic_cross,
            titleRes = R.string.shuffleTypeNone
        ),
        MenuItem(
            id = QuestionnaireShuffleType.SHUFFLED_QUESTIONS.ordinal,
            iconRes = R.drawable.ic_shuffle_new,
            titleRes = R.string.shuffleTypeQuestions
        ),
        MenuItem(
            id = QuestionnaireShuffleType.SHUFFLED_ANSWERS.ordinal,
            iconRes = R.drawable.ic_shuffle_new,
            titleRes = R.string.shuffleTypeAnswers
        ),
        MenuItem(
            id = QuestionnaireShuffleType.SHUFFLED_QUESTIONS_AND_ANSWERS.ordinal,
            iconRes = R.drawable.ic_shuffle_new,
            titleRes = R.string.shuffleTypeQuestionsAndAnswers
        ),
    )
}