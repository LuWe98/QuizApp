package com.example.quizapp.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.datawrappers.QuizAppLanguage
import com.example.quizapp.model.datastore.datawrappers.QuizAppTheme
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.backendsyncer.BackendSyncer
import com.example.quizapp.model.ktor.backendsyncer.SyncFacultyAndCourseOfStudiesResultType.*
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_CHANGED
import com.example.quizapp.model.ktor.responses.SyncUserDataResponse.SyncUserDataResponseType.DATA_UP_TO_DATE
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.ConfirmationRequestType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmSettings.*
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VmSettings @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val backendSyncer: BackendSyncer
) : BaseViewModel<FragmentSettingsEvent>() {

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
        navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.LogoutConfirmationRequest))
    }

    fun onGoToManageUsersClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromSettingsToManageUsersScreen)
    }

    fun onGoToManageCoursesOfStudiesClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromSettingsToManageCoursesOfStudiesScreen)
    }

    fun onGoToManageFacultiesClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromSettingToManageFacultiesScreen)
    }

    fun onPreferredCourseOfStudiesButtonClicked() = launch(IO) {
        preferencesRepository.getPreferredCourseOfStudiesId().let {
            navigationDispatcher.dispatch(ToCourseOfStudiesSelectionDialog(it.toTypedArray()))
        }
    }

    fun onCourseOfStudiesSelectionResultReceived(result: FragmentResult.CourseOfStudiesSelectionResult) = launch(IO) {
        preferencesRepository.updatePreferredCourseOfStudiesIds(result.courseOfStudiesIds.toList())
    }


    fun onLanguageButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.LanguageSelection(preferencesRepository.getLanguage())))
    }

    fun onLanguageSelectionResultReceived(result: SelectionResult.LanguageSelectionResult) = launch(IO) {
        if (preferencesRepository.getLanguage() != result.selectedItem) {
            preferencesRepository.updateLanguage(result.selectedItem)
            eventChannel.send(RecreateActivityEvent)
        }
    }


    fun onThemeButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.ThemeSelection(preferencesRepository.getTheme())))
    }

    fun onThemeSelectionResultReceived(result: SelectionResult.ThemeSelectionResult) = launch(IO) {
        if (preferencesRepository.getTheme() != result.selectedItem) {
            preferencesRepository.updateTheme(result.selectedItem)
            withContext(Main) {
                AppCompatDelegate.setDefaultNightMode(result.selectedItem.appCompatId)
            }
        }
    }

    fun onShuffleTypeButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.ShuffleTypeSelection(preferencesRepository.getShuffleType())))
    }

    fun onShuffleTypeSelectionResultReceived(result: SelectionResult.ShuffleTypeSelectionResult) = launch(IO) {
        preferencesRepository.updateShuffleType(result.selectedItem)
    }

    fun onLogoutConfirmationResultReceived(result: ConfirmationResult.LogoutConfirmationResult) = launch(IO) {
        if(result.confirmed) {
            eventChannel.send(LogoutEvent)
        }
    }


    fun syncUserDataClicked() = launch(IO, applicationScope) {
        val user = preferencesRepository.user
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.syncingUserData))

        runCatching {
            backendRepository.syncUserData(user.id)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            when (response.responseType) {
                DATA_UP_TO_DATE -> {
                    eventChannel.send(ShowMessageSnackBarEvent(R.string.userDataIsAlreadyUpToDate))
                }
                DATA_CHANGED -> {
                    eventChannel.send(ShowMessageSnackBarEvent(R.string.userDataUpdated))
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
        }.onFailure {
            eventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotSyncUserData))
        }
    }

    fun onSyncQuestionnairesClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.syncingQuestionnaires))

        backendSyncer.synAllQuestionnaireData().let { resultType ->
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
            eventChannel.send(ShowMessageSnackBarEvent(resultType.messageRes))
        }
    }

    fun onSyncCosAndFacultiesClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.syncingFacultiesAndCourseOfStudies))

        backendSyncer.syncFacultiesAndCoursesOfStudies().let { resultType ->
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
            eventChannel.send(ShowMessageSnackBarEvent(resultType.messageRes))
        }
    }

    fun onBackButtonClicked() = launch(IO)  {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onChangePasswordCardClicked() = launch(IO)  {
        navigationDispatcher.dispatch(ToChangePasswordDialog)
    }


    sealed class FragmentSettingsEvent: ViewModelEventMarker {
        class ShowMessageSnackBarEvent(val messageRes: Int) : FragmentSettingsEvent()
        object RecreateActivityEvent : FragmentSettingsEvent()
        object LogoutEvent : FragmentSettingsEvent()
    }
}