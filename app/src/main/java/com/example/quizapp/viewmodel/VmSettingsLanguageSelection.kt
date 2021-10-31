package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.menudatamodels.MenuItemDataModel
import com.example.quizapp.view.fragments.settingsscreen.QuizAppLanguage
import com.example.quizapp.viewmodel.VmSettingsLanguageSelection.LanguageSelectionEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VmSettingsLanguageSelection @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val applicationScope: CoroutineScope
) : ViewModel() {

    private val languageSelectionEventChannel = Channel<LanguageSelectionEvent>()

    val languageSelectionEventChannelFlow = languageSelectionEventChannel.receiveAsFlow()

    val currentLanguage = runBlocking { preferencesRepository.getLanguage() }

    fun onItemSelected(itemId: Int) = launch(IO, applicationScope){
        val selectedLanguage = QuizAppLanguage.values()[itemId]
        languageSelectionEventChannel.send(OnLanguageSelectedEvent(selectedLanguage != currentLanguage))
        preferencesRepository.updateLanguage(selectedLanguage)
    }

    sealed class LanguageSelectionEvent {
        class OnLanguageSelectedEvent(val recreateActivity: Boolean): LanguageSelectionEvent()
    }
}