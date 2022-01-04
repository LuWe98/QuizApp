package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.properties.Degree
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.entities.FacultyCourseOfStudiesRelation
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.InsertCourseOfStudiesResponse.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminAddEditCourseOfStudiesArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.dispatcher.fragmentresult.requests.UpdateStringRequestType
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.*
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.bson.types.ObjectId
import javax.inject.Inject

@HiltViewModel
class VmAdminAddEditCourseOfStudies @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val dataMapper: DataMapper,
    private val applicationScope: CoroutineScope,
    private val state: SavedStateHandle
) : BaseViewModel<AddEditCourseOfStudiesEvent>() {

    private val args = FragmentAdminAddEditCourseOfStudiesArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if (args.courseOfStudiesWithFaculties == null) R.string.create else R.string.edit

    private val parsedCourseOfStudies get() = args.courseOfStudiesWithFaculties?.courseOfStudies

    private val parsedCourseOfStudiesId get() = parsedCourseOfStudies?.id ?: ObjectId().toHexString()

    private val parsedCourseOfStudiesAbbreviation get() = parsedCourseOfStudies?.abbreviation ?: ""

    private val parsedCourseOfStudiesName get() = parsedCourseOfStudies?.name ?: ""

    private val parsedCourseOfStudiesDegree get() = parsedCourseOfStudies?.degree ?: Degree.BACHELOR

    private val parsedFacultyIds get() = args.courseOfStudiesWithFaculties?.faculties?.map(Faculty::id) ?: emptyList()


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


    fun onAbbreviationCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(UpdateStringRequestType.UpdateCourseOfStudiesAbbreviationRequest(cosAbbreviation)))
    }

    fun onNameCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToStringUpdateDialog(UpdateStringRequestType.UpdateCourseOfStudiesNameRequest(cosName)))
    }

    fun onFacultyCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToFacultySelectionDialog(cosFacultyIds))
    }


    fun onDegreeCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.DegreeSelection(cosDegree)))
    }


    fun onAbbreviationUpdateResultReceived(result: UpdateStringValueResult.AddEditCourseOfStudiesAbbreviationUpdateResult) {
        state.set(COS_ABBREVIATION_KEY, result.updatedStringValue)
        cosAbbreviationMutableStateFlow.value = result.updatedStringValue
    }

    fun onNameUpdateResultReceived(result: UpdateStringValueResult.AddEditCourseOfStudiesNameUpdateResult) {
        state.set(COS_NAME_KEY, result.updatedStringValue)
        cosNameMutableStateFlow.value = result.updatedStringValue
    }

    fun onFacultySelectionResultReceived(result: FragmentResult.FacultySelectionResult) {
        result.facultyIds.let {
            state.set(COS_FACULTY_IDS_KEY, it)
            cosFacultyIdsMutableStateFlow.value = it
        }
    }

    fun onFacultyChipClicked(faculty: Faculty) {
        cosFacultyIds.toMutableList().apply {
            remove(faculty.id)
            state.set(COS_FACULTY_IDS_KEY, this)
            cosFacultyIdsMutableStateFlow.value = this
        }
    }

    fun onDegreeSelectionResultReceived(result: SelectionResult.DegreeSelectionResult) {
        state.set(COS_DEGREE_KEY, result.selectedItem)
        cosDegreeMutableStateFlow.value = result.selectedItem
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onSaveButtonClicked() = launch(IO, applicationScope) {
        if (cosAbbreviation.isBlank() || cosName.isEmpty()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorSomeFieldsAreEmpty))
            return@launch
        }

        if (cosFacultyIds.isEmpty()) {
            eventChannel.send(ShowMessageSnackBar(R.string.errorNoFacultyAssigned))
            return@launch
        }

        val updatedCourseOfStudies = CourseOfStudies(
            id = parsedCourseOfStudiesId,
            abbreviation = cosAbbreviation,
            name = cosName,
            degree = cosDegree
        )

        navigationDispatcher.dispatch(ToLoadingDialog(R.string.savingCourseOfStudies))

        runCatching {
            dataMapper.mapRoomCourseOfStudiesToMongoCourseOfStudies(updatedCourseOfStudies, cosFacultyIds).let { mongoCourseOfStudies ->
                backendRepository.insertCourseOfStudies(mongoCourseOfStudies)
            }
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            navigationDispatcher.dispatch(PopLoadingDialog)
        }.onSuccess { response ->
            if (response.responseType == InsertCourseOfStudiesResponseType.SUCCESSFUL) {
                localRepository.deleteFacultyCourseOfStudiesRelationsWith(updatedCourseOfStudies.id)

                if (args.courseOfStudiesWithFaculties == null) {
                    localRepository.insert(updatedCourseOfStudies)
                } else {
                    localRepository.update(updatedCourseOfStudies)
                }

                cosFacultyIds.map { FacultyCourseOfStudiesRelation(it, updatedCourseOfStudies.id) }.let {
                    localRepository.insert(it)
                }

                navigationDispatcher.dispatch(NavigateBack)
            }

            eventChannel.send(ShowMessageSnackBar(response.responseType.messageRes))
        }.onFailure {
            eventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotSaveCourseOfStudies))
        }
    }


    sealed class AddEditCourseOfStudiesEvent: UiEventMarker {
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : AddEditCourseOfStudiesEvent()
    }

    companion object {
        private const val COS_ABBREVIATION_KEY = "courseOfStudiesAbbreviationKey"
        private const val COS_NAME_KEY = "courseOfStudiesNameKey"
        private const val COS_FACULTY_IDS_KEY = "courseOfStudiesIdsKey"
        private const val COS_DEGREE_KEY = "courseOfStudiesDegreeKey"
    }
}