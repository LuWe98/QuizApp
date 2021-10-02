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
import com.example.quizapp.extensions.getColor
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.extensions.setDrawableTintWithRes
import com.example.quizapp.view.fragments.bindingfragmentsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmMain
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class ActivityMain : BindingActivity<ActivityMainBinding>(), NavController.OnDestinationChangedListener {

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
                bottomNavView.selectedItemId = R.id.fragmentHome
            }

            cardSettings.setOnClickListener {
                bottomNavView.selectedItemId = R.id.fragmentSettings
            }

            cardSearch.setOnClickListener {
                bottomNavView.selectedItemId = R.id.fragmentSearch
            }

            addCard.setOnClickListener {
                navigator.navigateToAddQuestionnaireScreen()
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



//        result = realmRepo.getQuestionnaireResultsWith("6155d3654e300b57025e3bff")
//        result.addChangeListener { t, changeSet ->
//            log("${t.asJSON()}")
//        }


//            val answers1 = MongoAnswer(answerText = "Answer 1")
//            val answers2 = MongoAnswer(answerText = "Answer 2")
//            val answers3 = MongoAnswer(answerText = "Answer 3")
//            val question = MongoQuestion(id = "6155d8508b1b2b6a1bd585bb", questionText = "Question 1", answers = RealmList(answers1, answers2, answers3))
//            realmRepo.insertTest(MongoQuestionnaire(id = "6155d3654e300b57025e3bff", title = "HANS Peter", questions = RealmList(question)))

//            launchForBackgroundRealm {
////                    val a = realmRepo.deleteQuestionnaireWithId("6155d3654e300b57025e3bff")
//                val a = realmRepo.deleteQuestionWith("6155d8508b1b2b6a1bd585bb")
//                log("HALLO: $a")
//            }
////                launch {
////                    try {
////                        val response = viewModel.getTodoKtor(43)
////                        log("Response: $response")
////                    } catch (e: Exception) {
////                        log("EXCEPTION : $e")
////                    }
////                }

//                launchForBackgroundRealm {
////                    val a = realmRepo.getQuestionnaireWithId("6155d3654e300b57025e3bff")
//                    val a = realmRepo.getQuestionWithId("6155d8508b1b2b6a1bd585bb")
//                    log("HALLO: $a")
//                }//            val answers1 = MongoAnswer(answerText = "Answer 1")
////            val answers2 = MongoAnswer(answerText = "Answer 2")
////            val answers3 = MongoAnswer(answerText = "Answer 3")
////            val question = MongoQuestion(id = "6155d8508b1b2b6a1bd585bb", questionText = "Question 1", answers = RealmList(answers1, answers2, answers3))
////            realmRepo.insertTest(MongoQuestionnaire(id = "6155d3654e300b57025e3bff", title = "HANS Peter", questions = RealmList(question)))
//
////            launchForBackgroundRealm {
//////                    val a = realmRepo.deleteQuestionnaireWithId("6155d3654e300b57025e3bff")
////                val a = realmRepo.deleteQuestionWith("6155d8508b1b2b6a1bd585bb")
////                log("HALLO: $a")
////            }
//////                launch {
//////                    try {
//////                        val response = viewModel.getTodoKtor(43)
//////                        log("Response: $response")
//////                    } catch (e: Exception) {
//////                        log("EXCEPTION : $e")
//////                    }
//////                }
//
//            //                launchForBackgroundRealm {
//////                    val a = realmRepo.getQuestionnaireWithId("6155d3654e300b57025e3bff")
////                    val a = realmRepo.getQuestionWithId("6155d8508b1b2b6a1bd585bb")
////                    log("HALLO: $a")
////                }