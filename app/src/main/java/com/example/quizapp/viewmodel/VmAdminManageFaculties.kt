package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.DeleteFacultyResponse.DeleteFacultyResponseType
import com.example.quizapp.model.selection.datawrappers.FacultyMoreOptionsItem
import com.example.quizapp.model.selection.datawrappers.FacultyMoreOptionsItem.DELETE
import com.example.quizapp.model.selection.datawrappers.FacultyMoreOptionsItem.EDIT
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmAdminManageFaculties.ManageFacultiesEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmAdminManageFaculties @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val fragmentAdminManageFacultiesEventChannel = Channel<ManageFacultiesEvent>()

    val fragmentAdminManageFacultiesEventChannelFlow = fragmentAdminManageFacultiesEventChannel.receiveAsFlow()


    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    val facultiesStateFlow = searchQueryMutableStateFlow.flatMapLatest {
        localRepository.findFacultiesWithNameFlow(it)
    }.distinctUntilChanged()



    fun onFacultyItemClicked(faculty: Faculty) {
        launch {
            fragmentAdminManageFacultiesEventChannel.send(NavigateToSelectionScreen(SelectionType.FacultyMoreOptionsSelection(faculty)))
        }
    }


    fun onMoreOptionsItemSelected(item: FacultyMoreOptionsItem, type: SelectionType.FacultyMoreOptionsSelection) {
        launch(IO) {
            when(item) {
                EDIT -> fragmentAdminManageFacultiesEventChannel.send(NavigateToAddEditFacultyEvent(type.faculty))
                DELETE -> fragmentAdminManageFacultiesEventChannel.send(NavigateToDeletionConfirmationEvent(type.faculty))
            }
        }
    }

    fun onDeleteFacultyConfirmed(confirmation: ConfirmationType.DeleteFacultyConfirmation) = launch(IO) {
        fragmentAdminManageFacultiesEventChannel.send(ShowLoadingDialog(R.string.deletingFaculty))

        runCatching {
            backendRepository.deleteFaculty(confirmation.faculty.id)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            fragmentAdminManageFacultiesEventChannel.send(HideLoadingDialog)
        }.onSuccess { response ->
            when(response.responseType) {
                DeleteFacultyResponseType.SUCCESSFUL -> {
                    localRepository.delete(confirmation.faculty)
                    fragmentAdminManageFacultiesEventChannel.send(ShowMessageSnackBar(R.string.deletedFaculty))
                }
                DeleteFacultyResponseType.NOT_ACKNOWLEDGED -> {
                    fragmentAdminManageFacultiesEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteFaculty))
                }
            }
        }.onFailure {
            fragmentAdminManageFacultiesEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteFaculty))
        }
    }


    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onDeleteSearchClicked(){
        if(searchQuery.isNotBlank()){
            launch {
                fragmentAdminManageFacultiesEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }


    sealed class ManageFacultiesEvent {
        object NavigateBack: ManageFacultiesEvent()
        class NavigateToSelectionScreen(val selectionType: SelectionType): ManageFacultiesEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int): ManageFacultiesEvent()
        class NavigateToAddEditFacultyEvent(val faculty: Faculty): ManageFacultiesEvent()
        class NavigateToDeletionConfirmationEvent(val faculty: Faculty): ManageFacultiesEvent()
        object ClearSearchQueryEvent: ManageFacultiesEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int): ManageFacultiesEvent()
        object HideLoadingDialog: ManageFacultiesEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
    }
}