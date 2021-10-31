package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmSettingsThemeSelection @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val applicationScope: CoroutineScope
) : ViewModel() {

    private val themeSelectionEventChannel = Channel<ThemeSelectionEvent>()

    val currentTheme = runBlocking { preferencesRepository.getTheme() }

    val themeSelectionEventChannelFlow = themeSelectionEventChannel.receiveAsFlow()

    fun onItemSelected(newThemeValue: Int) = launch(IO, applicationScope){
        themeSelectionEventChannel.send(ThemeSelectionEvent.OnThemeSelectedEvent(newThemeValue != currentTheme, newThemeValue))
        preferencesRepository.updateTheme(newThemeValue)
    }

    sealed class ThemeSelectionEvent {
        class OnThemeSelectedEvent(val recreateActivity: Boolean, val newTheme: Int): ThemeSelectionEvent()
    }
}