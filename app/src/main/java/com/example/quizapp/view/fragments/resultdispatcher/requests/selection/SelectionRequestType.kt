package com.example.quizapp.view.fragments.resultdispatcher.requests.selection

import android.content.Context
import android.os.Parcelable
import com.example.quizapp.R
import com.example.quizapp.model.databases.Degree
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.datastore.datawrappers.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.BrowseQuestionnaireMoreOptionsItem
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.CosMoreOptionsItem
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.FacultyMoreOptionsItem
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.UserMoreOptionsItem
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.SelectionResult.*
import kotlinx.parcelize.Parcelize


sealed class SelectionRequestType<T : Enum<T>>(
    val recyclerViewList: List<SelectionTypeItemMarker<T>>,
    val titleProvider: (Context) -> String,
    val resultProvider: (SelectionTypeItemMarker<*>) -> (FragmentResultDispatcher.SelectionResult<T>),
    val isItemSelectedProvider: (SelectionTypeItemMarker<*>) -> Boolean
) : Parcelable {

    @Parcelize
    data class RoleSelection(val currentRole: Role? = null) : SelectionRequestType<Role>(
            recyclerViewList = Role.values().toList(),
            titleProvider = { it.getString(R.string.roleSelection) },
            resultProvider = { RoleSelectionResult(it as Role) },
            isItemSelectedProvider = { currentRole == it }
    )

    @Parcelize
    data class DegreeSelection(val currentDegree: Degree? = null) : SelectionRequestType<Degree>(
            recyclerViewList = Degree.values().toList(),
            titleProvider = { it.getString(R.string.degreeSelection) },
            resultProvider = { DegreeSelectionResult(it as Degree) },
            isItemSelectedProvider = { currentDegree == it }
    )

    @Parcelize
    data class ShuffleTypeSelection(val currentShuffleType: QuestionnaireShuffleType) : SelectionRequestType<QuestionnaireShuffleType>(
            recyclerViewList = QuestionnaireShuffleType.values().toList(),
            titleProvider = { it.getString(R.string.shuffleTypeSelection) },
            resultProvider = { ShuffleTypeSelectionResult(it as QuestionnaireShuffleType) },
            isItemSelectedProvider = { currentShuffleType == it }
    )

    @Parcelize
    data class LanguageSelection(val currentLanguage: QuizAppLanguage) : SelectionRequestType<QuizAppLanguage>(
            recyclerViewList = QuizAppLanguage.values().toList(),
            titleProvider = { it.getString(R.string.languageSelection) },
            resultProvider = { LanguageSelectionResult(it as QuizAppLanguage) },
            isItemSelectedProvider = { currentLanguage == it }
    )

    @Parcelize
    data class ThemeSelection(val currentTheme: QuizAppTheme) : SelectionRequestType<QuizAppTheme>(
            recyclerViewList = QuizAppTheme.values().toList(),
            titleProvider = { it.getString(R.string.themeSelection) },
            resultProvider = { ThemeSelectionResult(it as QuizAppTheme) },
            isItemSelectedProvider = { currentTheme == it }
    )

    @Parcelize
    data class UserMoreOptionsSelection(val user: User) : SelectionRequestType<UserMoreOptionsItem>(
            recyclerViewList = UserMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, user.userName) },
            resultProvider = { UserMoreOptionsSelectionResult(user, it as UserMoreOptionsItem) },
            isItemSelectedProvider = { false }
    )

    @Parcelize
    data class CourseOfStudiesMoreOptionsSelection(val courseOfStudies: CourseOfStudies) : SelectionRequestType<CosMoreOptionsItem>(
            recyclerViewList = CosMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, courseOfStudies.name) },
            resultProvider = { CourseOfStudiesMoreOptionsResult(courseOfStudies, it as CosMoreOptionsItem) },
            isItemSelectedProvider = { false }
    )

    @Parcelize
    data class FacultyMoreOptionsSelection(val faculty: Faculty) : SelectionRequestType<FacultyMoreOptionsItem>(
            recyclerViewList = FacultyMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, faculty.name) },
            resultProvider = { FacultyMoreOptionsSelectionResult(faculty, it as FacultyMoreOptionsItem) },
            isItemSelectedProvider = { false }
    )

    @Parcelize
    data class RemoteOrderBySelection(val currentValue: RemoteQuestionnaireOrderBy) : SelectionRequestType<RemoteQuestionnaireOrderBy>(
            recyclerViewList = RemoteQuestionnaireOrderBy.values().toList(),
            titleProvider = { it.getString(R.string.orderByTypeSelection) },
            resultProvider = { RemoteOrderBySelectionResult(it as RemoteQuestionnaireOrderBy) },
            isItemSelectedProvider = { currentValue == it }
    )

    @Parcelize
    data class LocalOrderBySelection(val currentValue: LocalQuestionnaireOrderBy) : SelectionRequestType<LocalQuestionnaireOrderBy>(
            recyclerViewList = LocalQuestionnaireOrderBy.values().toList(),
            titleProvider = { it.getString(R.string.orderByTypeSelection) },
            resultProvider = { LocalOrderBySelectionResult(it as LocalQuestionnaireOrderBy) },
            isItemSelectedProvider = { currentValue == it }
    )

    @Parcelize
    data class ManageUsersOrderBySelection(val currentValue: ManageUsersOrderBy) : SelectionRequestType<ManageUsersOrderBy>(
            recyclerViewList = ManageUsersOrderBy.values().toList(),
            titleProvider = { it.getString(R.string.orderByTypeSelection) },
            resultProvider = { UsersOrderBySelectionResult(it as ManageUsersOrderBy) },
            isItemSelectedProvider = { currentValue == it }
    )

    @Parcelize
    data class BrowseQuestionnaireMoreOptionsSelection(val browsableQuestionnaire: BrowsableQuestionnaire) : SelectionRequestType<BrowseQuestionnaireMoreOptionsItem>(
            recyclerViewList = BrowseQuestionnaireMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, browsableQuestionnaire.title) },
            resultProvider = { RemoteQuestionnaireMoreOptionsSelectionResult(browsableQuestionnaire, it as BrowseQuestionnaireMoreOptionsItem) },
            isItemSelectedProvider = { false }
    )


    //    @Parcelize
//    data class QuestionnaireMoreOptionsSelection(val questionnaire: Questionnaire, val user: User) : SelectionRequestType<QuestionnaireMoreOptionsItem>(
//        recyclerViewList = QuestionnaireMoreOptionsItem.getMenuList(questionnaire, user),
//    )
    //        is QuestionnaireMoreOptionsSelection -> context.getString(R.string._ph, questionnaire.title)
}