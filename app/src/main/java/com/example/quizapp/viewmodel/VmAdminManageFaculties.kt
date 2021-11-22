package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.DeleteFacultyResponse
import com.example.quizapp.model.ktor.responses.DeleteFacultyResponse.*
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.viewmodel.VmAdminManageFaculties.FragmentAdminManageFacultiesEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class VmAdminManageFaculties @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository
) : ViewModel() {

    private val fragmentAdminManageFacultiesEventChannel = Channel<FragmentAdminManageFacultiesEvent>()

    val fragmentAdminManageFacultiesEventChannelFlow = fragmentAdminManageFacultiesEventChannel.receiveAsFlow()

    val facultiesStateFlow = localRepository.allFacultiesFlow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onFacultyItemClicked(faculty: Faculty) {
        launch {
            fragmentAdminManageFacultiesEventChannel.send(NavigateToFacultiesMoreOptionsDialogEvent(faculty))
        }
    }

    fun onDeleteFacultyConfirmed(faculty: Faculty) = launch(IO) {
        runCatching {
            fragmentAdminManageFacultiesEventChannel.send(ChangeProgressVisibilityEvent(true))
            backendRepository.deleteFaculty(faculty.id)
        }.onSuccess { response ->
            when(response.responseType) {
                DeleteFacultyResponseType.SUCCESSFUL -> {
                    localRepository.delete(faculty)
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

    sealed class FragmentAdminManageFacultiesEvent {
        object NavigateBack: FragmentAdminManageFacultiesEvent()
        class NavigateToFacultiesMoreOptionsDialogEvent(val faculty: Faculty): FragmentAdminManageFacultiesEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int): FragmentAdminManageFacultiesEvent()
        class ChangeProgressVisibilityEvent(val visible: Boolean): FragmentAdminManageFacultiesEvent()
    }
}