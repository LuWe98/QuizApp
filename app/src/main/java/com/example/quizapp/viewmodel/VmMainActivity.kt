package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.datastore.PreferenceRepository
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.viewmodel.VmMainActivity.*
import com.example.quizapp.viewmodel.VmMainActivity.MainViewModelEvent.ShowMessageSnackBar
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VmMainActivity @Inject constructor(
    localRepository: LocalRepository,
    private val preferenceRepository: PreferenceRepository,
    private val ktorClientAuth: KtorClientAuth,
    private val state: SavedStateHandle
) : EventViewModel<MainViewModelEvent>() {

    private var _showSplashScreen: Boolean = true

    val showSplashScreen get() = _showSplashScreen

    init {
        launch(IO) {
            delay(SPLASH_SCREEN_DELAY)
            _showSplashScreen = false
        }
    }

    val locallyPresentAuthorsFlow = localRepository.getAllLocalAuthorsFlow().distinctUntilChanged()

    val locallyPresentFacultiesFlow = localRepository.getAllFacultiesFlow().distinctUntilChanged()

    val locallyPresentCoursesOfStudiesFlow = localRepository.getAllCoursesOfStudiesFlow().distinctUntilChanged()

    val userFlow = preferenceRepository.userFlow.flowOn(IO).stateIn(viewModelScope, SharingStarted.Lazily, null)

    private var manualLogoutFlag = state.get<Boolean>(MANUAL_LOGOUT_FLAG_KEY) ?: false
        set(value) {
            state.set(MANUAL_LOGOUT_FLAG_KEY, value)
            field = value
        }

    fun onLogoutConfirmed() = launch(IO) {
        manualLogoutFlag = true
        preferenceRepository.clearPreferenceDataOnLogout()
    }

    fun onUserDataChanged(user: User?, currentDestinationId: Int?) = launch(IO) {
        user?.let {
            if (user.isEmpty && (currentDestinationId ?: 0) != R.id.fragmentAuth) {
                ktorClientAuth.resetJwtAuth()
                navigationDispatcher.dispatch(ToAuthScreen)
                eventChannel.send(ShowMessageSnackBar(if (manualLogoutFlag) R.string.loggedOut else R.string.errorLoggedOutBecauseCredentialsChanged))
                manualLogoutFlag = false
            }
        }
    }

    fun onLocallyPresentAuthorsChanged(locallyPresentAuthors: List<AuthorInfo>) = launch(IO) {
        deleteNonExistentEntries(
            locallyPresentAuthors,
            AuthorInfo::userId,
            preferenceRepository.getLocalFilteredAuthorIds(),
            preferenceRepository::updateLocalFilteredAuthorIds
        )
    }

    fun onLocallyPresentCoursesOfStudiesChanged(localCourseOfStudies: List<CourseOfStudies>) = launch(IO) {
        deleteNonExistentEntries(
            localCourseOfStudies,
            CourseOfStudies::id,
            preferenceRepository.getLocalFilteredCosIds(),
            preferenceRepository::updateLocalFilteredCoursesOfStudiesIds
        )

        deleteNonExistentEntries(
            localCourseOfStudies,
            CourseOfStudies::id,
            preferenceRepository.getBrowsableFilteredCosIds(),
            preferenceRepository::updateRemoteFilteredCoursesOfStudiesIds
        )
    }

    fun onLocallyPresentFacultiesChanges(localFaculties: List<Faculty>) = launch(IO) {
        deleteNonExistentEntries(
            localFaculties,
            Faculty::id,
            preferenceRepository.getLocalFilteredCosIds(),
            preferenceRepository::updateLocalFilteredFacultyIds
        )

        deleteNonExistentEntries(
            localFaculties,
            Faculty::id,
            preferenceRepository.getBrowsableFilteredCosIds(),
            preferenceRepository::updateRemoteFilteredFacultyIds
        )
    }

    private suspend fun <T> deleteNonExistentEntries(
        localEntries: List<T>,
        entryIdProvider: (T) -> (String),
        currentlyFilteredEntryIds: Set<String>,
        updateAction: suspend (Collection<String>) -> (Unit)
    ) {
        currentlyFilteredEntryIds.filter { id ->
            localEntries.none { entry ->
                entryIdProvider(entry) == id
            }
        }.let { unavailableIds ->
            if (unavailableIds.isNotEmpty()) {
                updateAction(currentlyFilteredEntryIds - unavailableIds.toSet())
            }
        }
    }

    sealed class MainViewModelEvent : UiEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : MainViewModelEvent()
    }


    companion object {
        private const val MANUAL_LOGOUT_FLAG_KEY = "manualLogoutFlagKey"
        private const val SPLASH_SCREEN_DELAY = 500L
    }
}