package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmSettings @Inject constructor(
    private val applicationScope : CoroutineScope,
    private val preferencesRepository: PreferencesRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val fragmentSettingsEventChannel = Channel<FragmentSettingsEvent>()

    val fragmentSettingsEventChannelFlow = fragmentSettingsEventChannel.receiveAsFlow()

    val userInfoFlow get() = preferencesRepository.userFlow.asLiveData().distinctUntilChanged()

    val userInfo get() = preferencesRepository.user

    fun onLogoutClicked() {
        launch(IO) {
            fragmentSettingsEventChannel.send(OnLogoutClickedEvent)
        }
    }

     fun onGoToAdminPageClicked(){
         launch(IO) {
             fragmentSettingsEventChannel.send(NavigateToAdminScreen)
         }
     }


    fun onLogoutConfirmed(){
        applicationScope.launch(IO) {
            preferencesRepository.resetPreferenceData()
            fragmentSettingsEventChannel.send(NavigateToLoginScreen)

            //TODO --> IDS of Delted Questionnaires müssen noch weiterhin bestehen bleiben, dass sie beim nächsten start gelöscht werden können.
            localRepository.deleteAllData()
        }
    }

    sealed class FragmentSettingsEvent{
        object OnLogoutClickedEvent : FragmentSettingsEvent()
        object NavigateToLoginScreen : FragmentSettingsEvent()
        object NavigateToAdminScreen : FragmentSettingsEvent()
    }
}