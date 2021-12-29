package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse
import com.example.quizapp.model.ktor.BackendResponse.DeleteFacultyResponse
import com.example.quizapp.model.ktor.BackendResponse.DeleteFacultyResponse.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.FacultyMoreOptionsItem.DELETE
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.datawrappers.FacultyMoreOptionsItem.EDIT
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.resultdispatcher.requests.ConfirmationRequestType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmAdminManageFaculties.ManageFacultiesEvent
import com.example.quizapp.viewmodel.VmAdminManageFaculties.ManageFacultiesEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmAdminManageFaculties.ManageFacultiesEvent.ShowMessageSnackBar
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class VmAdminManageFaculties @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : BaseViewModel<ManageFacultiesEvent>() {

    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    val facultiesStateFlow = searchQueryMutableStateFlow.flatMapLatest {
        localRepository.findFacultiesWithNameFlow(it)
    }.distinctUntilChanged()


    fun onFacultyItemClicked(faculty: Faculty) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.FacultyMoreOptionsSelection(faculty)))
    }


    fun onFacultyMoreOptionsSelectionResultReceived(result: SelectionResult.FacultyMoreOptionsSelectionResult) = launch(IO) {
        when (result.selectedItem) {
            EDIT -> navigationDispatcher.dispatch(FromManageFacultiesToAddEditFaulty(result.calledOnFaculty))
            DELETE -> navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.DeleteFacultyConfirmationRequest(result.calledOnFaculty)))
        }
    }

    fun onDeleteFacultyConfirmationResultReceived(result: ConfirmationResult.DeleteFacultyConfirmationResult) = launch(IO) {
        if(!result.confirmed) return@launch

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.deletingFaculty))

        runCatching {
            backendRepository.deleteFaculty(result.faculty.id)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            when (response.responseType) {
                DeleteFacultyResponseType.SUCCESSFUL -> {
                    localRepository.delete(result.faculty)
                    eventChannel.send(ShowMessageSnackBar(R.string.deletedFaculty))
                }
                DeleteFacultyResponseType.NOT_ACKNOWLEDGED -> {
                    eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteFaculty))
                }
            }
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteFaculty))
        }
    }


    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onDeleteSearchClicked() = launch(IO) {
        if (searchQuery.isNotBlank()) {
            eventChannel.send(ClearSearchQueryEvent)
        }
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onAddFacultyButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromManageFacultiesToAddEditFaulty())
    }


    sealed class ManageFacultiesEvent: ViewModelEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : ManageFacultiesEvent()
        object ClearSearchQueryEvent : ManageFacultiesEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
    }
}