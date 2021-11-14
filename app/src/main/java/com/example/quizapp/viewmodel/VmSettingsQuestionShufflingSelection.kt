package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmSettingsQuestionShufflingSelection @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val applicationScope: CoroutineScope
) : ViewModel() {

    private val questionShufflingSelectionEventChannel = Channel<QuestionOrderingSelectionEvent>()

    val questionShufflingSelectionEventChannelFlow = questionShufflingSelectionEventChannel.receiveAsFlow()

    val currentQuestionShuffleType = runBlocking { preferencesRepository.getShuffleType() }

    fun onItemSelected(itemId: Int) = launch(IO, applicationScope){
        val selectedShuffleType = QuestionnaireShuffleType.values()[itemId]
        questionShufflingSelectionEventChannel.send(QuestionOrderingSelectionEvent.OnQuestionOrderingSelectedEvent)
        preferencesRepository.updateShuffleType(selectedShuffleType)
    }

    sealed class QuestionOrderingSelectionEvent {
        object OnQuestionOrderingSelectedEvent: QuestionOrderingSelectionEvent()
    }
}