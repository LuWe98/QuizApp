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
import javax.inject.Inject

@ActivityRetainedScoped
class Navigator @Inject constructor(
    private val activity: QuizActivity
) {

    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }

    private val navHostFragment get() = activity.supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment

    private val navController get() = navHostFragment.navController

    private val currentFragment: Fragment get() = navHostFragment.childFragmentManager.fragments.first()
    private val currentDestination get() = navController.currentDestination
    val currentDestinationId get() = currentDestination?.id
    private val currentBackStackEntry get() = navController.currentBackStackEntry
    private val currentSaveStateHandle get() = currentBackStackEntry!!.savedStateHandle
    private val previousBackStackEntry get() = navController.previousBackStackEntry
    private val previousSaveStateHandle get() = previousBackStackEntry!!.savedStateHandle


    fun addOnDestinationChangedListener(listener: NavController.OnDestinationChangedListener) {
        navController.addOnDestinationChangedListener(listener)
    }

    fun navigate(@IdRes id: Int) {
        navController.navigate(id)
    }

    fun popBackStack() = navController.popBackStack()

    fun navigateToAuthScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentHome, true).build()
        navController.navigate(MainNavGraphDirections.actionGlobalGoToAuthScreen(), navOptions)
    }

    fun navigateToHomeScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAuth, true).build()
        navController.navigate(FragmentAuthDirections.actionFragmentAuthToFragmentHome(), navOptions)
    }

    fun navigateToSettingsScreen() {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(MainNavGraphDirections.actionGlobalSettingsNavGraph())
    }

    fun navigateToSearchScreen() {
        navController.navigate(MainNavGraphDirections.actionGlobalFragmentSearch())
    }

    fun navigateToAddEditQuestionnaireScreen(completeQuestionnaire: CompleteQuestionnaire? = null, copy: Boolean = false) {
        navController.navigate(MainNavGraphDirections.actionGlobalAddEditQuestionnaireNavGraph(completeQuestionnaire, copy))
    }

    fun navigateToAddEditQuestionScreen(questionPosition: Int, questionWithAnswers: QuestionWithAnswers? = null) {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToFragmentAddEditQuestion(questionPosition, questionWithAnswers))
    }


    //QUIZ SCREENS
    fun navigateToQuizScreen(questionnaireId: String) {
        //val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentHome, false).build()
        navController.navigate(MainNavGraphDirections.actionGlobalGoToQuizScreen(questionnaireId))
    }

    fun navigateToQuizContainerScreen(questionPosition: Int = FIRST_QUESTION_POSITION, isShowSolutionScreen: Boolean = false) {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition, isShowSolutionScreen))
    }

    fun navigateToQuizResultScreen() {
        currentFragment.initMaterialZAxisAnimationForCaller()
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build()
        navController.navigate(FragmentQuizQuestionsContainerDirections.actionFragmentQuizContainerToFragmentQuizResult(), navOptions)
    }

    fun navigateToQuizContainerScreenFromResultScreen(showSolutions: Boolean) {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build()
        navController.navigate(FragmentQuizResultDirections.actionFragmentQuizResultToFragmentQuizContainer(FIRST_QUESTION_POSITION, showSolutions), navOptions)
    }

    fun navigateToQuizContainerScreenWithQuestionCardClick(questionPosition: Int, questionId: Long, clickedCard: CardView) {
        currentFragment.apply {
            initMaterialElevationScale()
            clickedCard.transitionName = questionId.toString()
            val extras = FragmentNavigatorExtras(clickedCard to getString(R.string.questionClickedTransitionName))
            navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition, false), extras)
        }
    }


    //SELECTION DESTINATIONS
    fun navigateToCourseOfStudiesSelection(selectedCourseOfStudiesIds: Array<String>) {
        if (currentDestinationId == R.id.bsdfCourseOfStudiesSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfCourseOfStudiesSelection(selectedCourseOfStudiesIds))
    }

    fun navigateToFacultySelection(selectedFacultyIds: Array<String>) {
        if (currentDestinationId == R.id.bsdfFacultySelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfFacultySelection(selectedFacultyIds))
    }

    fun navigateToRemoteAuthorSelection(selectedAuthors: Array<AuthorInfo>) {
        if (currentDestinationId == R.id.bsdfRemoteAuthorSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfRemoteAuthorSelection(selectedAuthors))
    }

    fun navigateToLocalAuthorSelection(selectedAuthorIds: Array<String>) {
        if (currentDestinationId == R.id.bsdfLocalAuthorSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfLocalAuthorSelection(selectedAuthorIds))
    }

    fun navigateToSelectionDialog(selectionType: SelectionRequestType<*>) {
        if (currentDestinationId == R.id.bsdfSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfSelection(selectionType))
    }

    fun navigateToLocalQuestionnaireFilterSelection() {
        if (currentDestinationId == R.id.bsdfLocalQuestionnaireFilterSelection) return
        navController.navigate(FragmentHomeDirections.actionGlobalBsdfLocalQuestionnaireFilterSelection())
    }

    fun navigateToRemoteQuestionnaireFilterSelection(selectedAuthors: Array<AuthorInfo>) {
        if (currentDestinationId == R.id.bsdfBrowseQuestionnaireFilterSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfBrowseQuestionnaireFilterSelection(selectedAuthors))
    }


    //STRING PICKER DIALOG
    fun navigateToUpdateStringDialog(resultType: UpdateStringValueResult) {
        if (currentDestinationId == R.id.dfUpdateStringValue) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(resultType))
    }


    //CONFIRMATION DIALOG
    fun navigateToConfirmationDialog(confirmationRequestType: ConfirmationRequestType) {
        if (currentDestinationId == R.id.dfConfirmation) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfConfirmation(confirmationRequestType))
    }


    //DIALOGS
    fun navigateToShareQuestionnaireDialog(questionnaireId: String) {
        navController.navigate(MainNavGraphDirections.actionGlobalDfShareQuestionnaire(questionnaireId))
    }

    fun navigateToChangePasswordScreen() {
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToDfChangePassword())
    }


    //BOTTOM SHEETS
    fun navigateToQuestionnaireMoreOptions(questionnaire: Questionnaire) {
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
    }


    //USER ADMIN SCREENS
    fun navigateToAdminManageUsersScreen() {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageUsers())
    }

    fun navigateToChangeUserRoleDialog(user: User) {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build()
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfUserRoleChange(user), navOptions)
    }

    fun navigateToAdminAddEditUser(user: User? = null) {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToFragmentAdminAddEditUser(user))
    }

    fun navigateToAdminManageUsersFilterSelection(selectedRoles: Array<Role>) {
        if (currentDestinationId == R.id.bsdfManageUsersFilterSelection) return
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfManageUsersFilterSelection(selectedRoles))
    }


    //FACULTY ADMIN SCREENS
    fun navigateToAdminManageFacultiesScreen() {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageFaculties())
    }

    fun navigateToAdminAddEditFaculty(faculty: Faculty? = null) {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToFragmentAdminAddEditFaculties(faculty))
    }


    //COURSE OF STUDIES SCREENS
    fun navigateToAdminManageCourseOfStudiesScreen() {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageCourseOfStudies())
    }

    fun navigateToAdminAddEditCourseOfStudies(courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties? = null) {
        currentFragment.initMaterialZAxisAnimationForCaller()
        navController.navigate(
            FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToFragmentAdminAddEditCourseOfStudies(
                courseOfStudiesWithFaculties
            )
        )
    }


    //LOADING DIALOG
    fun navigateToLoadingDialog(@StringRes messageRes: Int) {
        if (currentDestinationId == R.id.dfLoading) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfLoading(messageRes))
    }

    fun popLoadingDialog() {
        if (navController.backQueue[navController.backQueue.size - 1].destination.id == R.id.dfLoading) {
            navController.popBackStack()
        }
    }
}