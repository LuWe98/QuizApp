package com.example.quizapp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.setupWithNavController
import com.example.quizapp.R
import com.example.quizapp.databinding.ActivityMainBinding
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmMain
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
        navigator.addOnDestinationChangedAction(this)
    }

    private fun initViews(){
        binding.apply {
            bottomNavView.apply {
                setupWithNavController(navigator.navController)
            }

            fab.setOnClickListener {
                navigator.navigateToAddQuestionnaireScreen()
            }
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when(destination.id){
            R.id.fragmentQuizOverview, R.id.fragmentQuizContainer, R.id.fragmentAddQuestionnaire, R.id.fragmentAddQuestion -> {
                binding.bottomAppBar.performHide()
                changeBottomAppBarVisibility(false)
            }
            R.id.fragmentHome, R.id.fragmentSettings, R.id.fragmentSearch -> {
                binding.bottomAppBar.performShow()
                changeBottomAppBarVisibility(true)
            }
        }
    }

    private fun changeBottomAppBarVisibility(show : Boolean){
        binding.bottomAppBar.animate().setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if(!show) {
                    binding.bottomAppBar.isVisible = show
                } else {
                    binding.fab.show()
                }
            }

            override fun onAnimationStart(animation: Animator?) {
                if(show) {
                    binding.bottomAppBar.isVisible = show
                } else {
                    binding.fab.hide()
                }
            }
        })
    }
}