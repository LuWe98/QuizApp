package com.example.quizapp.view

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.quizapp.R
import com.example.quizapp.databinding.ActivityQuizBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.utils.BindingUtils.getBinding
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.view.dispatcher.DispatchEventQueueContainer
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher
import com.example.quizapp.viewmodel.VmMainActivity
import com.example.quizapp.viewmodel.VmMainActivity.MainViewModelEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class QuizActivity : BindingActivity<ActivityQuizBinding>(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var eventQueue: DispatchEventQueueContainer

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher


    private val vmQuizActivity: VmMainActivity by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSplashScreen(savedInstanceState)
        initViews(savedInstanceState)
    }

    private fun initSplashScreen(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            setTheme(R.style.Theme_QuizApp)
            return
        }

        installSplashScreen().apply {
            setKeepVisibleCondition {
                vmQuizActivity.showSplashScreen
            }
        }
    }

    private fun initViews(savedInstanceState: Bundle?) {
        binding = getBinding(this).apply {
            setContentView(root)
        }
        playFadeInAnim(savedInstanceState)
        navController.addOnDestinationChangedListener(this)
        registerObservers()
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        currentSnackBar?.let {
            if (navigationDispatcher.isLastDestinationDialog) return@let
            it.dismiss()
            currentSnackBar = null
        }
    }

    private fun registerObservers() {
        vmQuizActivity.userFlow.collectWhenStarted(this) {
            vmQuizActivity.onUserDataChanged(it, navController.currentDestination?.id)
        }

        eventQueue.eventChannelFlow.collectWhenStarted(this) { event ->
            event.execute(this)
        }

        vmQuizActivity.eventChannelFlow.collectWhenStarted(this) { event ->
            when (event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }

        vmQuizActivity.locallyPresentAuthorsFlow.collectWhenStarted(this) {
            vmQuizActivity.onLocallyPresentAuthorsChanged(it)
        }

        vmQuizActivity.locallyPresentFacultiesFlow.collectWhenStarted(this) {
            vmQuizActivity.onLocallyPresentFacultiesChanges(it)
        }

        vmQuizActivity.locallyPresentCoursesOfStudiesFlow.collectWhenStarted(this) {
            vmQuizActivity.onLocallyPresentCoursesOfStudiesChanged(it)
        }
    }

    fun logoutUser() = vmQuizActivity.onLogoutConfirmed()


    override fun recreate() = playFadeOutAnim()

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

    private fun getHexColor(progress: Float) = Integer.toHexString((255 * progress).toInt()).let {
        "#" + (if (it.length == 1) "0$it" else it) + "000000"
    }.let(Color::parseColor)
}