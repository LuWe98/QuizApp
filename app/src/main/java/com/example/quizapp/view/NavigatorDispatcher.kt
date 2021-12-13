package com.example.quizapp.view

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import com.example.quizapp.MainNavGraphDirections
import com.example.quizapp.R
import com.example.quizapp.extensions.initMaterialElevationScale
import com.example.quizapp.extensions.initMaterialZAxisAnimationForCaller
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionnaireDirections
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminManageCourseOfStudiesDirections
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminManageFacultiesDirections
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminManageUsersDirections
import com.example.quizapp.view.fragments.authscreen.FragmentAuthDirections
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.view.fragments.homescreen.FragmentHomeDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizOverviewDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizResultDirections
import com.example.quizapp.view.fragments.settingsscreen.FragmentSettingsDirections
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

typealias NavigationCommand = (NavController, NavHostFragment) -> Unit

val QuizActivity.navHostFragment get() : NavHostFragment = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

val QuizActivity.navController get() = navHostFragment.navController

val NavHostFragment.currentFragment get() : Fragment = childFragmentManager.fragments.first()


@ActivityRetainedScoped
class NavigatorDispatcher @Inject constructor() {

    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }

    private val navEventChannel = Channel<NavigationCommand>()

    val navigationEventChannelFlow = navEventChannel.receiveAsFlow()


    suspend fun navigate(@IdRes id: Int) = navEventChannel.send { navController, _ ->
        navController.navigate(id)
    }

    suspend fun popBackStack() = navEventChannel.send { navController, _ ->
        navController.popBackStack()
    }

    suspend fun navigateToAuthScreen() = navEventChannel.send { navController, _ ->
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentHome, true).build()
        navController.navigate(MainNavGraphDirections.actionGlobalGoToAuthScreen(), navOptions)
    }

    suspend fun navigateToHomeScreen() = navEventChannel.send { navController, _ ->
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAuth, true).build()
        navController.navigate(FragmentAuthDirections.actionFragmentAuthToFragmentHome(), navOptions)
    }

    suspend fun navigateToSettingsScreen() = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(MainNavGraphDirections.actionGlobalSettingsNavGraph())
    }

    suspend fun navigateToSearchScreen() = navEventChannel.send { navController, _ ->
        navController.navigate(MainNavGraphDirections.actionGlobalFragmentSearch())
    }

    suspend fun navigateToAddEditQuestionnaireScreen(completeQuestionnaire: CompleteQuestionnaire? = null, copy: Boolean = false) = navEventChannel.send { navController, _ ->
        navController.navigate(MainNavGraphDirections.actionGlobalAddEditQuestionnaireNavGraph(completeQuestionnaire, copy))
    }

    suspend fun navigateToAddEditQuestionScreen(questionPosition: Int, questionWithAnswers: QuestionWithAnswers? = null) =
        navEventChannel.send { navController, navHostFragment ->
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToFragmentAddEditQuestion(questionPosition, questionWithAnswers))
        }


    //QUIZ SCREENS
    suspend fun navigateToQuizScreen(questionnaireId: String) = navEventChannel.send { navController, _ ->
        //val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentHome, false).build()
        navController.navigate(MainNavGraphDirections.actionGlobalGoToQuizScreen(questionnaireId))
    }

    suspend fun navigateToQuizContainerScreen(questionPosition: Int = FIRST_QUESTION_POSITION, isShowSolutionScreen: Boolean = false) =
        navEventChannel.send { navController, navHostFragment ->
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition, isShowSolutionScreen))
        }

    suspend fun navigateToQuizResultScreen() = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build()
        navController.navigate(FragmentQuizQuestionsContainerDirections.actionFragmentQuizContainerToFragmentQuizResult(), navOptions)
    }

    suspend fun navigateToQuizContainerScreenFromResultScreen(showSolutions: Boolean) = navEventChannel.send { navController, _ ->
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build()
        navController.navigate(FragmentQuizResultDirections.actionFragmentQuizResultToFragmentQuizContainer(FIRST_QUESTION_POSITION, showSolutions), navOptions)
    }

    suspend fun navigateToQuizContainerScreenWithQuestionCardClick(questionPosition: Int, questionId: Long, clickedCard: CardView) =
        navEventChannel.send { navController, navHostFragment ->
            navHostFragment.currentFragment.apply {
                initMaterialElevationScale()
                clickedCard.transitionName = questionId.toString()
                val extras = FragmentNavigatorExtras(clickedCard to getString(R.string.questionClickedTransitionName))
                navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition, false), extras)
            }
        }


    //SELECTION DESTINATIONS
    suspend fun navigateToCourseOfStudiesSelection(selectedCourseOfStudiesIds: Array<String>) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfCourseOfStudiesSelection) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfCourseOfStudiesSelection(selectedCourseOfStudiesIds))
    }

    suspend fun navigateToFacultySelection(selectedFacultyIds: Array<String>) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfFacultySelection) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfFacultySelection(selectedFacultyIds))
    }

    suspend fun navigateToRemoteAuthorSelection(selectedAuthors: Array<AuthorInfo>) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfRemoteAuthorSelection) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfRemoteAuthorSelection(selectedAuthors))
    }

    suspend fun navigateToLocalAuthorSelection(selectedAuthorIds: Array<String>) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfLocalAuthorSelection) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfLocalAuthorSelection(selectedAuthorIds))
    }

    suspend fun navigateToSelectionDialog(selectionType: SelectionType) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfSelection) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfSelection(selectionType))
    }

    suspend fun navigateToLocalQuestionnaireFilterSelection() = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfLocalQuestionnaireFilterSelection) return@send
        navController.navigate(FragmentHomeDirections.actionGlobalBsdfLocalQuestionnaireFilterSelection())
    }

    suspend fun navigateToRemoteQuestionnaireFilterSelection(selectedAuthors: Array<AuthorInfo>) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfBrowseQuestionnaireFilterSelection) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfBrowseQuestionnaireFilterSelection(selectedAuthors))
    }


    //STRING PICKER DIALOG
    suspend fun navigateToUpdateStringDialog(initialValue: String, updateStringType: UpdateStringType) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.dfUpdateStringValue) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(updateStringType, initialValue))
    }


    //CONFIRMATION DIALOG
    suspend fun navigateToConfirmationDialog(confirmationType: ConfirmationType) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.dfConfirmation) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalDfConfirmation(confirmationType))
    }


    //DIALOGS
    suspend fun navigateToShareQuestionnaireDialog(questionnaireId: String) = navEventChannel.send { navController, _ ->
        navController.navigate(MainNavGraphDirections.actionGlobalDfShareQuestionnaire(questionnaireId))
    }

    suspend fun navigateToChangePasswordScreen() = navEventChannel.send { navController, _ ->
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToDfChangePassword())
    }


    //BOTTOM SHEETS
    suspend fun navigateToQuestionnaireMoreOptions(questionnaire: Questionnaire) = navEventChannel.send { navController, _ ->
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
    }


    //USER ADMIN SCREENS
    suspend fun navigateToAdminManageUsersScreen() = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageUsers())
    }

    suspend fun navigateToChangeUserRoleDialog(user: User) = navEventChannel.send { navController, _ ->
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build()
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfUserRoleChange(user), navOptions)
    }

    suspend fun navigateToAdminAddEditUser(user: User? = null) = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToFragmentAdminAddEditUser(user))
    }

    suspend fun navigateToAdminManageUsersFilterSelection(selectedRoles: Array<Role>) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.bsdfManageUsersFilterSelection) return@send
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfManageUsersFilterSelection(selectedRoles))
    }


    //FACULTY ADMIN SCREENS
    suspend fun navigateToAdminManageFacultiesScreen() = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageFaculties())
    }

    suspend fun navigateToAdminAddEditFaculty(faculty: Faculty? = null) = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToFragmentAdminAddEditFaculties(faculty))
    }


    //COURSE OF STUDIES SCREENS
    suspend fun navigateToAdminManageCourseOfStudiesScreen() = navEventChannel.send { navController, navHostFragment ->
        navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageCourseOfStudies())
    }

    suspend fun navigateToAdminAddEditCourseOfStudies(courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties? = null) =
        navEventChannel.send { navController, navHostFragment ->
            navHostFragment.currentFragment.initMaterialZAxisAnimationForCaller()
            navController.navigate(
                FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToFragmentAdminAddEditCourseOfStudies(
                    courseOfStudiesWithFaculties
                )
            )
        }


    //LOADING DIALOG
    suspend fun navigateToLoadingDialog(@StringRes messageRes: Int) = navEventChannel.send { navController, _ ->
        if (navController.currentDestination?.id == R.id.dfLoading) return@send
        navController.navigate(MainNavGraphDirections.actionGlobalDfLoading(messageRes))
    }

    suspend fun popLoadingDialog() = navEventChannel.send { navController, _ ->
        if (navController.backQueue[navController.backQueue.size - 1].destination.id == R.id.dfLoading) {
            navController.popBackStack()
        }
    }
}