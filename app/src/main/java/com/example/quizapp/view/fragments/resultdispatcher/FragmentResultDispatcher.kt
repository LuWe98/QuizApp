package com.example.quizapp.view.fragments.resultdispatcher

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.example.quizapp.extensions.currentFragment
import com.example.quizapp.extensions.navHostFragment
import com.example.quizapp.model.databases.properties.Degree
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.datastore.datawrappers.*
import com.example.quizapp.view.QuizActivity
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.Companion.getResultKey
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.FragmentResult
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionTypeItemMarker
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.*
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Listener for various FragmentResultTypes -> Acts as a wrapper for setFragmentResultListener.
 * Possible FragmentResults are represented in the [FragmentResult] sealed class.
 * This function automatically gets the result key for the given class and returns the corresponding result type.
 */
inline fun <reified ResultType : FragmentResult> Fragment.setFragmentResultEventListener(crossinline action: (ResultType) -> Unit) {
    setFragmentResultListener(getResultKey(ResultType::class)) { key, bundle ->
        bundle.getParcelable<ResultType>(key)?.let(action)
    }
}

@ActivityRetainedScoped
class FragmentResultDispatcher @Inject constructor() {

    companion object {

        const val FRAGMENT_RESULT_KEY_SUFFIX = "FragmentResultKey"

        fun <ResultType : FragmentResult> getResultKey(clazz: KClass<ResultType>) = clazz.simpleName.plus(FRAGMENT_RESULT_KEY_SUFFIX)

    }

    private val fragmentResultChannel = Channel<FragmentResult>()

    val fragmentResultChannelFlow = fragmentResultChannel.receiveAsFlow()


    suspend fun dispatch(result: FragmentResult) = fragmentResultChannel.send(result)


    sealed class FragmentResult : Parcelable {

        private val resultKey: String get() = this::class.simpleName.plus(FRAGMENT_RESULT_KEY_SUFFIX)

        fun execute(quizActivity: QuizActivity) {
            quizActivity.navHostFragment.currentFragment.apply {
                setFragmentResult(resultKey, Bundle().apply {
                    putParcelable(resultKey, this@FragmentResult)
                })
            }
        }

        @Parcelize
        data class FacultySelectionResult(val facultyIds: List<String>) : FragmentResult()

        @Parcelize
        data class CourseOfStudiesSelectionResult(val courseOfStudiesIds: List<String>) : FragmentResult()

        @Parcelize
        data class RemoteAuthorSelectionResult(val authors: List<AuthorInfo>) : FragmentResult()

        @Parcelize
        data class LocalAuthorSelectionResult(val authorIds: List<String>) : FragmentResult()

        @Parcelize
        data class ManageUsersFilterResult(val selectedRoles: Set<Role>) : FragmentResult()

        @Parcelize
        data class RemoteQuestionnaireFilterResult(val selectedAuthors: Set<AuthorInfo>) : FragmentResult()

        @Parcelize
        data class AddEditAnswerResult(val answer: Answer): FragmentResult()

    }

    sealed class ConfirmationResult : FragmentResult() {

        abstract val confirmed: Boolean

        @Parcelize
        class DeleteUserConfirmationResult(override val confirmed: Boolean, val user: User) : ConfirmationResult()

        @Parcelize
        class DeleteFacultyConfirmationResult(override val confirmed: Boolean, val faculty: Faculty) : ConfirmationResult()

        @Parcelize
        class DeleteCourseOfStudiesResult(override val confirmed: Boolean, val courseOfStudies: CourseOfStudies) : ConfirmationResult()

        @Parcelize
        class LogoutConfirmationResult(override val confirmed: Boolean) : ConfirmationResult()

        @Parcelize
        class LoadCsvFileConfirmationResult(override val confirmed: Boolean) : ConfirmationResult()

    }

    sealed class UpdateStringValueResult : FragmentResult() {

        abstract val updatedStringValue: String

        @Parcelize
        data class QuestionnaireTitleUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class QuestionnaireSubjectUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditQuestionAnswerTextUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditQuestionTextUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditFacultyAbbreviationUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditFacultyNameUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditCourseOfStudiesAbbreviationUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditCourseOfStudiesNameUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditUserNameUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

        @Parcelize
        data class AddEditUserPasswordUpdateResult(override val updatedStringValue: String) : UpdateStringValueResult()

    }

    sealed class SelectionResult<SelectedItemType : Enum<SelectedItemType>> : FragmentResult() {

        abstract val selectedItem: SelectionTypeItemMarker<SelectedItemType>

        @Parcelize
        data class RoleSelectionResult(override val selectedItem: Role): SelectionResult<Role>()

        @Parcelize
        data class DegreeSelectionResult(override val selectedItem: Degree): SelectionResult<Degree>()

        @Parcelize
        data class ShuffleTypeSelectionResult(override val selectedItem: QuestionnaireShuffleType): SelectionResult<QuestionnaireShuffleType>()

        @Parcelize
        data class LanguageSelectionResult(override val selectedItem: QuizAppLanguage): SelectionResult<QuizAppLanguage>()

        @Parcelize
        data class ThemeSelectionResult(override val selectedItem: QuizAppTheme): SelectionResult<QuizAppTheme>()

        @Parcelize
        data class RemoteOrderBySelectionResult(override val selectedItem: RemoteQuestionnaireOrderBy): SelectionResult<RemoteQuestionnaireOrderBy>()

        @Parcelize
        data class LocalOrderBySelectionResult(override val selectedItem: LocalQuestionnaireOrderBy): SelectionResult<LocalQuestionnaireOrderBy>()

        @Parcelize
        data class UsersOrderBySelectionResult(override val selectedItem: ManageUsersOrderBy): SelectionResult<ManageUsersOrderBy>()

        @Parcelize
        data class CourseOfStudiesMoreOptionsResult(
            val calledOnCourseOfStudies: CourseOfStudies,
            override val selectedItem: CosMoreOptionsItem
        ) : SelectionResult<CosMoreOptionsItem>()

        @Parcelize
        data class FacultyMoreOptionsSelectionResult(
            val calledOnFaculty: Faculty,
            override val selectedItem: FacultyMoreOptionsItem
        ) : SelectionResult<FacultyMoreOptionsItem>()

        @Parcelize
        data class UserMoreOptionsSelectionResult(
            val calledOnUser: User,
            override val selectedItem: UserMoreOptionsItem
        ): SelectionResult<UserMoreOptionsItem>()

        @Parcelize
        data class RemoteQuestionnaireMoreOptionsSelectionResult(
            val calledOnRemoteQuestionnaire: BrowsableQuestionnaire,
            override val selectedItem: BrowseQuestionnaireMoreOptionsItem
        ): SelectionResult<BrowseQuestionnaireMoreOptionsItem>()

        @Parcelize
        data class AddEditAnswerMoreOptionsSelectionResult(
            val calledOnAnswer: Answer,
            override val selectedItem : AddEditAnswerMoreOptionsItem
        ): SelectionResult<AddEditAnswerMoreOptionsItem>()
    }
}