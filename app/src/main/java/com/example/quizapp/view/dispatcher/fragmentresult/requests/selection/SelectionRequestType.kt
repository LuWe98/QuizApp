package com.example.quizapp.view.dispatcher.fragmentresult.requests.selection

import android.content.Context
import android.os.Parcelable
import com.example.quizapp.R
import com.example.quizapp.model.databases.properties.Degree
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.datawrappers.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers.*
import kotlinx.parcelize.Parcelize


sealed class SelectionRequestType<T : Enum<T>>(
    val recyclerViewList: List<SelectionTypeItemMarker<T>>,
    val titleProvider: (Context) -> String?,
    val resultProvider: (SelectionTypeItemMarker<*>) -> (SelectionResult<T>),
    val isItemSelectedProvider: (SelectionTypeItemMarker<*>) -> Boolean = { false }
) : Parcelable {

    @Parcelize
    data class RoleSelection(val currentRole: Role? = null) : SelectionRequestType<Role>(
            recyclerViewList = Role.values().toList(),
            titleProvider = { it.getString(R.string.roleSelection) },
            resultProvider = { SelectionResult.RoleSelectionResult(it as Role) },
            isItemSelectedProvider = { currentRole == it }
    )

    @Parcelize
    data class DegreeSelection(val currentDegree: Degree? = null) : SelectionRequestType<Degree>(
            recyclerViewList = Degree.values().toList(),
            titleProvider = { it.getString(R.string.degreeSelection) },
            resultProvider = { SelectionResult.DegreeSelectionResult(it as Degree) },
            isItemSelectedProvider = { currentDegree == it }
    )

    @Parcelize
    data class ShuffleTypeSelection(val currentShuffleType: QuestionnaireShuffleType) : SelectionRequestType<QuestionnaireShuffleType>(
            recyclerViewList = QuestionnaireShuffleType.values().toList(),
            titleProvider = { it.getString(R.string.shuffleTypeSelection) },
            resultProvider = { SelectionResult.ShuffleTypeSelectionResult(it as QuestionnaireShuffleType) },
            isItemSelectedProvider = { currentShuffleType == it }
    )

    @Parcelize
    data class LanguageSelection(val currentLanguage: QuizAppLanguage) : SelectionRequestType<QuizAppLanguage>(
            recyclerViewList = QuizAppLanguage.values().toList(),
            titleProvider = { it.getString(R.string.languageSelection) },
            resultProvider = { SelectionResult.LanguageSelectionResult(it as QuizAppLanguage) },
            isItemSelectedProvider = { currentLanguage == it }
    )

    @Parcelize
    data class ThemeSelection(val currentTheme: QuizAppTheme) : SelectionRequestType<QuizAppTheme>(
            recyclerViewList = QuizAppTheme.values().toList(),
            titleProvider = { it.getString(R.string.themeSelection) },
            resultProvider = { SelectionResult.ThemeSelectionResult(it as QuizAppTheme) },
            isItemSelectedProvider = { currentTheme == it }
    )

    @Parcelize
    data class UserMoreOptionsSelection(val user: User) : SelectionRequestType<UserMoreOptionsItem>(
            recyclerViewList = UserMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, user.userName) },
            resultProvider = { SelectionResult.UserMoreOptionsSelectionResult(user, it as UserMoreOptionsItem) }
    )

    @Parcelize
    data class CourseOfStudiesMoreOptionsSelection(val courseOfStudies: CourseOfStudies) : SelectionRequestType<CosMoreOptionsItem>(
            recyclerViewList = CosMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, courseOfStudies.name) },
            resultProvider = { SelectionResult.CourseOfStudiesMoreOptionsResult(courseOfStudies, it as CosMoreOptionsItem) }
    )

    @Parcelize
    data class FacultyMoreOptionsSelection(val faculty: Faculty) : SelectionRequestType<FacultyMoreOptionsItem>(
            recyclerViewList = FacultyMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, faculty.name) },
            resultProvider = { SelectionResult.FacultyMoreOptionsSelectionResult(faculty, it as FacultyMoreOptionsItem) }
    )

    @Parcelize
    data class RemoteOrderBySelection(val currentValue: RemoteQuestionnaireOrderBy) : SelectionRequestType<RemoteQuestionnaireOrderBy>(
            recyclerViewList = RemoteQuestionnaireOrderBy.values().toList(),
            titleProvider = { it.getString(R.string.orderByTypeSelection) },
            resultProvider = { SelectionResult.RemoteOrderBySelectionResult(it as RemoteQuestionnaireOrderBy) },
            isItemSelectedProvider = { currentValue == it }
    )

    @Parcelize
    data class LocalOrderBySelection(val currentValue: LocalQuestionnaireOrderBy) : SelectionRequestType<LocalQuestionnaireOrderBy>(
            recyclerViewList = LocalQuestionnaireOrderBy.values().toList(),
            titleProvider = { it.getString(R.string.orderByTypeSelection) },
            resultProvider = { SelectionResult.LocalOrderBySelectionResult(it as LocalQuestionnaireOrderBy) },
            isItemSelectedProvider = { currentValue == it }
    )

    @Parcelize
    data class ManageUsersOrderBySelection(val currentValue: ManageUsersOrderBy) : SelectionRequestType<ManageUsersOrderBy>(
            recyclerViewList = ManageUsersOrderBy.values().toList(),
            titleProvider = { it.getString(R.string.orderByTypeSelection) },
            resultProvider = { SelectionResult.UsersOrderBySelectionResult(it as ManageUsersOrderBy) },
            isItemSelectedProvider = { currentValue == it }
    )

    @Parcelize
    data class BrowseQuestionnaireMoreOptionsSelection(val browsableQuestionnaire: BrowsableQuestionnaire) : SelectionRequestType<BrowseQuestionnaireMoreOptionsItem>(
            recyclerViewList = BrowseQuestionnaireMoreOptionsItem.values().toList(),
            titleProvider = { it.getString(R.string._ph, browsableQuestionnaire.title) },
            resultProvider = { SelectionResult.RemoteQuestionnaireMoreOptionsSelectionResult(browsableQuestionnaire, it as BrowseQuestionnaireMoreOptionsItem) }
    )

    @Parcelize
    data class AddEditAnswerMoreOptionsSelection(val answer: Answer) : SelectionRequestType<AddEditAnswerMoreOptionsItem>(
        recyclerViewList = AddEditAnswerMoreOptionsItem.values().toList(),
        titleProvider = { null },
        resultProvider = { SelectionResult.AddEditAnswerMoreOptionsSelectionResult(answer, it as AddEditAnswerMoreOptionsItem) }
    )

    @Parcelize
    data class AddEditQuestionMoreOptionsSelection(val questionWithAnswers: QuestionWithAnswers) : SelectionRequestType<AddEditQuestionMoreOptionsItem>(
        recyclerViewList = AddEditQuestionMoreOptionsItem.values().toList(),
        titleProvider = { null },
        resultProvider = { SelectionResult.AddEditQuestionMoreOptionsSelectionResult(questionWithAnswers, it as AddEditQuestionMoreOptionsItem) }
    )





    //    @Parcelize
//    data class QuestionnaireMoreOptionsSelection(val questionnaire: Questionnaire, val user: User) : SelectionRequestType<QuestionnaireMoreOptionsItem>(
//        recyclerViewList = QuestionnaireMoreOptionsItem.getMenuList(questionnaire, user),
//    )
    //        is QuestionnaireMoreOptionsSelection -> context.getString(R.string._ph, questionnaire.title)
}