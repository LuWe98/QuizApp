package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertFacultyResponse.*
import com.example.quizapp.view.fragments.resultdispatcher.FragmentResultDispatcher.*
import com.example.quizapp.view.fragments.resultdispatcher.UpdateStringValueResult.*
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.managefaculties.FragmentAdminAddEditFacultiesArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.*
import com.example.quizapp.viewmodel.VmAdminAddEditFaculty.AddEditFacultyEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
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
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle
) : BaseViewModel<AddEditFacultyEvent>() {

    private val args = FragmentAdminAddEditFacultiesArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if (args.faculty == null) R.string.addFaculty else R.string.editFaculty

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
        navigationDispatcher.dispatch(ToStringUpdateDialog(AddEditFacultyAbbreviationUpdateResult(facultyAbbreviation)))
    }

    fun onNameCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(AddEditFacultyNameUpdateResult(facultyName)))
    }

    fun onAbbreviationUpdateResultReceived(result: AddEditFacultyAbbreviationUpdateResult) {
        state.set(FACULTY_ABBREVIATION_KEY, result.stringValue)
        facultyAbbreviationMutableStateFlow.value = result.stringValue
    }

    fun onNameUpdateResultReceived(result: AddEditFacultyNameUpdateResult) {
        state.set(FACULTY_NAME_KEY, result.stringValue)
        facultyNamMutableStateFlow.value = result.stringValue
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
            updatedFaculty.asMongoFaculty.let { mongoFaculty ->
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

    sealed class AddEditFacultyEvent: ViewModelEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : AddEditFacultyEvent()
    }

    companion object {
        private const val FACULTY_ABBREVIATION_KEY = "facultyAbbreviationKey"
        private const val FACULTY_NAME_KEY = "facultyNameKey"
    }
}