package com.example.quizapp.viewmodel

import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.QuizAppLanguage
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_CHANGED
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_UP_TO_DATE
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    val userRoleFlow = userFlow.map(User::role::get).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userNameFlow = userFlow.map(User::userName::get).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val themeNameResFlow = preferencesRepository.themeFlow.map {
        when (it) {
            MODE_NIGHT_NO -> R.string.light
            MODE_NIGHT_YES -> R.string.dark
            MODE_NIGHT_FOLLOW_SYSTEM -> R.string.systemDefault
            else -> throw IllegalStateException()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val languageNameResFlow = preferencesRepository.languageFlow
        .map(QuizAppLanguage::textRes::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val shuffleTypeNameResFlow = preferencesRepository.shuffleTypeFlow
        .map(QuestionnaireShuffleType::textRes::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val preferredCoursesOfStudiesFlow = preferencesRepository.preferredCourseOfStudiesIdFlow
        .map(Set<String>::toList)
        .map(localRepository::getCoursesOfStudiesNameWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onLogoutClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(OnLogoutClickedEvent)
    }

    fun onGoToAdminPageClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(NavigateToAdminScreen)
    }

    fun onPreferredCourseOfStudiesButtonClicked(){
        launch(IO) {
            preferencesRepository.getPreferredCourseOfStudiesId().let {
                fragmentSettingsEventChannel.send(NavigateToCourseOfStudiesSelectionScreen(it))
            }
        }
    }

    fun onCourseOfStudiesUpdateTriggered(coursesOfStudiesIds: List<String>){
        launch(IO) {
            preferencesRepository.updatePreferredCourseOfStudiesIds(coursesOfStudiesIds)
        }
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

    sealed class FragmentSettingsEvent {
        object OnLogoutClickedEvent : FragmentSettingsEvent()
        object NavigateToLoginScreen : FragmentSettingsEvent()
        object NavigateToAdminScreen : FragmentSettingsEvent()
        class NavigateToCourseOfStudiesSelectionScreen(val courseOfStudiesIds: Set<String>): FragmentSettingsEvent()
        class ShowMessageSnackBarEvent(val messageRes: Int) : FragmentSettingsEvent()
    }
}