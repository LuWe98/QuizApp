package com.example.quizapp.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.QuizAppLanguage
import com.example.quizapp.model.datastore.QuizAppTheme
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_CHANGED
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_UP_TO_DATE
import com.example.quizapp.utils.BackendSyncer
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VmSettings @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val backendSyncer: BackendSyncer
) : ViewModel() {

    private val fragmentSettingsEventChannel = Channel<FragmentSettingsEvent>()

    val fragmentSettingsEventChannelFlow = fragmentSettingsEventChannel.receiveAsFlow()

    private val userFlow = preferencesRepository.userFlow.flowOn(IO).distinctUntilChanged()

    val userRoleFlow = userFlow.map(User::role::get).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userNameFlow = userFlow.map(User::userName::get).stateIn(viewModelScope, SharingStarted.Lazily, null)

    val themeNameResFlow = preferencesRepository.themeFlow
        .map(QuizAppTheme::textRes::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

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

    fun onGoToManageUsersClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(NavigateToAdminManageUsersScreenEvent)
    }

    fun onGoToManageCoursesOfStudiesClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(NavigateToAdminManageCoursesOfStudiesScreenEvent)
    }

    fun onGoToManageFacultiesClicked() = launch(IO) {
        fragmentSettingsEventChannel.send(NavigateToAdminManageFacultiesScreenEvent)
    }

    fun onPreferredCourseOfStudiesButtonClicked() {
        launch(IO) {
            preferencesRepository.getPreferredCourseOfStudiesId().let {
                fragmentSettingsEventChannel.send(NavigateToCourseOfStudiesSelectionScreen(it.toTypedArray()))
            }
        }
    }

    fun onCourseOfStudiesUpdateTriggered(coursesOfStudiesIds: Array<String>) {
        launch(IO) {
            preferencesRepository.updatePreferredCourseOfStudiesIds(coursesOfStudiesIds.toList())
        }
    }


    fun onLanguageButtonClicked() {
        launch(IO) {
            fragmentSettingsEventChannel.send(NavigateToLanguageSelection(preferencesRepository.getLanguage()))
        }
    }

    fun onLanguageUpdateReceived(newLanguage: QuizAppLanguage) {
        launch(IO) {
            if (preferencesRepository.getLanguage() != newLanguage) {
                preferencesRepository.updateLanguage(newLanguage)
                fragmentSettingsEventChannel.send(RecreateActivityEvent)
            }
        }
    }


    fun onThemeButtonClicked() {
        launch(IO) {
            fragmentSettingsEventChannel.send(NavigateToThemeSelection(preferencesRepository.getTheme()))
        }
    }

    fun onThemeUpdateReceived(newTheme: QuizAppTheme) {
        launch(IO) {
            if (preferencesRepository.getTheme() != newTheme) {
                preferencesRepository.updateTheme(newTheme)
                withContext(Main) {
                    AppCompatDelegate.setDefaultNightMode(newTheme.appCompatId)
                }
            }
        }
    }


    fun onShuffleTypeButtonClicked() {
        launch(IO) {
            fragmentSettingsEventChannel.send(NavigateToShuffleTypeSelection(preferencesRepository.getShuffleType()))
        }
    }

    fun onShuffleTypeUpdateReceived(newShuffleType: QuestionnaireShuffleType) {
        launch(IO) {
            preferencesRepository.updateShuffleType(newShuffleType)
        }
    }

    fun onLogoutConfirmationReceived(logoutConfirmation: ConfirmationType.LogoutConfirmation) {
        launch(IO) {
            fragmentSettingsEventChannel.send(LogoutEvent)
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


    fun onSyncQuestionnairesClicked(){

    }

    fun onSyncCosAndFacultiesClicked(){

    }


    sealed class FragmentSettingsEvent {
        object OnLogoutClickedEvent : FragmentSettingsEvent()
        object NavigateToLoginScreen : FragmentSettingsEvent()
        object NavigateToAdminManageUsersScreenEvent : FragmentSettingsEvent()
        object NavigateToAdminManageCoursesOfStudiesScreenEvent : FragmentSettingsEvent()
        object NavigateToAdminManageFacultiesScreenEvent : FragmentSettingsEvent()
        class NavigateToCourseOfStudiesSelectionScreen(val courseOfStudiesIds: Array<String>) : FragmentSettingsEvent()
        class ShowMessageSnackBarEvent(val messageRes: Int) : FragmentSettingsEvent()
        object RecreateActivityEvent : FragmentSettingsEvent()
        class NavigateToLanguageSelection(val currentLanguage: QuizAppLanguage) : FragmentSettingsEvent()
        class NavigateToThemeSelection(val currentTheme: QuizAppTheme) : FragmentSettingsEvent()
        class NavigateToShuffleTypeSelection(val shuffleType: QuestionnaireShuffleType) : FragmentSettingsEvent()
        object LogoutEvent: FragmentSettingsEvent()
    }
}