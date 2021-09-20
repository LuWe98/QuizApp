package com.example.quizapp.ui

import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import com.example.quizapp.MainNavGraphDirections
import com.example.quizapp.R
import com.example.quizapp.extensions.initMaterialElevationScale
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.ui.fragments.addquestionnairescreen.FragmentAddQuestionDirections
import com.example.quizapp.ui.fragments.addquestionnairescreen.FragmentAddQuestionnaireDirections
import com.example.quizapp.ui.fragments.homescreen.FragmentHomeDirections
import com.example.quizapp.ui.fragments.quizscreen.FragmentQuizOverviewDirections
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Navigator @Inject constructor(private val navHostFragmentWeakReference: WeakReference<NavHostFragment>) {

    private val naHostFragment get() = navHostFragmentWeakReference.get()!!
    val navController get() = naHostFragment.navController

    val currentFragment: Fragment get() = naHostFragment.childFragmentManager.fragments.first()
    val currentDestination get() = navController.currentDestination
    val currentDestinationId get() = currentDestination?.id
    val currentBackStackEntry get() = navController.currentBackStackEntry
    val currentSaveStateHandle get() = currentBackStackEntry!!.savedStateHandle
    val previousBackStackEntry get() = navController.previousBackStackEntry
    val previousSaveStateHandle get() = previousBackStackEntry!!.savedStateHandle


    fun addOnDestinationChangedListener(listener : NavController.OnDestinationChangedListener){
        navController.addOnDestinationChangedListener(listener)
    }

    fun navigate(@IdRes id: Int){ navController.navigate(id) }

    fun popBackStack(){ navController.popBackStack() }


    fun navigateToAddQuestionnaireScreen(questionnaireId: Long = NO_QUESTIONNAIRE_ID){
        navController.navigate(MainNavGraphDirections.actionGlobalAddNavGraph(questionnaireId))
    }

    fun navigateToEditQuestionScreen(questionPosition: Int, questionWithAnswers: QuestionWithAnswers){
        navController.navigate(FragmentAddQuestionnaireDirections.actionFragmentAddQuestionnaireToFragmentAddQuestion(questionPosition, questionWithAnswers))
    }

    fun navigateToQuizScreen(questionnaireId: Long){
        navController.navigate(FragmentHomeDirections.actionFragmentHomeToFragmentQuizOverview(questionnaireId))
    }

    fun navigateToQuizContainerScreen(questionPosition : Int = FIRST_QUESTION_POSITION){
        navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition))
    }

    fun navigateToQuizContainerScreenWithQuestionCardClick(questionPosition : Int, questionId : Long, clickedCard : CardView){
        currentFragment.apply {
            initMaterialElevationScale()
            clickedCard.transitionName = questionId.toString()
            val extras = FragmentNavigatorExtras(clickedCard to getString(R.string.questionClickedTransitionName))
            navController.navigate(FragmentQuizOverviewDirections.actionFragmentQuizOverviewToFragmentQuizContainer(questionPosition), extras)
        }
    }


    fun navigateToLogin(){
        navController.navigate(FragmentHomeDirections.actionFragmentHomeToFragmentLogin())
    }

    fun navigateToSearchScreen() {
        navController.navigate(FragmentHomeDirections.actionFragmentHomeToFragmentSearch())
    }


    companion object {
        const val NO_QUESTIONNAIRE_ID = 0L
        const val FIRST_QUESTION_POSITION = 0
    }
}