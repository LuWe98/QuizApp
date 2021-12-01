package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.Degree
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertCourseOfStudiesResponse.*
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminAddEditCourseOfStudiesArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import org.bson.types.ObjectId
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditCourseOfStudies @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle
): ViewModel() {

    private val args = FragmentAdminAddEditCourseOfStudiesArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if(args.courseOfStudiesWithFaculties == null) R.string.addCourseOfStudies else R.string.editCourseOfStudies

    private val parsedCourseOfStudies get() = args.courseOfStudiesWithFaculties?.courseOfStudies

    private val parsedCourseOfStudiesId get() = parsedCourseOfStudies?.id ?: ObjectId().toHexString()

    private val parsedCourseOfStudiesAbbreviation get() = parsedCourseOfStudies?.abbreviation ?: ""

    private val parsedCourseOfStudiesName get() = parsedCourseOfStudies?.name ?: ""

    private val parsedCourseOfStudiesDegree get() = parsedCourseOfStudies?.degree ?: Degree.BACHELOR

    private val parsedFacultyIds get() = args.courseOfStudiesWithFaculties?.faculties?.map(Faculty::id) ?: emptyList()



    private val addEditCourseOfStudiesEventChannel = Channel<AddEditCourseOfStudiesEvent>()

    val addEditCourseOfStudiesEventChannelFlow = addEditCourseOfStudiesEventChannel.receiveAsFlow()


    private val cosAbbreviationMutableStateFlow = state.getMutableStateFlow(COS_ABBREVIATION_KEY, parsedCourseOfStudiesAbbreviation)

    val cosAbbreviationStateFlow = cosAbbreviationMutableStateFlow.asStateFlow()

    private val cosAbbreviation get() = cosAbbreviationMutableStateFlow.value


    private val cosNameMutableStateFlow = state.getMutableStateFlow(COS_NAME_KEY, parsedCourseOfStudiesName)

    val cosNameStateFlow = cosNameMutableStateFlow.asStateFlow()

    private val cosName get() = cosNameMutableStateFlow.value


    private val cosFacultyIdsMutableStateFlow = state.getMutableStateFlow(COS_FACULTY_IDS_KEY, parsedFacultyIds)

    private val cosFacultyIds get() = cosFacultyIdsMutableStateFlow.value

    val cosFacultyIdsStateFlow = cosFacultyIdsMutableStateFlow
        .map(localRepository::getFacultiesWithIds)
        .distinctUntilChanged()


    private val cosDegreeMutableStateFlow = state.getMutableStateFlow(COS_DEGREE_KEY, parsedCourseOfStudiesDegree)

    val cosDegreeStateFlow = cosDegreeMutableStateFlow.asStateFlow()

    private val cosDegree get() = cosDegreeMutableStateFlow.value





    fun onAbbreviationCardClicked(){
        launch(IO) {
            addEditCourseOfStudiesEventChannel.send(
                NavigateToUpdateStringDialog(
                    cosAbbreviation,
                    UpdateStringType.COURSE_OF_STUDIES_ABBREVIATION
                )
            )
        }
    }

    fun onNameCardClicked(){
        launch(IO) {
            addEditCourseOfStudiesEventChannel.send(
                NavigateToUpdateStringDialog(
                    cosName,
                    UpdateStringType.COURSE_OF_STUDIES_NAME
                )
            )
        }
    }

    fun onFacultyCardClicked(){
        launch(IO) {
            addEditCourseOfStudiesEventChannel.send(NavigateToFacultySelectionScreen(cosFacultyIds.toTypedArray()))
        }
    }

    fun onDegreeCardClicked(){
        launch(IO) {
            addEditCourseOfStudiesEventChannel.send(NavigateToDegreeSelectionScreen(cosDegree))
        }
    }


    fun onAbbreviationUpdateReceived(newAbbreviation: String) {
        state.set(COS_ABBREVIATION_KEY, newAbbreviation)
        cosAbbreviationMutableStateFlow.value = newAbbreviation
    }

    fun onNameUpdateReceived(newName: String) {
        state.set(COS_NAME_KEY, newName)
        cosNameMutableStateFlow.value = newName
    }

    fun onFacultyIdsUpdateReceived(newFacultyIds: Array<String>) {
        newFacultyIds.toList().let {
            state.set(COS_FACULTY_IDS_KEY, it)
            cosFacultyIdsMutableStateFlow.value = it
        }
    }

    fun onDegreeResultReceived(newDegree: Degree) {
        state.set(COS_DEGREE_KEY, newDegree)
        cosDegreeMutableStateFlow.value = newDegree
    }


    fun onSaveButtonClicked() {
        launch(IO, applicationScope) {
            if(cosAbbreviation.isBlank() || cosName.isEmpty()) {
                addEditCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
                return@launch
            }

            if(cosFacultyIds.isEmpty()) {
                addEditCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.errorNoFacultyAssigned))
                return@launch
            }

            val updatedCourseOfStudies = CourseOfStudies(
                id = parsedCourseOfStudiesId,
                abbreviation = cosAbbreviation,
                name = cosName,
                degree = cosDegree
            )

            addEditCourseOfStudiesEventChannel.send(ShowLoadingDialog(R.string.savingCourseOfStudies))

            runCatching {
                DataMapper.mapRoomCourseOfStudiesToMongoCourseOfStudies(updatedCourseOfStudies, cosFacultyIds).let { mongoCourseOfStudies ->
                    backendRepository.insertCourseOfStudies(mongoCourseOfStudies)
                }
            }.also {
                delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
                addEditCourseOfStudiesEventChannel.send(HideLoadingDialog)
            }.onSuccess { response ->
                if(response.responseType == InsertCourseOfStudiesResponseType.SUCCESSFUL) {
                    localRepository.deleteFacultyCourseOfStudiesRelationsWith(updatedCourseOfStudies.id)

                    if(args.courseOfStudiesWithFaculties == null) {
                        localRepository.insert(updatedCourseOfStudies)
                    } else {
                        localRepository.update(updatedCourseOfStudies)
                    }

                    cosFacultyIds.map { FacultyCourseOfStudiesRelation(it, updatedCourseOfStudies.id) }.let {
                        localRepository.insert(it)
                    }

                    addEditCourseOfStudiesEventChannel.send(NavigateBackEvent)
                }

                addEditCourseOfStudiesEventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
            }.onFailure {
                addEditCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotSaveCourseOfStudies))
            }
        }
    }




    sealed class AddEditCourseOfStudiesEvent {
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: UpdateStringType) : AddEditCourseOfStudiesEvent()
        class NavigateToFacultySelectionScreen(val selectedIds: Array<String>): AddEditCourseOfStudiesEvent()
        class NavigateToDegreeSelectionScreen(val currentDegree: Degree?) : AddEditCourseOfStudiesEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int): AddEditCourseOfStudiesEvent()
        object NavigateBackEvent: AddEditCourseOfStudiesEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int): AddEditCourseOfStudiesEvent()
        object HideLoadingDialog: AddEditCourseOfStudiesEvent()
    }

    companion object {
        private const val COS_ABBREVIATION_KEY = "courseOfStudiesAbbreviationKey"
        private const val COS_NAME_KEY = "courseOfStudiesNameKey"
        private const val COS_FACULTY_IDS_KEY = "courseOfStudiesIdsKey"
        private const val COS_DEGREE_KEY = "courseOfStudiesDegreeKey"
    }
}