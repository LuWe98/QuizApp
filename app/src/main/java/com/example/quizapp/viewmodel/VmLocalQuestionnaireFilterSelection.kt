package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.LocalQuestionnaireOrderBy
import com.example.quizapp.viewmodel.VmLocalQuestionnaireFilterSelection.LocalQuestionnaireFilterSelectionEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmLocalQuestionnaireFilterSelection @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : ViewModel() {


    private val localQuestionnaireFilterSelectionEventChannel = Channel<LocalQuestionnaireFilterSelectionEvent>()

    val localQuestionnaireFilterSelectionEventChannelFlow = localQuestionnaireFilterSelectionEventChannel.receiveAsFlow()



    private val selectedOrderByMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_BY_KEY, runBlocking(IO) { preferencesRepository.getLocalQuestionnaireOrderBy() })

    val selectedOrderByStateFlow = selectedOrderByMutableStateFlow.asStateFlow()

    private val selectedOrderBy get() = selectedOrderByMutableStateFlow.value


    private val selectedOrderAscendingMutableStateFlow = state.getMutableStateFlow(SELECTED_ORDER_ASCENDING_KEY, runBlocking(IO) { preferencesRepository.getLocalAscendingOrder() })

    val selectedOrderAscendingStateFlow = selectedOrderAscendingMutableStateFlow.asStateFlow()

    private val selectedOrderAscending get() = selectedOrderAscendingMutableStateFlow.value




    fun onOrderByCardClicked(){
        launch(IO) {
            localQuestionnaireFilterSelectionEventChannel.send(NavigateToOrderBySelection(selectedOrderBy))
        }
    }

    fun onOrderByUpdateReceived(newValue: LocalQuestionnaireOrderBy) {
        state.set(SELECTED_ORDER_BY_KEY, newValue)
        selectedOrderByMutableStateFlow.value = newValue
    }

    fun onOrderAscendingCardClicked() {
        state.set(SELECTED_ORDER_ASCENDING_KEY, !selectedOrderAscending)
        selectedOrderAscendingMutableStateFlow.value = !selectedOrderAscending
    }


    fun onApplyButtonClicked(){
        launch(IO) {
            preferencesRepository.updateLocalAscendingOrder(selectedOrderAscending)
            preferencesRepository.updateLocalQuestionnaireOrderBy(selectedOrderBy)
            localQuestionnaireFilterSelectionEventChannel.send(ApplySelectionEvent())
        }
    }

    sealed class LocalQuestionnaireFilterSelectionEvent {
        class NavigateToOrderBySelection(val orderBy: LocalQuestionnaireOrderBy): LocalQuestionnaireFilterSelectionEvent()
        class ApplySelectionEvent(): LocalQuestionnaireFilterSelectionEvent()
    }


    companion object {
        private const val SELECTED_ORDER_BY_KEY = "selectedOrderByKey"
        private const val SELECTED_ORDER_ASCENDING_KEY = "selectedOrderAscendingKey"
    }
}