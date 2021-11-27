package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.DeleteFacultyResponse.DeleteFacultyResponseType
import com.example.quizapp.model.menus.FacultyMoreOptionsItem
import com.example.quizapp.model.menus.FacultyMoreOptionsItem.DELETE
import com.example.quizapp.model.menus.FacultyMoreOptionsItem.EDIT
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmAdminManageFaculties.FragmentAdminManageFacultiesEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmAdminManageFaculties @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val fragmentAdminManageFacultiesEventChannel = Channel<FragmentAdminManageFacultiesEvent>()

    val fragmentAdminManageFacultiesEventChannelFlow = fragmentAdminManageFacultiesEventChannel.receiveAsFlow()


    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value


    val facultiesStateFlow = searchQueryMutableStateFlow.map {
        localRepository.findFacultiesWithName(it)
    }.distinctUntilChanged()



    fun onFacultyItemClicked(faculty: Faculty) {
        launch {
            fragmentAdminManageFacultiesEventChannel.send(NavigateToFacultiesMoreOptionsDialogEvent(faculty))
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
        runCatching {
            fragmentAdminManageFacultiesEventChannel.send(ChangeProgressVisibilityEvent(true))
            backendRepository.deleteFaculty(confirmation.faculty.id)
        }.onSuccess { response ->
            when(response.responseType) {
                DeleteFacultyResponseType.SUCCESSFUL -> {
                    localRepository.delete(confirmation.faculty)
                    fragmentAdminManageFacultiesEventChannel.send(ShowMessageSnackBar(R.string.deletedFaculty))
                }
                DeleteFacultyResponseType.NOT_ACKNOWLEDGED -> {

                }
            }
        }.onFailure {
            log("FAILURE: $it")
        }

        fragmentAdminManageFacultiesEventChannel.send(ChangeProgressVisibilityEvent(false))
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


    sealed class FragmentAdminManageFacultiesEvent {
        object NavigateBack: FragmentAdminManageFacultiesEvent()
        class NavigateToFacultiesMoreOptionsDialogEvent(val faculty: Faculty): FragmentAdminManageFacultiesEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int): FragmentAdminManageFacultiesEvent()
        class ChangeProgressVisibilityEvent(val visible: Boolean): FragmentAdminManageFacultiesEvent()
        class NavigateToAddEditFacultyEvent(val faculty: Faculty): FragmentAdminManageFacultiesEvent()
        class NavigateToDeletionConfirmationEvent(val faculty: Faculty): FragmentAdminManageFacultiesEvent()
        object ClearSearchQueryEvent: FragmentAdminManageFacultiesEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
    }
}