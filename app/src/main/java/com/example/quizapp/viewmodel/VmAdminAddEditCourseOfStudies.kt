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
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.FragmentAdminAddEditCourseOfStudiesArgs
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.*
import com.example.quizapp.viewmodel.VmAdminAddEditCourseOfStudies.AddEditCourseOfStudiesEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
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
) : EventViewModel<AddEditCourseOfStudiesEvent>() {

    private val args = FragmentAdminAddEditCourseOfStudiesArgs.fromSavedStateHandle(state)

    val pageTitleRes get() = if (args.courseOfStudiesWithFaculties == null) R.string.create else R.string.edit

    private val parsedCourseOfStudies get() = args.courseOfStudiesWithFaculties?.courseOfStudies

    private val parsedCourseOfStudiesId get() = parsedCourseOfStudies?.id ?: ObjectId().toHexString()

    private val parsedCourseOfStudiesAbbreviation get() = parsedCourseOfStudies?.abbreviation ?: ""

    private val parsedCourseOfStudiesName get() = parsedCourseOfStudies?.name ?: ""

    private val parsedCourseOfStudiesDegree get() = parsedCourseOfStudies?.degree ?: Degree.BACHELOR

    private val parsedFacultyIds get() = args.courseOfStudiesWithFaculties?.faculties?.map(Faculty::id) ?: emptyList()


    private var _cosAbbreviation = state.get<String>(COS_ABBREVIATION_KEY) ?:  parsedCourseOfStudiesAbbreviation
        set(value) {
            state.set(COS_ABBREVIATION_KEY, value)
            field = value
        }

    val cosAbbreviation get() = _cosAbbreviation


    private var _cosName = state.get<String>(COS_NAME_KEY) ?: parsedCourseOfStudiesName
    set(value) {
        state.set(COS_NAME_KEY, value)
        field = value
    }

    val cosName get() = _cosName


    private val cosFacultyIdsMutableStateFlow = state.getMutableStateFlow(COS_FACULTY_IDS_KEY, parsedFacultyIds)

    private val cosFacultyIds get() = cosFacultyIdsMutableStateFlow.value

    val cosFacultyIdsStateFlow = cosFacultyIdsMutableStateFlow
        .map(localRepository::getFacultiesWithIds)
        .distinctUntilChanged()


    private val cosDegreeMutableStateFlow = state.getMutableStateFlow(COS_DEGREE_KEY, parsedCourseOfStudiesDegree)

    val cosDegreeStateFlow = cosDegreeMutableStateFlow.asStateFlow()

    private val cosDegree get() = cosDegreeMutableStateFlow.value


    fun setFacultyIds(ids: List<String>) {
        state.set(COS_FACULTY_IDS_KEY, ids)
        cosFacultyIdsMutableStateFlow.value = ids
    }

    fun onAbbreviationUpdated(abbr: String) {
        _cosAbbreviation = abbr
    }

    fun onNameChanged(name: String) {
        _cosName = name
    }

    fun onFacultyCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToFacultySelectionDialog(cosFacultyIds))
    }

    fun onDegreeCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.DegreeSelection(cosDegree)))
    }

    fun onClearFacultiesClicked(){
        setFacultyIds(emptyList())
    }

    fun onFacultySelectionResultReceived(result: FragmentResult.FacultySelectionResult) {
        setFacultyIds(result.facultyIds)
    }

    fun onFacultyChipClicked(faculty: Faculty) {
        cosFacultyIds.toMutableList().apply {
            remove(faculty.id)
            setFacultyIds(this)
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
            navigationDispatcher.dispatchDelayed(PopLoadingDialog, DfLoading.LOADING_DIALOG_DISMISS_DELAY)
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