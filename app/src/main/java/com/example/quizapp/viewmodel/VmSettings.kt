package com.example.quizapp.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_CHANGED
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_UP_TO_DATE
import com.example.quizapp.model.mongodb.documents.user.User
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmSettings @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val fragmentSettingsEventChannel = Channel<FragmentSettingsEvent>()

    val fragmentSettingsEventChannelFlow = fragmentSettingsEventChannel.receiveAsFlow()

    private val userFlow = preferencesRepository.userFlow.flowOn(IO).distinctUntilChanged()

    val userRoleFlow = userFlow.map { it.role }.asLiveData().distinctUntilChanged()

    val userNameFlow = userFlow.map { it.userName }.asLiveData().distinctUntilChanged()

    val themeNameResFlow = preferencesRepository.themeFlow.map {
        when (it) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.string.light
            AppCompatDelegate.MODE_NIGHT_YES -> R.string.dark
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.string.systemDefault
            else -> throw IllegalStateException()
        }
    }.distinctUntilChanged()

    val languageFlow = preferencesRepository.languageFlow.distinctUntilChanged()


    fun onLogoutClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(OnLogoutClickedEvent)
    }

    fun onGoToAdminPageClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(NavigateToAdminScreen)
    }

    fun onRefreshListenerTriggered() = applicationScope.launch(IO) {
        val user = preferencesRepository.user

        runCatching {
            backendRepository.syncUserData(user.id)
        }.onFailure {
            fragmentSettingsEventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotSyncUserData))
        }.onSuccess { response ->
            when (response.responseType) {
                DATA_UP_TO_DATE -> {
                    fragmentSettingsEventChannel.send(ShowMessageSnackBarEvent(R.string.userDataUpToDate))
                }
                DATA_CHANGED -> {
                    fragmentSettingsEventChannel.send(ShowMessageSnackBarEvent(R.string.userDataUpdated))
                    User(
                        id = user.id,
                        userName = user.userName,
                        password = user.password,
                        role = response.role!!,
                        lastModifiedTimestamp = response.lastModifiedTimestamp!!
                    ).let { user ->
                        preferencesRepository.updateUserCredentials(user)
                    }
                }
            }
        }
    }


    fun onLogoutConfirmed() = launch(IO) {
        preferencesRepository.clearPreferenceData()
    }

    sealed class FragmentSettingsEvent {
        object OnLogoutClickedEvent : FragmentSettingsEvent()
        object NavigateToLoginScreen : FragmentSettingsEvent()
        object NavigateToAdminScreen : FragmentSettingsEvent()
        class ShowMessageSnackBarEvent(val messageRes: Int) : FragmentSettingsEvent()
    }
}