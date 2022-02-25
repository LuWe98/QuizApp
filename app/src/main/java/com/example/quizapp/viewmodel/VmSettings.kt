package com.example.quizapp.viewmodel

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.LocalRepositoryImpl
import com.example.quizapp.model.datastore.PreferenceRepository
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.datawrappers.QuizAppLanguage
import com.example.quizapp.model.datastore.datawrappers.QuizAppTheme
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse
import com.example.quizapp.model.ktor.BackendResponse.*
import com.example.quizapp.model.ktor.BackendResponse.DeleteUserResponse.*
import com.example.quizapp.model.ktor.BackendResponse.SyncUserDataResponse.*
import com.example.quizapp.model.ktor.BackendResponse.UpdateUserCanShareQuestionnaireWithResponse.*
import com.example.quizapp.model.ktor.backendsyncer.BackendSyncer
import com.example.quizapp.model.ktor.backendsyncer.SyncFacultyAndCourseOfStudiesResultType.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.ConfirmationRequestType
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmSettings.*
import com.example.quizapp.viewmodel.VmSettings.FragmentSettingsEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VmSettings @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val preferenceRepository: PreferenceRepository,
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val backendSyncer: BackendSyncer
) : EventViewModel<FragmentSettingsEvent>() {

    val userRoleFlow = preferenceRepository.userRoleFlow.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userNameFlow = preferenceRepository.userNameFlow.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userCanShareQuestionnaireWithFlow = preferenceRepository.userCanShareQuestionnaireWith.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val themeNameResFlow = preferenceRepository.themeFlow
        .map(QuizAppTheme::textRes::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val languageNameResFlow = preferenceRepository.languageFlow
        .map(QuizAppLanguage::textRes::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val shuffleTypeNameResFlow = preferenceRepository.shuffleTypeFlow
        .map(QuestionnaireShuffleType::textRes::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val preferredCoursesOfStudiesFlow = preferenceRepository.preferredCourseOfStudiesIdFlow
        .map(Set<String>::toList)
        .map(localRepository::getCoursesOfStudiesNameWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onLogoutClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.LogoutConfirmationRequest))
    }

    fun onDeleteAccountClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.DeleteAccountConfirmationRequest))
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
        preferenceRepository.getPreferredCourseOfStudiesId().let {
            navigationDispatcher.dispatch(ToCourseOfStudiesSelectionDialog(it))
        }
    }

    fun onCourseOfStudiesSelectionResultReceived(result: FragmentResult.CourseOfStudiesSelectionResult) = launch(IO) {
        preferenceRepository.updatePreferredCourseOfStudiesIds(result.courseOfStudiesIds.toList())
    }


    fun onLanguageButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.LanguageSelection(preferenceRepository.getLanguage())))
    }

    fun onLanguageSelectionResultReceived(result: SelectionResult.LanguageSelectionResult) = launch(IO) {
        if (preferenceRepository.getLanguage() != result.selectedItem) {
            preferenceRepository.updateLanguage(result.selectedItem)
            eventChannel.send(RecreateActivityEvent)
        }
    }


    fun onThemeButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.ThemeSelection(preferenceRepository.getTheme())))
    }

    fun onThemeSelectionResultReceived(result: SelectionResult.ThemeSelectionResult) = launch(IO) {
        if (preferenceRepository.getTheme() != result.selectedItem) {
            preferenceRepository.updateTheme(result.selectedItem)
            withContext(Main) {
                AppCompatDelegate.setDefaultNightMode(result.selectedItem.appCompatId)
            }
        }
    }

    fun onShuffleTypeButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.ShuffleTypeSelection(preferenceRepository.getShuffleType())))
    }

    fun onShuffleTypeSelectionResultReceived(result: SelectionResult.ShuffleTypeSelectionResult) = launch(IO) {
        preferenceRepository.updateShuffleType(result.selectedItem)
    }

    fun onCanShareQuestionnaireWithClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.updatingPreference))

        runCatching {
            backendRepository.userApi.updateUserCanShareQuestionnaireWith()
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            when(response.responseType) {
                UpdateUserCanShareQuestionnaireWithResponseType.SUCCESSFUL -> {
                    preferenceRepository.updateUserCanShareQuestionnaireWith(response.newValue)
                }
                UpdateUserCanShareQuestionnaireWithResponseType.NOT_ACKNOWLEDGED -> {
                    eventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotChangePreference))
                }
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotChangePreference))
        }
    }

    fun onLogoutConfirmationResultReceived(result: ConfirmationResult.LogoutConfirmationResult) = launch(IO) {
        if(result.confirmed) {
            eventChannel.send(LogoutEvent)
        }
    }

    fun onDeleteAccountConfirmationResultReceived(result: ConfirmationResult.DeleteAccountConfirmationResult) = launch(IO) {
        if(!result.confirmed) return@launch

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.deletingAccount))

        runCatching {
            backendRepository.userApi.deleteSelf()
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            if(response.responseType == DeleteUserResponseType.SUCCESSFUL) {
                eventChannel.send(LogoutEvent)
            }
        }
    }

    fun syncUserDataClicked() = launch(IO, applicationScope) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.syncingUserData))

        runCatching {
            backendRepository.userApi.syncUserData()
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            when (response.responseType) {
                SyncUserDataResponseType.DATA_UP_TO_DATE -> {
                    eventChannel.send(ShowMessageSnackBarEvent(R.string.userDataIsAlreadyUpToDate))
                }
                SyncUserDataResponseType.DATA_CHANGED -> {
                    eventChannel.send(ShowMessageSnackBarEvent(R.string.userDataUpdated))
                    preferenceRepository.updateUserRole(response.role!!)
                    preferenceRepository.updateUserLastModifiedTimeStamp(response.lastModifiedTimestamp!!)
                }
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBarEvent(R.string.errorCouldNotSyncUserData))
        }
    }

    fun onSyncQuestionnairesClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.syncingQuestionnaires))

        backendSyncer.synAllQuestionnaireData().let { resultType ->
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            eventChannel.send(ShowMessageSnackBarEvent(resultType.messageRes))
        }
    }

    fun onSyncCosAndFacultiesClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.syncingFacultiesAndCourseOfStudies))

        backendSyncer.syncFacultiesAndCoursesOfStudies().let { resultType ->
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            eventChannel.send(ShowMessageSnackBarEvent(resultType.messageRes))
        }
    }

    fun onBackButtonClicked() = launch(IO)  {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onChangePasswordCardClicked() = launch(IO)  {
        navigationDispatcher.dispatch(ToChangePasswordDialog)
    }


    sealed class FragmentSettingsEvent: UiEventMarker {
        class ShowMessageSnackBarEvent(val messageRes: Int) : FragmentSettingsEvent()
        object RecreateActivityEvent : FragmentSettingsEvent()
        object LogoutEvent : FragmentSettingsEvent()
    }
}