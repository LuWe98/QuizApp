package com.example.quizapp.view.fragments.dialogs.selection

import android.content.Context
import android.os.Parcelable
import com.example.quizapp.R
import com.example.quizapp.model.databases.Degree
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.datastore.datawrappers.*
import com.example.quizapp.model.menus.*
import com.example.quizapp.model.menus.datawrappers.*
import kotlinx.parcelize.Parcelize

sealed class SelectionType(
    val resultKey: String,
    val recyclerViewList: List<SelectionTypeItemMarker<*>>
) : Parcelable {

    companion object {
        const val INITIAL_VALUE_SUFFIX = "-initial"

        const val SELECTION_ROLE_RESULT_KEY = "roleSelectionResultKey"
        const val SELECTION_DEGREE_RESULT_KEY = "degreeSelectionResultKey"
        const val SELECTION_SHUFFLE_TYPE_RESULT_KEY = "shuffleTypeSelectionResultKey"
        const val SELECTION_LANGUAGE_RESULT_KEY = "languageSelectionResultKey"
        const val SELECTION_THEME_RESULT_KEY = "themeSelectionResultKey"
        const val SELECTION_USER_MORE_OPTIONS_RESULT_KEY = "userMoreOptionsSelectionResultKey"
        const val SELECTION_COS_MORE_OPTIONS_RESULT_KEY = "cosMoreOptionsSelectionResultKey"
        const val SELECTION_FACULTY_MORE_OPTIONS_RESULT_KEY = "facultyMoreOptionsSelectionResultKey"
        const val SELECTION_BROWSABLE_ORDER_BY_RESULT_KEY = "orderByBrowseQuestionnairesSelectionResultKey"
        const val SELECTION_LOCAL_QUESTIONNAIRE_ORDER_BY_RESULT_KEY = "orderByLocalQuestionnairesSelectionResultKey"
        const val SELECTION_MANAGE_USERS_ORDER_BY_RESULT_KEY = "orderByManageUsersSelectionResultKey"

        const val SELECTION_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY = "questionnaireMoreOptionsSelectionResultKey"
        const val SELECTION_BROWSE_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY = "browseQuestionnaireMoreOptionsSelectionResultKey"


        inline fun <reified ResultType : SelectionTypeItemMarker<ResultType>> getResultKeyWithResultClass() = when (ResultType::class) {
            Role::class -> SELECTION_ROLE_RESULT_KEY
            Degree::class -> SELECTION_DEGREE_RESULT_KEY
            QuestionnaireShuffleType::class -> SELECTION_SHUFFLE_TYPE_RESULT_KEY
            QuizAppLanguage::class -> SELECTION_LANGUAGE_RESULT_KEY
            QuizAppTheme::class -> SELECTION_THEME_RESULT_KEY
            UserMoreOptionsItem::class -> SELECTION_USER_MORE_OPTIONS_RESULT_KEY
            CosMoreOptionsItem::class -> SELECTION_COS_MORE_OPTIONS_RESULT_KEY
            FacultyMoreOptionsItem::class -> SELECTION_FACULTY_MORE_OPTIONS_RESULT_KEY
            QuestionnaireMoreOptionsItem::class -> SELECTION_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY
            BrowsableOrderBy::class -> SELECTION_BROWSABLE_ORDER_BY_RESULT_KEY
            LocalQuestionnaireOrderBy::class -> SELECTION_LOCAL_QUESTIONNAIRE_ORDER_BY_RESULT_KEY
            ManageUsersOrderBy::class -> SELECTION_MANAGE_USERS_ORDER_BY_RESULT_KEY
            BrowseQuestionnaireMoreOptionsItem::class -> SELECTION_BROWSE_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY
            else -> throw IllegalArgumentException("Cant get result key with '${ResultType::class.simpleName}'")
        }
    }

    @Parcelize
    data class RoleSelection(val currentRole: Role? = null) : SelectionType(
        resultKey = SELECTION_ROLE_RESULT_KEY,
        recyclerViewList = Role.values().toList()
    )

    @Parcelize
    data class DegreeSelection(val currentDegree: Degree? = null) : SelectionType(
        resultKey = SELECTION_DEGREE_RESULT_KEY,
        recyclerViewList = Degree.values().toList()
    )

    @Parcelize
    data class ShuffleTypeSelection(val currentShuffleType: QuestionnaireShuffleType) : SelectionType(
        resultKey = SELECTION_SHUFFLE_TYPE_RESULT_KEY,
        recyclerViewList = QuestionnaireShuffleType.values().toList()
    )

    @Parcelize
    data class LanguageSelection(val currentLanguage: QuizAppLanguage) : SelectionType(
        resultKey = SELECTION_LANGUAGE_RESULT_KEY,
        recyclerViewList = QuizAppLanguage.values().toList()
    )

    @Parcelize
    data class ThemeSelection(val currentTheme: QuizAppTheme) : SelectionType(
        resultKey = SELECTION_THEME_RESULT_KEY,
        recyclerViewList = QuizAppTheme.values().toList()
    )

    @Parcelize
    data class UserMoreOptionsSelection(val user: User) : SelectionType(
        resultKey = SELECTION_USER_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = UserMoreOptionsItem.values().toList(),
    )

    @Parcelize
    data class CourseOfStudiesMoreOptionsSelection(val courseOfStudies: CourseOfStudies) : SelectionType(
        resultKey = SELECTION_COS_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = CosMoreOptionsItem.values().toList(),
    )

    @Parcelize
    data class FacultyMoreOptionsSelection(val faculty: Faculty) : SelectionType(
        resultKey = SELECTION_FACULTY_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = FacultyMoreOptionsItem.values().toList(),
    )

    @Parcelize
    data class BrowsableOrderBySelection(val currentValue: BrowsableOrderBy) : SelectionType(
        resultKey = SELECTION_BROWSABLE_ORDER_BY_RESULT_KEY,
        recyclerViewList = BrowsableOrderBy.values().toList()
    )

    @Parcelize
    data class LocalQuestionnaireOrderBySelection(val currentValue: LocalQuestionnaireOrderBy) : SelectionType(
        resultKey = SELECTION_LOCAL_QUESTIONNAIRE_ORDER_BY_RESULT_KEY,
        recyclerViewList = LocalQuestionnaireOrderBy.values().toList()
    )

    @Parcelize
    data class ManageUsersOrderBySelection(val currentValue: ManageUsersOrderBy) : SelectionType(
        resultKey = SELECTION_MANAGE_USERS_ORDER_BY_RESULT_KEY,
        recyclerViewList = ManageUsersOrderBy.values().toList()
    )


    @Parcelize
    data class QuestionnaireMoreOptionsSelection(val questionnaire: Questionnaire, val user: User) : SelectionType(
        resultKey = SELECTION_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = QuestionnaireMoreOptionsItem.getMenuList(questionnaire, user),
    )


    @Parcelize
    data class BrowseQuestionnaireMoreOptionsSelection(val browsableQuestionnaire: BrowsableQuestionnaire): SelectionType(
        resultKey = SELECTION_BROWSE_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = BrowseQuestionnaireMoreOptionsItem.values().toList()
    )


    fun isItemSelected(item: SelectionTypeItemMarker<*>) = when (this) {
        is RoleSelection -> currentRole == item
        is ThemeSelection -> currentTheme == item
        is DegreeSelection -> currentDegree == item
        is ShuffleTypeSelection -> currentShuffleType == item
        is LanguageSelection -> currentLanguage == item
        is BrowsableOrderBySelection -> currentValue == item
        is ManageUsersOrderBySelection -> currentValue == item
        is LocalQuestionnaireOrderBySelection -> currentValue == item
        else -> false
    }

    fun getTitle(context: Context) = when (this) {
        is UserMoreOptionsSelection -> context.getString(R.string._ph, user.userName)
        is CourseOfStudiesMoreOptionsSelection -> context.getString(R.string._ph, courseOfStudies.name)
        is FacultyMoreOptionsSelection -> context.getString(R.string._ph, faculty.name)
        is QuestionnaireMoreOptionsSelection -> context.getString(R.string._ph, questionnaire.title)
        is BrowseQuestionnaireMoreOptionsSelection -> context.getString(R.string._ph, browsableQuestionnaire.title)
        is BrowsableOrderBySelection -> context.getString(R.string.orderByTypeSelection)
        is DegreeSelection -> context.getString(R.string.degreeSelection)
        is LanguageSelection -> context.getString(R.string.languageSelection)
        is LocalQuestionnaireOrderBySelection -> context.getString(R.string.orderByTypeSelection)
        is ManageUsersOrderBySelection -> context.getString(R.string.orderByTypeSelection)
        is RoleSelection -> context.getString(R.string.roleSelection)
        is ShuffleTypeSelection -> context.getString(R.string.shuffleTypeSelection)
        is ThemeSelection -> context.getString(R.string.themeSelection)
    }
}