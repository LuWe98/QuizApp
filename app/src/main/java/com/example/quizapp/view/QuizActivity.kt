package com.example.quizapp.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.setupWithNavController
import com.example.quizapp.R
import com.example.quizapp.databinding.ActivityQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmMain
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

//TODO -> SETTINGS ÜBERARBEITEN MIT CUSTOM VIEWS STATT RECYCLERVIEW !!
//TODO ---> CHANGE USERNAME AND PASSWORD IMPLEMENTIEREN
//TODO -> SHARE QUESTIONNAIRE VLLT MIT LISTE IN EINEM FRAGEBOGEN ÜBERARBEITEN, in der Liste stehen alle User mit denen der geteilt wurde, man kann den dann auch bearbeiten
//TODO -> Faculty, CourseOfStudies und Subject implementieren | Als ein embedded Document in dem Questionnaire
//TODO ---> Verknüpfung von Faculty, courseOfStudies und Subject
//TODO ----> Faculty hat ne liste von CourseOfStudies und das wiederrum ne liste von Subjects ?
//TODO ----> Oder einfach über ID verknüfen, aber als eine Liste von IDS, dass ein Fach z.B. in WIB und WNB sein kann
//TODO -> Kennzeichnung bei eigenen Fragebögen ob der public ist oder private

@AndroidEntryPoint
class QuizActivity : BindingActivity<ActivityQuizBinding>(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var navigatorProvider: Provider<Navigator>
    private val navigator get() = navigatorProvider.get()!!

    private val vmMain : VmMain by viewModels()

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base?.setLocale(Locale.ENGLISH))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
        navigator.addOnDestinationChangedListener(this)
    }

    private fun initViews() {
        binding.apply {
            bottomNavView.setupWithNavController(navigator.navController)

            cardHome.onClick {
                if(bottomNavView.selectedItemId == R.id.fragmentSettings){
                    navigator.popBackStack()
                } else {
                    bottomNavView.selectedItemId = R.id.fragmentHome
                }
            }

            cardSettings.onClick {
                bottomNavView.selectedItemId = R.id.fragmentSettings
            }

            cardSearch.onClick {
                navigator.navigateToSearchScreen()
//               bottomNavView.selectedItemId = R.id.fragmentSearch
            }

            addCard.onClick {
//                navigator.navigateToBackdropFragment()

                navigator.navigateToAddQuestionnaireScreen()
            }
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        currentSnackBar?.let {
            it.dismiss()
            currentSnackBar = null
        }

        when (destination.id) {
            R.id.fragmentQuizOverview, R.id.fragmentQuizContainer, R.id.fragmentAddQuestionnaire, R.id.fragmentAddQuestion, R.id.fragmentAuth, R.id.fragmentSearch-> {
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
            else -> {
                changeBottomAppBarVisibility(false)
            }

//            R.id.fragmentSearch -> {
//                binding.apply {
//                    changeCustomBottomNavBarVisibility(cardSearch, ivSearch)
//                }
//                changeBottomAppBarVisibility(true)
//            }
        }
    }

    private fun changeCustomBottomNavBarVisibility(cardToShow: MaterialCardView, imageViewToChangeTintOf: ImageView) {
        binding.apply {
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
        if(show){
            binding.bottomAppBar.performShow()
            binding.bottomAppBar.isVisible = true
        } else {
            binding.bottomAppBar.performHide()
        }

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