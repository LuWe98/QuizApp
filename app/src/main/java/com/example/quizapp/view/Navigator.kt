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
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
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
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(
    private val navHostWeakReference: WeakReference<NavHostFragment>
) {

    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }

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

    //TODO -> Soll auf homescreen gepoppt werden oder nicht ?
    fun navigateToQuizScreen(questionnaireId: String) {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentHome, false).build()
        navController.navigate(MainNavGraphDirections.actionGlobalGoToQuizScreen(questionnaireId), navOptions)
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

    fun navigateToLocalQuestionnaireFilterSelection(){
        if(currentDestinationId == R.id.bsdfLocalQuestionnaireFilterSelection) return
        navController.navigate(FragmentHomeDirections.actionGlobalBsdfLocalQuestionnaireFilterSelection())
    }


    fun navigateToSearchScreen() {
        navController.navigate(MainNavGraphDirections.actionGlobalFragmentSearch())
    }

    fun navigateToQuestionnaireFilterDialog(
        selectedCosIds: Array<String>,
        selectedFacultyIds: Array<String>,
        selectedAuthors: Array<AuthorInfo>
    ) {
        if(currentDestinationId == R.id.bsdfBrowseQuestionnaireFilterSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfBrowseQuestionnaireFilterSelection(selectedCosIds, selectedFacultyIds, selectedAuthors))
    }

    fun navigateToQuestionnaireMoreOptions(questionnaire: Questionnaire){
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
    }


    fun navigateToShareQuestionnaireDialog(questionnaireId: String) {
        navController.navigate(MainNavGraphDirections.actionGlobalDfShareQuestionnaire(questionnaireId))
    }


    fun navigateToAddEditQuestionnaireScreen(completeQuestionnaire: CompleteQuestionnaire? = null, copy: Boolean = false) {
        navController.navigate(MainNavGraphDirections.actionGlobalAddEditQuestionnaireNavGraph(completeQuestionnaire, copy))
    }

    fun navigateToAddEditQuestionScreen(questionPosition: Int, questionWithAnswers: QuestionWithAnswers? = null) {
        navController.navigate(FragmentAddEditQuestionnaireDirections.actionFragmentAddEditQuestionnaireToFragmentAddEditQuestion(questionPosition, questionWithAnswers))
    }


    fun navigateToCourseOfStudiesSelection(selectedCourseOfStudiesIds: Array<String>){
        if(currentDestinationId == R.id.bsdfCourseOfStudiesSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfCourseOfStudiesSelection(selectedCourseOfStudiesIds))
    }

    fun navigateToFacultySelection(selectedFacultyIds: Array<String>) {
        if(currentDestinationId == R.id.bsdfFacultySelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfFacultySelection(selectedFacultyIds))
    }

    fun navigateToRemoteAuthorSelection(selectedAuthors: Array<AuthorInfo>) {
        if(currentDestinationId == R.id.bsdfRemoteAuthorSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfRemoteAuthorSelection(selectedAuthors))
    }

    fun navigateToLocalAuthorSelection(selectedAuthorIds: Array<String>) {
        if(currentDestinationId == R.id.bsdfLocalAuthorSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfLocalAuthorSelection(selectedAuthorIds))
    }

    fun navigateToUpdateStringDialog(initialValue: String, updateStringType: UpdateStringType) {
        if(currentDestinationId == R.id.dfUpdateStringValue) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(updateStringType, initialValue))
    }

    fun navigateToSelectionDialog(selectionType: SelectionType) {
        if(currentDestinationId == R.id.bsdfSelection) return
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfSelection(selectionType))
    }

    fun navigateToConfirmationDialog(confirmationType: ConfirmationType) {
        if(currentDestinationId == R.id.dfConfirmation) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfConfirmation(confirmationType))
    }





    //ADMIN SCREENS
    fun navigateToAdminManageUsersScreen(){
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageUsers())
    }

    fun navigateToChangeUserRoleDialog(user: User){
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdminManageUsers, false).build()
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfUserRoleChange(user), navOptions)
    }

    fun navigateToAdminAddEditUser(user: User? = null) {
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToFragmentAdminAddEditUser(user))
    }

    fun navigateToAdminManageUsersFilterSelection(selectedRoles: Array<Role>){
        if(currentDestinationId == R.id.bsdfManageUsersFilterSelection) return
        navController.navigate(FragmentAdminManageUsersDirections.actionFragmentAdminManageUsersToBsdfManageUsersFilterSelection(selectedRoles))
    }




    fun navigateToAdminManageFacultiesScreen() {
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageFaculties())
    }

    fun navigateToAdminAddEditFaculty(faculty: Faculty? = null) {
        navController.navigate(FragmentAdminManageFacultiesDirections.actionFragmentAdminManageFacultiesToFragmentAdminAddEditFaculties(faculty))
    }




    fun navigateToAdminManageCourseOfStudiesScreen() {
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdminManageCourseOfStudies())
    }

    fun navigateToAdminAddEditCourseOfStudies(courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties? = null){
        navController.navigate(FragmentAdminManageCourseOfStudiesDirections.actionFragmentAdminManageCourseOfStudiesToFragmentAdminAddEditCourseOfStudies(courseOfStudiesWithFaculties))
    }



    fun navigateToLoadingDialog(@StringRes messageRes: Int){
        if(currentDestinationId == R.id.dfLoading) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfLoading(messageRes))
    }

    fun popLoadingDialog(){
        if(navController.backQueue[navController.backQueue.size -1].destination.id == R.id.dfLoading) {
            navController.popBackStack()
        }
    }
}