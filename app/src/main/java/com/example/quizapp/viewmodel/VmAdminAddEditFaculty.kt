package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.InsertFacultyResponse.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminAddEditFacultiesArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.dispatcher.fragmentresult.requests.UpdateStringRequestType
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.*
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.AddEditFacultyEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import org.bson.types.ObjectId
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditFaculty @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val dataMapper: DataMapper,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle
) : EventViewModel<AddEditFacultyEvent>() {

    private val args = FragmentAdminAddEditFacultiesArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if (args.faculty == null) R.string.create else R.string.edit

    private val parsedFacultyAbbreviation get() = args.faculty?.abbreviation ?: ""

    private val parsedFacultyName get() = args.faculty?.name ?: ""

    private val parsedFacultyId = args.faculty?.id ?: ObjectId().toHexString()


    private var facultyAbbreviationMutableStateFlow = state.getMutableStateFlow(FACULTY_ABBREVIATION_KEY, parsedFacultyAbbreviation)

    val facultyAbbreviationStateFlow = facultyAbbreviationMutableStateFlow.asStateFlow()

    private val facultyAbbreviation get() = facultyAbbreviationMutableStateFlow.value


    private var facultyNamMutableStateFlow = state.getMutableStateFlow(FACULTY_NAME_KEY, parsedFacultyName)

    val facultyNameStateFlow = facultyNamMutableStateFlow.asStateFlow()

    private val facultyName get() = facultyNamMutableStateFlow.value


    fun onAbbreviationCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(UpdateStringRequestType.UpdateFacultyAbbreviationRequest(facultyAbbreviation)))
    }

    fun onNameCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(UpdateStringRequestType.UpdateFacultyNameRequest(facultyName)))
    }

    fun onAbbreviationUpdateResultReceived(result: UpdateStringValueResult.AddEditFacultyAbbreviationUpdateResult) {
        state.set(FACULTY_ABBREVIATION_KEY, result.updatedStringValue)
        facultyAbbreviationMutableStateFlow.value = result.updatedStringValue
    }

    fun onNameUpdateResultReceived(result: UpdateStringValueResult.AddEditFacultyNameUpdateResult) {
        state.set(FACULTY_NAME_KEY, result.updatedStringValue)
        facultyNamMutableStateFlow.value = result.updatedStringValue
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onSaveButtonClicked() = launch(IO, applicationScope) {
        if (facultyAbbreviation.isBlank() || facultyName.isBlank()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        val updatedFaculty = Faculty(
            id = parsedFacultyId,
            abbreviation = facultyAbbreviation.uppercase(),
            name = facultyName
        )

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.savingFaculty))

        runCatching {
            dataMapper.mapRoomFacultyToMongoFaculty(updatedFaculty).let { mongoFaculty ->
                backendRepository.insertFaculty(mongoFaculty)
            }
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            if (response.responseType == InsertFacultyResponseType.SUCCESSFUL) {
                if (args.faculty == null) {
                    localRepository.insert(updatedFaculty)
                } else {
                    localRepository.update(updatedFaculty)
                }
                navigationDispatcher.dispatch(NavigateBack)
            }
            eventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotSaveFaculty))
        }
    }

    sealed class AddEditFacultyEvent: UiEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : AddEditFacultyEvent()
    }

    companion object {
        private const val FACULTY_ABBREVIATION_KEY = "facultyAbbreviationKey"
        private const val FACULTY_NAME_KEY = "facultyNameKey"
    }
}