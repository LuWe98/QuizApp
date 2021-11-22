package com.example.quizapp.view

import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import com.example.quizapp.MainNavGraphDirections
import com.example.quizapp.R
import com.example.quizapp.extensions.initMaterialElevationScale
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionnaireDirections
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.BsdfManageCourseOfStudiesMoreOptionsDirections
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminManageCourseOfStudiesDirections
import com.example.quizapp.view.fragments.adminscreens.managefaculties.BsdfManageFacultiesMoreOptionsDirections
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminManageFacultiesDirections
import com.example.quizapp.view.fragments.adminscreens.manageusers.BsdfUserMoreOptionsDirections
import com.example.quizapp.view.fragments.adminscreens.manageusers.FragmentAdminManageUsersDirections
import com.example.quizapp.view.fragments.authscreen.FragmentAuthDirections
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateStringValueType
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizOverviewDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestionsContainerDirections
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizResultDirections
import com.example.quizapp.view.fragments.settingsscreen.FragmentSettingsDirections
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(
    private val navHostWeakReference: WeakReference<NavHostFragment>
) {

    private val naHostFragment get() = navHostWeakReference.get()!!
    val navController get() = naHostFragment.navController

    val currentFragment: Fragment get() = naHostFragment.childFragmentManager.fragments.first()
    val currentDestination get() = navController.currentDestination
    val currentDestinationId get() = currentDestination?.id
    val currentBackStackEntry get() = navController.currentBackStackEntry
    val currentSaveStateHandle get() = currentBackStackEntry!!.savedStateHandle
    val previousBackStackEntry get() = navController.previousBackStackEntry
    val previousSaveStateHandle get() = previousBackStackEntry!!.savedStateHandle


    fun addOnDestinationChangedListener(listener: NavController.OnDestinationChangedListener) {
        navController.addOnDestinationChangedListener(listener)
    }

    fun navigate(@IdRes id: Int) {
        navController.navigate(id)
    }

    fun popBackStack() {
        navController.popBackStack()
    }

    fun navigateToQuizScreen(questionnaireId: String) {
        navController.navigate(MainNavGraphDirections.actionGlobalGoToQuizScreen(questionnaireId))
    }

    fun navigateToQuizContainerScreen(questionPosition: Int = FIRST_QUESTION_POSITION, isShowSolutionScreen: Boolean = false) {
        navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition, isShowSolutionScreen))
    }

    fun navigateToQuizResultScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentQuizOverview, false).build()
        navController.navigate(FragmentQuizQuestionsContainerDirections.actionFragmentQuizContainerToFragmentQuizResult(), navOptions)
    }

    fun navigateToQuizContainerScreenFromResultScreen(showSolutions: Boolean){
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


    fun navigateToSettingsScreen(){
        navController.navigate(MainNavGraphDirections.actionGlobalSettingsNavGraph())
    }

    fun navigateToLoginScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentHome, true).build()
        navController.navigate(MainNavGraphDirections.actionGlobalGoToAuthScreen(), navOptions)
    }

    fun navigateToHomeScreen() {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAuth, true).build()
        navController.navigate(FragmentAuthDirections.actionFragmentAuthToFragmentHome(), navOptions)
    }

    fun navigateToSearchScreen() {
        navController.navigate(MainNavGraphDirections.actionGlobalFragmentSearch())
    }

    fun navigateToQuestionnaireMoreOptions(questionnaire: Questionnaire){
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
    }


    fun navigateToShareQuestionnaireDialog(questionnaireId: String) {
        navController.navigate(MainNavGraphDirections.actionGlobalDfShareQuestionnaire(questionnaireId))
    }

    fun navigateToBackdropFragment(){
        navController.navigate(MainNavGraphDirections.actionGlobalBackdropFragment())
    }

    fun navigateToThemeSelection(){
        if(currentDestinationId == R.id.bsdfThemeSelection) return
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToBsdfThemeSelection())
    }

    fun navigateToLanguageSelection(){
        if(currentDestinationId == R.id.bsdfLanguageSelection) return
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToBsdfLanguageSelection())
    }

    fun navigateToShuffleTypeSelection(){
        if(currentDestinationId == R.id.bsdfShuffleTypeSelection) return
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToBsdfShuffleTypeSelection())
    }

    fun navigateToAddEditQuestionnaireScreen(completeQuestionnaire: CompleteQuestionnaire? = null, copy: Boolean = false) {
        navController.navigate(MainNavGraphDirections.actionGlobalAddEditQuestionnaireNavGraph(completeQuestionnaire, copy))
    }

    fun navigateToAddEditQuestionScreen(questionPosition: Int, questionWithAnswers: QuestionWithAnswers? = null) {
        navController.navigate(FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToFragmentAddEditQuestion(questionPosition, questionWithAnswers))
    }


    fun navigateToCourseOfStudiesSelection(selectedCourseOfStudiesIds: Set<String>){
        if(currentDestinationId == R.id.bsdfCourseOfStudiesSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfCourseOfStudiesSelection(selectedCourseOfStudiesIds.toTypedArray()))
    }

    fun navigateToFacultySelection(selectedFacultyIds: Set<String>) {
        if(currentDestinationId == R.id.bsdfFacultySelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfFacultySelection(selectedFacultyIds.toTypedArray()))
    }

    fun navigateToUpdateStringValueDialog(initialValue: String, updateStringValueType: DfUpdateStringValueType) {
        if(currentDestinationId == R.id.dfUpdateStringValue) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(updateStringValueType, initialValue))
    }

    fun navigateToLogoutWarningScreen() {
        if(currentDestinationId == R.id.dfLogoutWarning) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfLogoutWarning())
    }







    //ADMIN SCREENS

    fun navigateToAdminManageUsersScreen(){
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdmin())
    }

    fun navigateToUserMoreOptionsDialog(user: User){
        if(currentDestinationId == R.id.bsdfUserMoreOptions) return
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfUserMoreOptions(user))
    }

    fun navigateToChangeUserRoleDialog(user: User){
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build()
        navController.navigate(BsdfUserMoreOptionsDirections.actionBsdfUserMoreOptionsToBsdfChangeUserRole(user), navOptions)
    }

    fun navigateToAdminUserDeletionConfirmation(user: User) {
        if(currentDestinationId == R.id.dfAdminUserDeletionConfirmation) return
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build()
        navController.navigate(BsdfUserMoreOptionsDirections.actionBsdfUserMoreOptionsToDfAdminUserDeletionConfirmation(user), navOptions)
    }



    fun navigateToAdminManageFacultiesScreen() {
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageFaculties())
    }

    fun navigateToAdminManageFacultiesMoreOptionsDialog(faculty: Faculty) {
        if(currentDestinationId == R.id.bsdfManageFacultiesMoreOptions) return
        navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToBsdfManageFacultiesMoreOptions(faculty))
    }

    fun navigateToAdminAddEditFaculty(faculty: Faculty? = null) {
        if(currentDestinationId == R.id.bsdfManageFacultiesMoreOptions) popBackStack()
        navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToFragmentAdminAddEditFaculties(faculty))
    }

    fun navigateToAdminFacultyDeletionConfirmation(faculty: Faculty) {
        if(currentDestinationId == R.id.dfAdminFacultyDeletionConfirmation) return
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageFaculties, false).build()
        navController.navigate(BsdfManageFacultiesMoreOptionsDirections.actionBsdfManageFacultiesMoreOptionsToDfAdminFacultyDeletionConfirmation(faculty), navOptions)
    }



    fun navigateToAdminManageCourseOfStudiesScreen() {
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageCourseOfStudies())
    }

    fun navigateToAdminManageCoursesOfStudiesMoreOptionsDialog(courseOfStudies: CourseOfStudies) {
        if(currentDestinationId == R.id.bsdfManageCourseOfStudiesMoreOptions) return
        navController.navigate(FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToBsdfManageCourseOfStudiesMoreOptions(courseOfStudies))
    }

    fun navigateToAdminAddEditCourseOfStudies(courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties? = null){
        if(currentDestinationId == R.id.bsdfManageCourseOfStudiesMoreOptions) popBackStack()
        navController.navigate(
            FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToFragmentAdminAddEditCourseOfStudies(courseOfStudiesWithFaculties)
        )
    }

    fun navigateToAdminCourseOfStudiesDeletionConfirmation(courseOfStudies: CourseOfStudies) {
        if(currentDestinationId == R.id.dfAdminCourseOfStudiesDeletionConfirmation) return
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageCourseOfStudies, false).build()
        navController.navigate(
            BsdfManageCourseOfStudiesMoreOptionsDirections.actionBsdfManageCourseOfStudiesMoreOptionsToDfAdminCourseOfStudiesDeletionConfirmation(courseOfStudies),
            navOptions
        )
    }



    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }
}