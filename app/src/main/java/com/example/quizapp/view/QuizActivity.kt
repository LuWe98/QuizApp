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
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.extensions.showSnackBar
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.datastore.datawrappers.ManageUsersOrderBy
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.view.bindingsuperclasses.BindingActivity
import com.example.quizapp.viewmodel.VmQuizActivity
import com.example.quizapp.viewmodel.VmQuizActivity.MainViewModelEvent.NavigateToLoginScreenEvent
import com.example.quizapp.viewmodel.VmQuizActivity.MainViewModelEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max
import kotlin.math.min

//TODO -> CHANGE USERNAME AND PASSWORD IMPLEMENTIEREN
//TODO -> SHARE QUESTIONNAIRE VLLT MIT LISTE IN EINEM FRAGEBOGEN ÜBERARBEITEN, in der Liste stehen alle User mit denen der geteilt wurde, man kann den dann auch bearbeiten
//TODO -> THEME COLORS
//TODO -> RVI_LAYOUTS
//TODO -> RAW QUERY FÜR MAINSCREEN ANSCHAUEN
//TODO -> REINLADEN VON EXCEL DATEIEN IM ADD EDIT QUESTIONNAIRE SCREEN

@AndroidEntryPoint
class QuizActivity : BindingActivity<ActivityQuizBinding>(), NavController.OnDestinationChangedListener {

    @Inject
    lateinit var navigatorProvider: Provider<Navigator>
    private val navigator get() = navigatorProvider.get()!!

    private val vmQuizActivity: VmQuizActivity by viewModels()

    @Inject
    lateinit var backendRepository: BackendRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        playFadeInAnim(savedInstanceState)

        navigator.addOnDestinationChangedListener(this)
        registerObservers()

        launch(IO) {
            runCatching {
                backendRepository.getPagedUsersAdmin(
                    page = 1,
                    searchString = "",
                    roles = Role.values().toSet(),
                    orderBy = ManageUsersOrderBy.USER_NAME,
                    ascending = true
                )
            }.onFailure {
                log("Failure: $it")
            }.onSuccess {
                log("SUCCESS")
            }
        }
    }


    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        currentSnackBar?.let {
            it.dismiss()
            currentSnackBar = null
        }
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