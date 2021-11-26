package com.example.quizapp.view.fragments.dialogs.selection

import android.os.Parcelable
import androidx.annotation.StringRes
import com.example.quizapp.R
import com.example.quizapp.model.databases.Degree
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.QuizAppLanguage
import com.example.quizapp.model.datastore.QuizAppTheme
import com.example.quizapp.model.menus.*
import kotlinx.parcelize.Parcelize

sealed class SelectionType(
    @StringRes val titleRes: Int,
    val resultKey: String,
    val recyclerViewList: List<SelectionTypeItemMarker<*>>,
    vararg val additionalTitleResources: String
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
        const val SELECTION_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY = "questionnaireMoreOptionsSelectionResultKey"
        const val SELECTION_SORTING_TYPE_RESULT_KEY = "sortBySelectionResultKey"

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
            SortBy::class -> SELECTION_SORTING_TYPE_RESULT_KEY
            else -> throw IllegalArgumentException("Cant get result key with '${ResultType::class.simpleName}'")
        }
    }

    @Parcelize
    data class RoleSelection(val currentRole: Role? = null) : SelectionType(
        titleRes = R.string.roleSelection,
        resultKey = SELECTION_ROLE_RESULT_KEY,
        recyclerViewList = Role.values().toList()
    )

    @Parcelize
    data class DegreeSelection(val currentDegree: Degree? = null) : SelectionType(
        titleRes = R.string.degreeSelection,
        resultKey = SELECTION_DEGREE_RESULT_KEY,
        recyclerViewList = Degree.values().toList()
    )

    @Parcelize
    data class ShuffleTypeSelection(val currentShuffleType: QuestionnaireShuffleType) : SelectionType(
        titleRes = R.string.shuffleTypeSelection,
        resultKey = SELECTION_SHUFFLE_TYPE_RESULT_KEY,
        recyclerViewList = QuestionnaireShuffleType.values().toList()
    )

    @Parcelize
    data class LanguageSelection(val currentLanguage: QuizAppLanguage) : SelectionType(
        titleRes = R.string.languageSelection,
        resultKey = SELECTION_LANGUAGE_RESULT_KEY,
        recyclerViewList = QuizAppLanguage.values().toList()
    )

    @Parcelize
    data class ThemeSelection(val currentTheme: QuizAppTheme) : SelectionType(
        titleRes = R.string.themeSelection,
        resultKey = SELECTION_THEME_RESULT_KEY,
        recyclerViewList = QuizAppTheme.values().toList()
    )

    @Parcelize
    data class UserMoreOptionsSelection(val user: User) : SelectionType(
        titleRes = R.string._ph,
        resultKey = SELECTION_USER_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = UserMoreOptionsItem.values().toList(),
        additionalTitleResources = arrayOf(user.userName)
    )

    @Parcelize
    data class CourseOfStudiesMoreOptionsSelection(val courseOfStudies: CourseOfStudies) : SelectionType(
        titleRes = R.string._ph,
        resultKey = SELECTION_COS_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = CosMoreOptionsItem.values().toList(),
        additionalTitleResources = arrayOf(courseOfStudies.name)
    )

    @Parcelize
    data class FacultyMoreOptionsSelection(val faculty: Faculty) : SelectionType(
        titleRes = R.string._ph,
        resultKey = SELECTION_FACULTY_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = FacultyMoreOptionsItem.values().toList(),
        additionalTitleResources = arrayOf(faculty.name)
    )

    @Parcelize
    data class SortingTypeSelection(val currentValue: SortBy) : SelectionType(
        titleRes = R.string.sortingTypeSelection,
        resultKey = SELECTION_SORTING_TYPE_RESULT_KEY,
        recyclerViewList = SortBy.values().toList()
    )



    //TODO -> Noch anschauen bisschen
    @Parcelize
    data class QuestionnaireMoreOptionsSelection(val questionnaire: Questionnaire, val user: User) : SelectionType(
        titleRes = R.string._ph,
        resultKey = SELECTION_QUESTIONNAIRE_MORE_OPTIONS_RESULT_KEY,
        recyclerViewList = QuestionnaireMoreOptionsItem.getMenuList(questionnaire, user),
        additionalTitleResources = arrayOf(questionnaire.title)
    )



    fun isItemSelected(item: SelectionTypeItemMarker<*>) = run {
        when (this) {
            is RoleSelection -> currentRole == item
            is ThemeSelection -> currentTheme == item
            is DegreeSelection -> currentDegree == item
            is ShuffleTypeSelection -> currentShuffleType == item
            is LanguageSelection -> currentLanguage == item
            is SortingTypeSelection -> currentValue == item
            else -> false
        }
    }
}