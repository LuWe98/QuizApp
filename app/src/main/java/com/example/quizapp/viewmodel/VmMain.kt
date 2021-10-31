package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.model.mongodb.documents.user.User
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.ConnectivityHelper
import com.example.quizapp.viewmodel.VmMain.MainViewModelEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmMain @Inject constructor(
    val localRepository: LocalRepository,
    val backendRepository: BackendRepository,
    val connectivityHelper: ConnectivityHelper,
    preferencesRepository: PreferencesRepository,
    private val ktorClientAuth: KtorClientAuth
) : ViewModel() {

    private val mainViewModelEventChannel = Channel<MainViewModelEvent>()

    val mainViewModelEventChannelFlow = mainViewModelEventChannel.receiveAsFlow()

    val userFlow = preferencesRepository.userFlow.flowOn(IO).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val currentLanguage = runBlocking(IO) { preferencesRepository.getLanguage() }


    fun onUserDataChanged(user: User?, currentDestinationId: Int?) = launch(IO) {
        user?.let {
            if(user.isEmpty && (currentDestinationId ?: 0) != R.id.fragmentAuth) {
                ktorClientAuth.resetJwtAuth()
                mainViewModelEventChannel.send(NavigateToLoginScreenEvent)
            }
        }
    }

    sealed class MainViewModelEvent {
        object NavigateToLoginScreenEvent: MainViewModelEvent()
    }
}