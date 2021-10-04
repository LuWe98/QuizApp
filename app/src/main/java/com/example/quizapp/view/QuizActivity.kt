package com.example.quizapp.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.setupWithNavController
import com.example.quizapp.R
import com.example.quizapp.databinding.ActivityMainBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmMain
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class QuizActivity : BindingActivity<ActivityMainBinding>(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var navigatorProvider: Provider<Navigator>
    private val navigator get() = navigatorProvider.get()!!

    private val viewModel: VmMain by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(viewModel.currentTheme)
        setContentView(binding.root)

        initViews()
        navigator.addOnDestinationChangedListener(this)
    }


    private fun initViews() {
        binding.apply {
            bottomNavView.setupWithNavController(navigator.navController)

            cardHome.setOnClickListener {
                if(bottomNavView.selectedItemId == R.id.fragmentSettings){
                    navigator.popBackStack()
                } else {
                    bottomNavView.selectedItemId = R.id.fragmentHome
                }
            }

            cardSettings.setOnClickListener {
                bottomNavView.selectedItemId = R.id.fragmentSettings
            }

            cardSearch.setOnClickListener {
                bottomNavView.selectedItemId = R.id.fragmentSearch
            }

            addCard.setOnClickListener {
//                navigator.navigateToAddQuestionnaireScreen()

//                launch {
//                    val mongoQuestionnaires = viewModel.getQuestionnairesOfUser()
//                    val qwa = mongoQuestionnaires.map { MongoMapper.mapMongoObjectToSqlEntities(it) }
//                    val remappedMongoQuestionnaires = qwa.map { MongoMapper.mapSqlEntitiesToMongoObject(it) }
//                    log("IS SAME? ${mongoQuestionnaires == remappedMongoQuestionnaires}")
//                }
            }
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when (destination.id) {
            R.id.fragmentQuizOverview, R.id.fragmentQuizContainer, R.id.fragmentAddQuestionnaire, R.id.fragmentAddQuestion, R.id.fragmentAuth, R.id.fragmentSearch -> {
                binding.bottomAppBar.performHide()
                changeBottomAppBarVisibility(false)
            }
            R.id.fragmentHome -> {
                binding.apply {
                    changeCustomBottomNavBarVisibility(cardHome, ivHome)
                }
                changeBottomAppBarVisibility(true)
            }
            R.id.fragmentSettings -> {
                binding.apply {
                    changeCustomBottomNavBarVisibility(cardSettings, ivSettings)
                }
                changeBottomAppBarVisibility(true)
            }
        }
    }

    private fun changeCustomBottomNavBarVisibility(cardToShow: MaterialCardView, imageViewToChangeTintOf: ImageView) {
        binding.apply {
            bottomAppBar.performShow()

            cardHome.setCardBackgroundColor(getColor(R.color.transparent))
            ivHome.setDrawableTintWithRes(R.color.black)
            cardSearch.setCardBackgroundColor(getColor(R.color.transparent))
            ivSearch.setDrawableTintWithRes(R.color.black)
            cardSettings.setCardBackgroundColor(getColor(R.color.transparent))
            ivSettings.setDrawableTintWithRes(R.color.black)
        }

        cardToShow.setCardBackgroundColor(getThemeColor(R.attr.colorPrimary))
        imageViewToChangeTintOf.setDrawableTintWithRes(R.color.white)
    }


    private fun changeBottomAppBarVisibility(show: Boolean) {
        binding.bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (!show) {
                    binding.bottomAppBar.isVisible = show
                } else {
                    //binding.fab.show()
                }
            }

            override fun onAnimationStart(animation: Animator?) {
                if (show) {
                    binding.bottomAppBar.isVisible = show
                } else {
                    //binding.fab.hide()
                }
            }
        })
    }
}