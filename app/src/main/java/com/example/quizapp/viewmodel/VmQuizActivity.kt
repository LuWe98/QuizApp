package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.viewmodel.VmQuizActivity.*
import com.example.quizapp.viewmodel.VmQuizActivity.MainViewModelEvent.ShowMessageSnackBar
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VmQuizActivity @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val ktorClientAuth: KtorClientAuth,
    private val state: SavedStateHandle
) : BaseViewModel<MainViewModelEvent>() {

    val userFlow = preferencesRepository.userFlow
        .flowOn(IO)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    private var manualLogoutFlag = state.get<Boolean>(MANUAL_LOGOUT_FLAG_KEY) ?: false
        set(value) {
            state.set(MANUAL_LOGOUT_FLAG_KEY, value)
            field = value
        }

    fun onLogoutConfirmed() = launch(IO) {
        manualLogoutFlag = true
        preferencesRepository.clearPreferenceDataOnLogout()
    }

    fun onUserDataChanged(user: User?, currentDestinationId: Int?) = launch(IO) {
        user?.let {
            if (user.isEmpty && (currentDestinationId ?: 0) != R.id.fragmentAuth) {
                ktorClientAuth.resetJwtAuth()
                navigationDispatcher.dispatch(ToAuthScreen)
                eventChannel.send(ShowMessageSnackBar(if (manualLogoutFlag) R.string.loggedOut else R.string.errorLoggedOutBecauseCredentialsChanged))
                manualLogoutFlag = false
            }
        }
    }

    sealed class MainViewModelEvent: ViewModelEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : MainViewModelEvent()
    }

    companion object {
        private const val MANUAL_LOGOUT_FLAG_KEY = "manualLogoutFlagKey"
    }
}