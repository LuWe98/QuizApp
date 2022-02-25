package com.example.quizapp.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.LocalRepositoryImpl
import com.example.quizapp.model.databases.room.RoomListLoadStatus
import com.example.quizapp.model.databases.room.asRoomListLoadStatus
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.DeleteCourseOfStudiesResponse.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers.CosMoreOptionsItem.DELETE
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers.CosMoreOptionsItem.EDIT
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.ConfirmationRequestType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.ManageCourseOfStudiesEvent
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.ManageCourseOfStudiesEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.ManageCourseOfStudiesEvent.ShowMessageSnackBar
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmAdminManageCoursesOfStudies @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : EventViewModel<ManageCourseOfStudiesEvent>() {

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    fun getFacultiesWithPlaceholder(context: Context) = runBlocking(IO) {
        localRepository.getAllFacultiesFlow().first().toMutableList().apply {
            add(
                Faculty(
                    id = NO_FACULTY_ID,
                    abbreviation = NO_ABBREVIATION,
                    name = context.getString(R.string.coursesOfStudiesWithoutFaculty)
                )
            )
        }.toList()
    }


    fun getCourseOfStudiesFlowWith(facultyId: String) = searchQueryMutableStateFlow.flatMapLatest { query ->
        if (facultyId == NO_FACULTY_ID) {
            localRepository.getCoursesOfStudiesNotAssociatedWithFacultyFlow(query).map { list ->
                list.asRoomListLoadStatus(query::isNotEmpty)
            }
        } else {
            localRepository.getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId, query).map { list ->
                list.asRoomListLoadStatus(query::isNotEmpty)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, RoomListLoadStatus.DataFound(emptyList()))


    fun onItemClicked(courseOfStudies: CourseOfStudies) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.CourseOfStudiesMoreOptionsSelection(courseOfStudies)))
    }

    fun onDeleteCourseOfStudiesConfirmationRequestReceived(result: ConfirmationResult.DeleteCourseOfStudiesResult) = launch(IO) {
        if(!result.confirmed) return@launch

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.deletingCourseOfStudies))

        runCatching {
            backendRepository.courseOfStudiesApi.deleteCourseOfStudies(result.courseOfStudies.id)
        }.also {
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
        }.onSuccess { response ->
            when (response.responseType) {
                DeleteCourseOfStudiesResponseType.SUCCESSFUL -> {
                    localRepository.delete(result.courseOfStudies)
                    eventChannel.send(ShowMessageSnackBar(R.string.deletedCourseOfStudies))
                }
                DeleteCourseOfStudiesResponseType.NOT_ACKNOWLEDGED -> {
                    eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteCourseOfStudies))
                }
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteCourseOfStudies))
        }
    }

    fun onCosMoreOptionsSelectionResultReceived(result: SelectionResult.CourseOfStudiesMoreOptionsResult) = launch(IO) {
        when (result.selectedItem) {
            EDIT -> {
                localRepository.getCourseOfStudiesWithFaculties(result.calledOnCourseOfStudies.id).let {
                    navigationDispatcher.dispatch(FromManageCourseOfStudiesToAddEditCourseOfStudies(it))
                }
            }
            DELETE -> {
                navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.DeleteCourseOfStudiesConfirmationRequest(result.calledOnCourseOfStudies)))
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onClearSearchQueryClicked() = launch(IO) {
        if (searchQuery.isNotEmpty()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onAddCourseOfStudiesButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromManageCourseOfStudiesToAddEditCourseOfStudies())
    }


    sealed class ManageCourseOfStudiesEvent: UiEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : ManageCourseOfStudiesEvent()
        object ClearSearchQueryEvent : ManageCourseOfStudiesEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val NO_FACULTY_ID = "NO_FACULTY_ID"
        private const val NO_ABBREVIATION = "-"
    }
}