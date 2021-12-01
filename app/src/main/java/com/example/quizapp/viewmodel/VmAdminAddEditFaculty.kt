package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertFacultyResponse.*
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminAddEditFacultiesArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.AddEditFacultyEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.bson.types.ObjectId
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditFaculty @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = FragmentAdminAddEditFacultiesArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if(args.faculty == null) R.string.addFaculty else R.string.editFaculty

    private val parsedFacultyAbbreviation get() = args.faculty?.abbreviation ?: ""

    private val parsedFacultyName get() = args.faculty?.name ?: ""

    private val parsedFacultyId = args.faculty?.id ?: ObjectId().toHexString()



    private val addEditFacultyEventChannel = Channel<AddEditFacultyEvent>()

    val addEditFacultyEventChannelFlow = addEditFacultyEventChannel.receiveAsFlow()



    private var facultyAbbreviationMutableStateFlow = state.getMutableStateFlow(FACULTY_ABBREVIATION_KEY, parsedFacultyAbbreviation)

    val facultyAbbreviationStateFlow = facultyAbbreviationMutableStateFlow.asStateFlow()

    private val facultyAbbreviation get() = facultyAbbreviationMutableStateFlow.value



    private var facultyNamMutableStateFlow = state.getMutableStateFlow(FACULTY_NAME_KEY, parsedFacultyName)

    val facultyNameStateFlow = facultyNamMutableStateFlow.asStateFlow()

    private val facultyName get() = facultyNamMutableStateFlow.value



    fun onAbbreviationCardClicked() {
        launch {
            addEditFacultyEventChannel.send(NavigateToUpdateStringDialog(facultyAbbreviation, UpdateStringType.FACULTY_ABBREVIATION))
        }
    }

    fun onNameCardClicked() {
        launch {
            addEditFacultyEventChannel.send(NavigateToUpdateStringDialog(facultyName, UpdateStringType.FACULTY_NAME))
        }
    }

    fun onAbbreviationUpdateReceived(newAbbreviation: String) {
        state.set(FACULTY_ABBREVIATION_KEY, newAbbreviation)
        facultyAbbreviationMutableStateFlow.value = newAbbreviation
    }

    fun onNameUpdateReceived(newName: String) {
        state.set(FACULTY_NAME_KEY, newName)
        facultyNamMutableStateFlow.value = newName
    }


    fun onSaveButtonClicked() {
        launch(IO, applicationScope) {
            if (facultyAbbreviation.isBlank() || facultyName.isBlank()) {
                addEditFacultyEventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
                return@launch
            }

            val updatedFaculty = Faculty(
                id = parsedFacultyId,
                abbreviation = facultyAbbreviation.uppercase(),
                name = facultyName
            )

            addEditFacultyEventChannel.send(ShowLoadingDialog(R.string.savingFaculty))

            runCatching {
                updatedFaculty.asMongoFaculty.let { mongoFaculty ->
                    backendRepository.insertFaculty(mongoFaculty)
                }
            }.also {
                delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
                addEditFacultyEventChannel.send(HideLoadingDialog)
            }.onSuccess { response ->
                if(response.responseType == InsertFacultyResponseType.SUCCESSFUL) {
                    if(args.faculty == null) {
                        localRepository.insert(updatedFaculty)
                    } else {
                        localRepository.update(updatedFaculty)
                    }
                    addEditFacultyEventChannel.send(NavigateBackEvent)
                }
                addEditFacultyEventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
            }.onFailure {
                addEditFacultyEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotSaveFaculty))
            }
        }
    }

    sealed class AddEditFacultyEvent {
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: UpdateStringType) : AddEditFacultyEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : AddEditFacultyEvent()
        object NavigateBackEvent : AddEditFacultyEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int): AddEditFacultyEvent()
        object HideLoadingDialog: AddEditFacultyEvent()
    }


    companion object {
        private const val FACULTY_ABBREVIATION_KEY = "facultyAbbreviationKey"
        private const val FACULTY_NAME_KEY = "facultyNameKey"
    }
}