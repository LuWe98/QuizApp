package com.example.quizapp.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
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
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmQuizActivity
import com.example.quizapp.viewmodel.VmQuizActivity.MainViewModelEvent.*
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max
import kotlin.math.min

//TODO -> SETTINGS ÜBERARBEITEN MIT CUSTOM VIEWS STATT RECYCLERVIEW !!
//TODO -> CHANGE USERNAME AND PASSWORD IMPLEMENTIEREN
//TODO -> SHARE QUESTIONNAIRE VLLT MIT LISTE IN EINEM FRAGEBOGEN ÜBERARBEITEN, in der Liste stehen alle User mit denen der geteilt wurde, man kann den dann auch bearbeiten

@AndroidEntryPoint
class QuizActivity : BindingActivity<ActivityQuizBinding>(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var navigatorProvider: Provider<Navigator>
    private val navigator get() = navigatorProvider.get()!!

    private val vmQuizActivity: VmQuizActivity by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        playFadeInAnim(savedInstanceState)

        initViews()
        navigator.addOnDestinationChangedListener(this)
        registerObservers()
    }

    private fun initViews() {
        binding.apply {
            bottomNavView.setupWithNavController(navigator.navController)

            cardHome.onClick {
                if (bottomNavView.selectedItemId == R.id.fragmentSettings) {
                    navigator.popBackStack()
                } else {
                    bottomNavView.selectedItemId = R.id.fragmentHome
                }
            }

            cardSettings.onClick {
//                bottomNavView.selectedItemId = R.id.fragmentSettings
                navigator.navigateToSettingsScreen()
            }

            cardSearch.onClick {
                navigator.navigateToSearchScreen()
//               bottomNavView.selectedItemId = R.id.fragmentSearch
            }

            addCard.onClick {
//                navigator.navigateToAddQuestionnaireScreen()
                navigator.navigateToAddEditQuestionnaireScreen()
            }

            addCard.onLongClick {
                navigator.navigateToFacultySelection(emptySet())
            }
        }
    }


    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        currentSnackBar?.let {
            it.dismiss()
            currentSnackBar = null
        }

        when (destination.id) {
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
        }
    }

    private fun changeCustomBottomNavBarVisibility(cardToShow: MaterialCardView, imageViewToChangeTintOf: ImageView) {
        binding.apply {
            cardHome.setCardBackgroundColor(getColor(R.color.transparent))
            ivHome.setDrawableTint(getThemeColor(R.attr.colorControlNormal))
            cardSearch.setCardBackgroundColor(getColor(R.color.transparent))
            ivSearch.setDrawableTint(getThemeColor(R.attr.colorControlNormal))
            cardSettings.setCardBackgroundColor(getColor(R.color.transparent))
            ivSettings.setDrawableTint(getThemeColor(R.attr.colorControlNormal))
        }

        cardToShow.setCardBackgroundColor(getThemeColor(R.attr.colorPrimary))
        imageViewToChangeTintOf.setDrawableTintWithRes(R.color.white)
    }


    private fun changeBottomAppBarVisibility(show: Boolean) {
        if (show) {
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

    private fun registerObservers() {
        vmQuizActivity.userFlow.collectWhenStarted(this) {
            vmQuizActivity.onUserDataChanged(it, navigator.currentDestinationId)
        }

        vmQuizActivity.mainViewModelEventChannelFlow.collectWhenStarted(this) { event ->
            when (event) {
                NavigateToLoginScreenEvent -> navigator.navigateToLoginScreen()
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }


    override fun recreate() {
        playFadeOutAnim()
    }

    //TODO -> Im Viewmodel flag setzen, dass anim spielen soll
    private fun playFadeInAnim(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return

        binding.transitionView.apply {
            animate()
                .setDuration(resources.getInteger(R.integer.recreateAnimDuration).toLong())
                .setUpdateListener {
                    getHexColor(1 - it.progress).let { color ->
                        window.statusBarColor = color
                        setBackgroundColor(color)
                    }
                }.withStartAction {
                    isVisible = true
                }.withEndAction {
                    isVisible = false
                }.start()
        }
    }

    //TODO -> Im Viewmodel flag setzen, dass anim spielen soll
    private fun playFadeOutAnim() {
        binding.transitionView.apply {
            animate()
                .setDuration(resources.getInteger(R.integer.recreateAnimDuration).toLong())
                .setUpdateListener {
                    getHexColor(it.progress).let { color ->
                        window.statusBarColor = color
                        setBackgroundColor(color)
                    }
                }.withStartAction {
                    isVisible = true
                }.withEndAction {
                    super.recreate()
                }.start()
        }
    }

    val ValueAnimator.progress get() = min(max(currentPlayTime / duration.toFloat(), 0f), 1f)

    private fun getHexColor(progress: Float): Int {
        return Integer.toHexString((255 * progress).toInt()).let {
            "#" + (if (it.length == 1) "0$it" else it) + "000000"
        }.let { hexColor ->
            Color.parseColor(hexColor)
        }
    }
}