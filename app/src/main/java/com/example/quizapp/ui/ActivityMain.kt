package com.example.quizapp.ui

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
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmMain
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class ActivityMain : BindingActivity<ActivityMainBinding>(), NavController.OnDestinationChangedListener {

    @Inject lateinit var navigatorProvider: Provider<Navigator>
    private val navigator get() = navigatorProvider.get()!!

    private val viewModel : VmMain by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(viewModel.currentTheme)
        setContentView(binding.root)

        initViews()
        navigator.addOnDestinationChangedListener(this)
    }

    private fun initViews(){
        binding.apply {
            bottomNavView.apply {
                setupWithNavController(navigator.navController)
            }

            homeCard.setOnClickListener {
//                launch {
//                    log("${viewModel.registerUser("Hallo@gmx.de", "Fisch123", "1234")}")
//                }

                bottomNavView.selectedItemId = R.id.fragmentHome
            }

            searchCard.setOnClickListener {
                bottomNavView.selectedItemId = R.id.fragmentSearch
            }

            addCard.setOnClickListener {
                navigator.navigateToAddQuestionnaireScreen()
            }

            settingsCard.setOnClickListener {
                bottomNavView.selectedItemId = R.id.fragmentSettings
            }
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when(destination.id){
            R.id.fragmentQuizOverview, R.id.fragmentQuizContainer, R.id.fragmentAddQuestionnaire, R.id.fragmentAddQuestion, R.id.fragmentLogin, R.id.fragmentSearch -> {
                binding.bottomAppBar.performHide()
                changeBottomAppBarVisibility(false)
            }
            R.id.fragmentHome -> {
                binding.apply {
                    changeCustomBottomNavBarVisibility(homeCard, homeIcon)
                }
                changeBottomAppBarVisibility(true)
            }
            R.id.fragmentSettings -> {
                binding.apply {
                    changeCustomBottomNavBarVisibility(settingsCard, settingsIcon)
                }
                changeBottomAppBarVisibility(true)
            }
        }
    }

    private fun changeCustomBottomNavBarVisibility(cardToShow: MaterialCardView, imageViewToChangeTintOf : ImageView){
        binding.apply {
            bottomAppBar.performShow()

            homeCard.setCardBackgroundColor(getColor(R.color.transparent))
            homeIcon.setDrawableTintWithRes(R.color.black)
            searchCard.setCardBackgroundColor(getColor(R.color.transparent))
            searchIcon.setDrawableTintWithRes(R.color.black)
            settingsCard.setCardBackgroundColor(getColor(R.color.transparent))
            settingsIcon.setDrawableTintWithRes(R.color.black)
        }

        cardToShow.setCardBackgroundColor(getThemeColor(R.attr.colorAccent))
        imageViewToChangeTintOf.setDrawableTintWithRes(R.color.white)
    }


    private fun changeBottomAppBarVisibility(show : Boolean){
        binding.bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if(!show) {
                    binding.bottomAppBar.isVisible = show
                } else {
                    //binding.fab.show()
                }
            }

            override fun onAnimationStart(animation: Animator?) {
                if(show) {
                    binding.bottomAppBar.isVisible = show
                } else {
                    //binding.fab.hide()
                }
            }
        })
    }
}