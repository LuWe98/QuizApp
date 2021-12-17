package com.example.quizapp.view

import androidx.annotation.StringRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.quizapp.MainNavGraphDirections
import com.example.quizapp.R
import com.example.quizapp.extensions.currentFragment
import com.example.quizapp.extensions.initMaterialZAxisAnimationForCaller
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.fragments.resultdispatcher.UpdateStringValueResult
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionnaireDirections
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminManageCourseOfStudiesDirections
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminManageFacultiesDirections
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminManageUsersDirections
import com.example.quizapp.view.fragments.authscreen.FragmentAuthDirections
import com.example.quizapp.view.fragments.resultdispatcher.requests.ConfirmationRequestType
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.view.fragments.homescreen.FragmentHomeDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizOverviewDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizResultDirections
import com.example.quizapp.view.fragments.settingsscreen.FragmentSettingsDirections
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@ActivityRetainedScoped
class NavigationDispatcher @Inject constructor() {

    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }

    private val navigationChannel = Channel<NavigationEvent>()

    val navigationChannelFlow = navigationChannel.receiveAsFlow()

    suspend fun dispatch(event: NavigationEvent) = navigationChannel.send(event)

    sealed class NavigationEvent {
        object NavigateBack : NavigationEvent()
        object ToAuthScreen : NavigationEvent()
        object FromAuthToHomeScreen : NavigationEvent()
        object FromHomeToSettingsScreen : NavigationEvent()
        object FromHomeToSearchScreen : NavigationEvent()
        class FromHomeToAddEditQuestionnaire(val completeQuestionnaire: CompleteQuestionnaire? = null, val copy: Boolean = false) : NavigationEvent()
        class FromAddEditQuestionnaireToAddEditQuestion(val questionPosition: Int, val questionWithAnswers: QuestionWithAnswers? = null) : NavigationEvent()
        class ToQuizScreen(val questionnaireId: String) : NavigationEvent()
        class FromQuizToQuizContainerScreen(val questionPosition: Int = FIRST_QUESTION_POSITION, val isShowSolutionScreen: Boolean = false) : NavigationEvent()
        object FromQuizContainerToQuizResultScreen : NavigationEvent()
        class FromQuizResultToQuizContainerScreen(val showSolutions: Boolean) : NavigationEvent()
        class ToCourseOfStudiesSelectionDialog(val selectedCourseOfStudiesIds: Array<String>) : NavigationEvent()
        class ToFacultySelectionDialog(val selectedFacultyIds: Array<String>) : NavigationEvent()
        class ToRemoteAuthorSelectionDialog(val selectedAuthors: Array<AuthorInfo>) : NavigationEvent()
        class ToLocalAuthorSelectionDialog(val selectedAuthorIds: Array<String>) : NavigationEvent()
        class ToSelectionDialog(val selectionType: SelectionRequestType<*>) : NavigationEvent()
        object ToLocalQuestionnaireFilterDialog : NavigationEvent()
        class ToRemoteQuestionnaireFilterDialog(val selectedAuthors: Array<AuthorInfo>) : NavigationEvent()
        class ToStringUpdateDialog(val resultType: UpdateStringValueResult) : NavigationEvent()
        class ToConfirmationDialog(val confirmationRequestType: ConfirmationRequestType) : NavigationEvent()
        class ToShareQuestionnaireDialog(val questionnaireId: String) : NavigationEvent()
        object ToChangePasswordDialog : NavigationEvent()
        class ToQuestionnaireMoreOptionsDialog(val questionnaire: Questionnaire) : NavigationEvent()
        object FromSettingsToManageUsersScreen : NavigationEvent()
        object FromSettingsToManageCoursesOfStudiesScreen : NavigationEvent()
        object FromSettingToManageFacultiesScreen : NavigationEvent()
        class FromManageUsersToChangeUserRoleDialog(val user: User) : NavigationEvent()
        class FromManageUsersToAddEditUser(val user: User? = null) : NavigationEvent()
        class FromManageUsersToUserFilterDialog(val selectedRoles: Array<Role>) : NavigationEvent()
        class FromManageFacultiesToAddEditFaulty(val faculty: Faculty? = null) : NavigationEvent()
        class FromManageCourseOfStudiesToAddEditCourseOfStudies(val courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties? = null) : NavigationEvent()
        class ToLoadingDialog(@StringRes val messageRes: Int) : NavigationEvent()
        object PopLoadingDialog : NavigationEvent()

        fun execute(navController: NavController, navHostFragment: NavHostFragment) = runCatching {
            when (this) {
                NavigateBack -> {
                    navController.popBackStack()
                }

                ToAuthScreen -> {
                    NavOptions.Builder().setPopUpTo(R.id.fragmentHome, true).build().let { navOptions ->
                        navController.navigate(MainNavGraphDirections.actionGlobalGoToAuthScreen(), navOptions)
                    }
                }

                FromAuthToHomeScreen -> {
                    NavOptions.Builder().setPopUpTo(R.id.fragmentAuth, true).build().let { navOptions ->
                        navController.navigate(FragmentAuthDirections.actionFragmentAuthToFragmentHome(), navOptions)
                    }
                }

                FromHomeToSettingsScreen -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(MainNavGraphDirections.actionGlobalSettingsNavGraph())
                }

                FromHomeToSearchScreen -> {
                    navController.navigate(MainNavGraphDirections.actionGlobalFragmentSearch())
                }

                is FromHomeToAddEditQuestionnaire -> {
                    navController.navigate(
                        MainNavGraphDirections.actionGlobalAddEditQuestionnaireNavGraph(
                            completeQuestionnaire,
                            copy
                        )
                    )
                }

                is FromAddEditQuestionnaireToAddEditQuestion -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(
                        FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToFragmentAddEditQuestion(
                            questionPosition,
                            questionWithAnswers
                        )
                    )
                }

                is ToQuizScreen -> {
                    navController.navigate(MainNavGraphDirections.actionGlobalGoToQuizScreen(questionnaireId))
                }

                is FromQuizToQuizContainerScreen -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition, isShowSolutionScreen))
                }

                FromQuizContainerToQuizResultScreen -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build().let { navOptions ->
                        navController.navigate(FragmentQuizQuestionsContainerDirections.actionFragmentQuizContainerToFragmentQuizResult(), navOptions)
                    }
                }

                is FromQuizResultToQuizContainerScreen -> {
                    NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build().let { navOptions ->
                        navController.navigate(
                            FragmentQuizResultDirections.actionFragmentQuizResultToFragmentQuizContainer(FIRST_QUESTION_POSITION, showSolutions),
                            navOptions
                        )
                    }
                }

                is ToCourseOfStudiesSelectionDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfCourseOfStudiesSelection) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfCourseOfStudiesSelection(selectedCourseOfStudiesIds))
                }

                is ToFacultySelectionDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfFacultySelection) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfFacultySelection(selectedFacultyIds))
                }

                is ToRemoteAuthorSelectionDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfRemoteAuthorSelection) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfRemoteAuthorSelection(selectedAuthors))
                }

                is ToLocalAuthorSelectionDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfLocalAuthorSelection) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfLocalAuthorSelection(selectedAuthorIds))
                }

                is ToSelectionDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfSelection) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfSelection(selectionType))
                }

                ToLocalQuestionnaireFilterDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfLocalQuestionnaireFilterSelection) return@runCatching
                    navController.navigate(FragmentHomeDirections.actionGlobalBsdfLocalQuestionnaireFilterSelection())
                }

                is ToRemoteQuestionnaireFilterDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfBrowseQuestionnaireFilterSelection) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfBrowseQuestionnaireFilterSelection(selectedAuthors))
                }

                is ToStringUpdateDialog -> {
                    if (navController.currentDestination?.id == R.id.dfUpdateStringValue) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(resultType))
                }

                is ToConfirmationDialog -> {
                    if (navController.currentDestination?.id == R.id.dfConfirmation) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalDfConfirmation(confirmationRequestType))
                }

                is ToShareQuestionnaireDialog -> {
                    navController.navigate(MainNavGraphDirections.actionGlobalDfShareQuestionnaire(questionnaireId))
                }

                ToChangePasswordDialog -> {
                    navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToDfChangePassword())
                }

                is ToQuestionnaireMoreOptionsDialog -> {
                    navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
                }

                FromSettingsToManageUsersScreen -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageUsers())
                }

                FromSettingsToManageCoursesOfStudiesScreen -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageCourseOfStudies())
                }

                FromSettingToManageFacultiesScreen -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageFaculties())
                }

                is FromManageUsersToChangeUserRoleDialog -> {
                    NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build().let { navOptions ->
                        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfUserRoleChange(user), navOptions)
                    }
                }

                is FromManageUsersToAddEditUser -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToFragmentAdminAddEditUser(user))
                }

                is FromManageUsersToUserFilterDialog -> {
                    if (navController.currentDestination?.id == R.id.bsdfManageUsersFilterSelection) return@runCatching
                    navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfManageUsersFilterSelection(selectedRoles))
                }

                is FromManageFacultiesToAddEditFaulty -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToFragmentAdminAddEditFaculties(faculty))
                }
                is FromManageCourseOfStudiesToAddEditCourseOfStudies -> {
                    navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
                    navController.navigate(
                        FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToFragmentAdminAddEditCourseOfStudies(
                            courseOfStudiesWithFaculties
                        )
                    )
                }

                is ToLoadingDialog -> {
                    if (navController.currentDestination?.id == R.id.dfLoading) return@runCatching
                    navController.navigate(MainNavGraphDirections.actionGlobalDfLoading(messageRes))
                }

                PopLoadingDialog -> {
                    if (navController.backQueue[navController.backQueue.size - 1].destination.id == R.id.dfLoading) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}