package com.example.quizapp.view

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.quizapp.R
import com.example.quizapp.databinding.ActivityQuizBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.navController
import com.example.quizapp.extensions.showSnackBar
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher
import com.example.quizapp.viewmodel.VmQuizActivity
import com.example.quizapp.viewmodel.VmQuizActivity.MainViewModelEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

//TODO -> SHARE QUESTIONNAIRE VLLT MIT LISTE IN EINEM FRAGEBOGEN ÜBERARBEITEN, in der Liste stehen alle User mit denen der geteilt wurde, man kann den dann auch bearbeiten
//TODO -> THEME COLORS
//TODO -> RVI_LAYOUTS
//TODO -> RAW QUERY FÜR MAINSCREEN ANSCHAUEN

@AndroidEntryPoint
class QuizActivity : BindingActivity<ActivityQuizBinding>(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    @Inject
    lateinit var fragmentResultDispatcher: FragmentResultDispatcher

    private val vmQuizActivity: VmQuizActivity by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        playFadeInAnim(savedInstanceState)

        navController.addOnDestinationChangedListener(this)
        registerObservers()
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        currentSnackBar?.let {
            if(navigationDispatcher.isLastDestinationDialog) return@let
            it.dismiss()
            currentSnackBar = null
        }
    }

    private fun registerObservers() {
        vmQuizActivity.userFlow.collectWhenStarted(this) {
            vmQuizActivity.onUserDataChanged(it, navController.currentDestination?.id)
        }

        navigationDispatcher.navigationChannelFlow.collectWhenStarted(this) { navEvent ->
            navEvent.execute(this)
        }

        fragmentResultDispatcher.fragmentResultChannelFlow.collectWhenStarted(this) { fragmentResultEvent ->
            fragmentResultEvent.execute(this)
        }

        vmQuizActivity.eventChannelFlow.collectWhenStarted(this) { event ->
            when (event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
            }
        }
    }


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