package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmSettings @Inject constructor(
    val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val fragmentSettingsEventChannel = Channel<FragmentSettingsEvent>()

    val fragmentSettingsEventChannelFlow = fragmentSettingsEventChannel.receiveAsFlow()

    fun onLogoutClicked() {
        launch {
            preferencesRepository.updateUserEmail("")
            preferencesRepository.updateUserPassword("")
            fragmentSettingsEventChannel.send(FragmentSettingsEvent.NavigateToLoginScreen)
        }
    }

    sealed class FragmentSettingsEvent{
        object NavigateToLoginScreen : FragmentSettingsEvent()
    }
}