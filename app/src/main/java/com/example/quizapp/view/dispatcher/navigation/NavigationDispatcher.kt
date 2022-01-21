package com.example.quizapp.view.dispatcher.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import com.example.quizapp.MainNavGraphDirections
import com.example.quizapp.R
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.QuizActivity
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.bindingsuperclasses.BindingDialogFragment
import com.example.quizapp.view.dispatcher.DispatchEvent
import com.example.quizapp.view.dispatcher.Dispatcher
import com.example.quizapp.view.dispatcher.DispatcherEventChannelContainer
import com.example.quizapp.view.dispatcher.fragmentresult.requests.ConfirmationRequestType
import com.example.quizapp.view.dispatcher.fragmentresult.requests.UpdateStringRequestType
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.view.fragments.addeditquestionnairescreen.BsdfAddEditQuestionnaireQuestionListDirections
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionDirections
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionnaireDirections
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminManageCourseOfStudiesDirections
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminManageFacultiesDirections
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminManageUsersDirections
import com.example.quizapp.view.fragments.authscreen.FragmentAuthDirections
import com.example.quizapp.view.fragments.homescreen.FragmentHomeDirections
import com.example.quizapp.view.fragments.quizscreen.BsdfQuizOverviewQuestionListDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizOverviewDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizResultDirections
import com.example.quizapp.view.fragments.settingsscreen.FragmentSettingsDirections
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class NavigationDispatcher @Inject constructor(
    private val eventQueue: DispatcherEventChannelContainer
) : Dispatcher<NavigationDispatcher.NavigationEvent> {

    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }

    private var _lastDestination: NavDestination? = null

    val lastDestination get() = _lastDestination

    private var _isLastDestinationDialog: Boolean = false

    val isLastDestinationDialog get() = _isLastDestinationDialog

    override suspend fun dispatch(event: NavigationEvent) = eventQueue.dispatchToQueue(event)

    sealed class NavigationEvent(private val navAction: QuizActivity.() -> Unit) : DispatchEvent {

        /**
         * Executes the [NavigationEvent] navAction statement which causes app navigation.
         */
        override suspend fun execute(quizActivity: QuizActivity) {
            runCatching {
                quizActivity.apply {
                    navigationDispatcher._lastDestination = navController.currentDestination
                    navigationDispatcher._isLastDestinationDialog = navHostFragment.currentFragment is BindingDialogFragment<*> ||
                            navHostFragment.currentFragment is BindingBottomSheetDialogFragment<*>
                }
                navAction(quizActivity)
            }
        }

        object NavigateBack : NavigationEvent({
            navController.popBackStack()
        })

        object ToAuthScreen : NavigationEvent({
            NavOptions.Builder().setPopUpTo(R.id.fragmentHome, true).build().let { navOptions ->
                navController.navigate(MainNavGraphDirections.actionGlobalGoToAuthScreen(), navOptions)
            }
        })

        object FromAuthToHomeScreen : NavigationEvent({
            NavOptions.Builder().setPopUpTo(R.id.fragmentAuth, true).build().let { navOptions ->
                navController.navigate(FragmentAuthDirections.actionFragmentAuthToFragmentHome(), navOptions)
            }
        })

        object FromHomeToSettingsScreen : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(MainNavGraphDirections.actionGlobalSettingsNavGraph())
        })

        object FromHomeToSearchScreen : NavigationEvent({
            navController.navigate(MainNavGraphDirections.actionGlobalFragmentSearch())
        })

        class FromHomeToAddEditQuestionnaire(private val completeQuestionnaire: CompleteQuestionnaire? = null, private val copy: Boolean = false) : NavigationEvent({
            navController.navigate(
                MainNavGraphDirections.actionGlobalAddEditQuestionnaireNavGraph(
                    completeQuestionnaire,
                    copy
                )
            )
        })

        class FromAddEditQuestionnaireToAddEditQuestion(private val questionPosition: Int, private val questionWithAnswers: QuestionWithAnswers? = null) : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(
                FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToFragmentAddEditQuestion(
                    questionPosition,
                    questionWithAnswers
                )
            )
        })

        class FromAddEditQuestionToAddEditAnswer(private val currentAnswer: Answer?) : NavigationEvent({
            navController.navigate(FragmentAddEditQuestionDirections.actionFragmentAddEditQuestionToDfAddEditAnswer(currentAnswer))
        })

        class FromAddEditQuestionnaireQuestionListToAddEditQuestion(private val questionPosition: Int, private val questionWithAnswers: QuestionWithAnswers? = null) :
            NavigationEvent({
                navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                navController.navigate(
                    BsdfAddEditQuestionnaireQuestionListDirections.actionBsdfAddEditQuestionnaireQuestionListToFragmentAddEditQuestion(
                        questionPosition,
                        questionWithAnswers
                    )
                )
            })

        object FromAddEditQuestionnaireToBsdfQuestionList : NavigationEvent({
            navController.navigate(FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToBsdfAddEditQuestionnaireQuestionList())
        })

        class ToQuizScreen(private val questionnaireId: String) : NavigationEvent({
            navController.navigate(MainNavGraphDirections.actionGlobalGoToQuizScreen(questionnaireId))
        })

        object FromQuizToQuestionListDialog : NavigationEvent({
            navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToBsdfQuizOverviewQuestionList())
        })

        class FromQuizQuestionListToQuizContainerScreen(
            private val questionPosition: Int = FIRST_QUESTION_POSITION,
            private val isShowSolutionScreen: Boolean = false
        ): NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(BsdfQuizOverviewQuestionListDirections.actionBsdfQuizOverviewQuestionListToFragmentQuizContainer(questionPosition, isShowSolutionScreen))
        })

        class FromQuizToQuizContainerScreen(private val isShowSolutionScreen: Boolean = false) : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(FIRST_QUESTION_POSITION, isShowSolutionScreen))
        })

        object FromQuizContainerToQuizResultScreen : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build().let { navOptions ->
                navController.navigate(FragmentQuizQuestionsContainerDirections.actionFragmentQuizContainerToFragmentQuizResult(), navOptions)
            }
        })

        class FromQuizResultToQuizContainerScreen(private val showSolutions: Boolean) : NavigationEvent({
            NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build().let { navOptions ->
                navController.navigate(
                    FragmentQuizResultDirections.actionFragmentQuizResultToFragmentQuizContainer(FIRST_QUESTION_POSITION, showSolutions),
                    navOptions
                )
            }
        })

        class ToCourseOfStudiesSelectionDialog(private val selectedCourseOfStudiesIds: Collection<String>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfCourseOfStudiesSelection) {
                navController.navigate(MainNavGraphDirections.actionGlobalBsdfCourseOfStudiesSelection(selectedCourseOfStudiesIds.toTypedArray()))
            }
        })

        class ToFacultySelectionDialog(private val selectedFacultyIds: Collection<String>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfFacultySelection) {
                navController.navigate(MainNavGraphDirections.actionGlobalBsdfFacultySelection(selectedFacultyIds.toTypedArray()))
            }
        })

        class ToRemoteAuthorSelectionDialog(private val selectedAuthors: Collection<AuthorInfo>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfRemoteAuthorSelection) {
                navController.navigate(MainNavGraphDirections.actionGlobalBsdfRemoteAuthorSelection(selectedAuthors.toTypedArray()))
            }
        })

        class ToLocalAuthorSelectionDialog(private val selectedAuthorIds: Collection<String>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfLocalAuthorSelection) {
                navController.navigate(MainNavGraphDirections.actionGlobalBsdfLocalAuthorSelection(selectedAuthorIds.toTypedArray()))
            }
        })

        class ToSelectionDialog(private val selectionType: SelectionRequestType<*>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfSelection) {
                navController.navigate(MainNavGraphDirections.actionGlobalBsdfSelection(selectionType))
            }
        })

        object ToLocalQuestionnaireFilterDialog : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfLocalQuestionnaireFilterSelection) {
                navController.navigate(FragmentHomeDirections.actionGlobalBsdfLocalQuestionnaireFilterSelection())
            }
        })

        class ToRemoteQuestionnaireFilterDialog(private val selectedAuthors: Collection<AuthorInfo>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfBrowseQuestionnaireFilterSelection) {
                navController.navigate(MainNavGraphDirections.actionGlobalBsdfBrowseQuestionnaireFilterSelection(selectedAuthors.toTypedArray()))
            }
        })

        class ToStringUpdateDialog(private val requestType: UpdateStringRequestType) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.dfUpdateStringValue) {
                navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(requestType))
            }
        })

        class ToConfirmationDialog(private val confirmationRequestType: ConfirmationRequestType) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.dfConfirmation) {
                navController.navigate(MainNavGraphDirections.actionGlobalDfConfirmation(confirmationRequestType))
            }
        })

        class ToShareQuestionnaireDialog(private val questionnaireId: String) : NavigationEvent({
            navController.navigate(MainNavGraphDirections.actionGlobalDfShareQuestionnaire(questionnaireId))
        })

        object ToChangePasswordDialog : NavigationEvent({
            navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToDfChangePassword())
        })

        class ToQuestionnaireMoreOptionsDialog(private val questionnaire: Questionnaire) : NavigationEvent({
            navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
        })

        object FromSettingsToManageUsersScreen : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageUsers())
        })

        object FromSettingsToManageCoursesOfStudiesScreen : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageCourseOfStudies())
        })

        object FromSettingToManageFacultiesScreen : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageFaculties())
        })

        class FromManageUsersToChangeUserRoleDialog(private val user: User) : NavigationEvent({
            NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build().let { navOptions ->
                navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfUserRoleChange(user), navOptions)
            }
        })

        class FromManageUsersToAddEditUser(private val user: User? = null) : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToFragmentAdminAddEditUser(user))
        })

        class FromManageUsersToUserFilterDialog(private val selectedRoles: Array<Role>) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.bsdfManageUsersFilterSelection) {
                navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfManageUsersFilterSelection(selectedRoles))
            }
        })

        class FromManageFacultiesToAddEditFaulty(private val faculty: Faculty? = null) : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToFragmentAdminAddEditFaculties(faculty))
        })

        class FromManageCourseOfStudiesToAddEditCourseOfStudies(private val courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties? = null) : NavigationEvent({
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(
                FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToFragmentAdminAddEditCourseOfStudies(
                    courseOfStudiesWithFaculties
                )
            )
        })

        class ToLoadingDialog(@StringRes private val messageRes: Int) : NavigationEvent({
            if (navController.currentDestination?.id != R.id.dfLoading) {
                navController.navigate(MainNavGraphDirections.actionGlobalDfLoading(messageRes))
            }
        })

        object PopLoadingDialog : NavigationEvent({
            if (navController.backQueue[navController.backQueue.size - 1].destination.id == R.id.dfLoading) {
                navController.popBackStack()
            }
        })
    }
}