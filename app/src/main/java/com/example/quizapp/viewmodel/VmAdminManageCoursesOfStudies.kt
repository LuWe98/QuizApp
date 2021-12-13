package com.example.quizapp.viewmodel

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.R
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.DeleteCourseOfStudiesResponse.DeleteCourseOfStudiesResponseType
import com.example.quizapp.model.selection.datawrappers.CosMoreOptionsItem
import com.example.quizapp.model.selection.datawrappers.CosMoreOptionsItem.*
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.ManageCourseOfStudiesEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmAdminManageCoursesOfStudies @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val manageCourseOfStudiesEventChannel = Channel<ManageCourseOfStudiesEvent>()

    val manageCourseOfStudiesEventChannelFlow = manageCourseOfStudiesEventChannel.receiveAsFlow()


    private val searchQueryMutableStateFlow = state.getMutableStateFlow(SEARCH_QUERY_KEY, "")

    val searchQueryStateFlow = searchQueryMutableStateFlow.asStateFlow()

    val searchQuery get() = searchQueryMutableStateFlow.value



    fun getFacultiesWithPlaceholder(context: Context) = runBlocking(IO) {
        localRepository.allFacultiesFlow.first().toMutableList().apply {
            add(
                Faculty(
                id = NO_FACULTY_ID,
                abbreviation = NO_ABBREVIATION,
                name = context.getString(R.string.coursesOfStudiesWithoutFaculty)
            )
            )
        }.toList()
    }


    fun getCourseOfStudiesFlowWith(facultyId: String) = searchQueryMutableStateFlow.flatMapLatest { query ->
        if (facultyId == NO_FACULTY_ID) {
            localRepository.getCoursesOfStudiesNotAssociatedWithFacultyFlow(query)
        } else {
            localRepository.getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId, query)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onItemClicked(courseOfStudies: CourseOfStudies) {
        launch(IO) {
            manageCourseOfStudiesEventChannel.send(NavigateToManageCourseOfStudiesMoreOptionsEvent(courseOfStudies))
        }
    }

    fun onDeleteCourseOfStudiesConfirmed(confirmation: ConfirmationType.DeleteCourseOfStudiesConfirmation) = launch(IO) {
        manageCourseOfStudiesEventChannel.send(ShowLoadingDialog(R.string.deletingCourseOfStudies))

        runCatching {
            backendRepository.deleteCourseOfStudies(confirmation.courseOfStudies.id)
        }.also {
            delay(DfLoading.LOADING_DIALOG_DISMISS_DELAY)
            manageCourseOfStudiesEventChannel.send(HideLoadingDialog)
        }.onSuccess { response ->
            when (response.responseType) {
                DeleteCourseOfStudiesResponseType.SUCCESSFUL -> {
                    localRepository.delete(confirmation.courseOfStudies)
                    manageCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.deletedCourseOfStudies))
                }
                DeleteCourseOfStudiesResponseType.NOT_ACKNOWLEDGED -> {
                    manageCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteCourseOfStudies))
                }
            }
        }.onFailure {
            manageCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.errorCouldNotDeleteCourseOfStudies))
        }
    }

    fun onMoreOptionsItemSelected(item: CosMoreOptionsItem, type: SelectionType.CourseOfStudiesMoreOptionsSelection) {
        launch(IO) {
            when(item) {
                EDIT -> {
                    localRepository.getCourseOfStudiesWithFaculties(type.courseOfStudies.id).let {
                        manageCourseOfStudiesEventChannel.send(NavigateToAddEditCourseOfStudiesEvent(it))
                    }
                }
                DELETE -> manageCourseOfStudiesEventChannel.send(NavigateToConfirmDeletionEvent(type.courseOfStudies))
            }
        }
    }

    fun onSearchQueryChanged(newQuery: String) {
        state.set(SEARCH_QUERY_KEY, newQuery)
        searchQueryMutableStateFlow.value = newQuery
    }

    fun onClearSearchQueryClicked(){
        if(searchQuery.isNotBlank()) {
            launch {
                manageCourseOfStudiesEventChannel.send(ClearSearchQueryEvent)
            }
        }
    }


    sealed class ManageCourseOfStudiesEvent {
        class NavigateToManageCourseOfStudiesMoreOptionsEvent(val courseOfStudies: CourseOfStudies) : ManageCourseOfStudiesEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : ManageCourseOfStudiesEvent()
        class NavigateToConfirmDeletionEvent(val courseOfStudies: CourseOfStudies): ManageCourseOfStudiesEvent()
        class NavigateToAddEditCourseOfStudiesEvent(val courseOfStudies: CourseOfStudiesWithFaculties): ManageCourseOfStudiesEvent()
        object ClearSearchQueryEvent: ManageCourseOfStudiesEvent()
        class ShowLoadingDialog(@StringRes val messageRes: Int): ManageCourseOfStudiesEvent()
        object HideLoadingDialog: ManageCourseOfStudiesEvent()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "searchQueryKey"
        private const val NO_FACULTY_ID = "NO_FACULTY_ID"
        private const val NO_ABBREVIATION = "-"
    }
}