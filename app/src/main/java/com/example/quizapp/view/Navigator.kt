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
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.view.fragments.addeditquestionnairescreen.FragmentAddEditQuestionnaireDirections
import com.example.quizapp.view.fragments.adminscreen.BsdfUserMoreOptionsDirections
import com.example.quizapp.view.fragments.adminscreen.FragmentAdminDirections
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

    fun navigateToAdminPage(){
        navController.navigate(FragmentSettingsDirections.actionFragmentSettingsToFragmentAdmin())
    }

    fun navigateToQuestionnaireMoreOptions(questionnaire: Questionnaire){
        navController.navigate(MainNavGraphDirections.actionGlobalBsdfQuestionnaireMoreOptions(questionnaire))
    }

    fun navigateToUserMoreOptions(user: User){
        navController.navigate(FragmentAdminDirections.actionFragmentAdminToBsdfUserMoreOptions(user))
    }

    fun navigateToChangeUserRoleDialog(user: User){
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.fragmentAdmin, false).build()
        navController.navigate(BsdfUserMoreOptionsDirections.actionBsdfUserMoreOptionsToBsdfChangeUserRole(user), navOptions)
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

    fun navigateToUpdateStringValueDialog(initialValue: String, updateStringValueType: DfUpdateStringValueType) {
        if(currentDestinationId == R.id.dfUpdateStringValue) return
        navController.navigate(MainNavGraphDirections.actionGlobalDfUpdateStringValue(updateStringValueType, initialValue))
    }


    companion object {
        const val FIRST_QUESTION_POSITION = 0
    }
}