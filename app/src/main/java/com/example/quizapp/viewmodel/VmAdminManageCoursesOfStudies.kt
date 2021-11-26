package com.example.quizapp.viewmodel

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.databases.room.junctions.FacultyWithCoursesOfStudies
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.DeleteCourseOfStudiesResponse.DeleteCourseOfStudiesResponseType
import com.example.quizapp.model.menus.CosMoreOptionsItem
import com.example.quizapp.model.menus.CosMoreOptionsItem.*
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.FragmentAdminManageCourseOfStudiesEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmAdminManageCoursesOfStudies @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    application: Application
) : AndroidViewModel(application) {

    private val fragmentAdminManageCourseOfStudiesEventChannel = Channel<FragmentAdminManageCourseOfStudiesEvent>()

    val fragmentAdminManageCourseOfStudiesEventChannelFlow = fragmentAdminManageCourseOfStudiesEventChannel.receiveAsFlow()

    val getFacultiesWithPlaceholder = runBlocking {
        localRepository.allFacultiesFlow.first().toMutableList().apply {
            add(Faculty(
                id = NO_FACULTY_ID,
                abbreviation = NO_ABBREVIATION,
                name = application.getString(R.string.coursesOfStudiesWithoutFaculty)
            ))
        }.toList()
    }

    fun getFacultyWithCourseOfStudiesFlow(facultyId: String) = if (facultyId == NO_FACULTY_ID) {
        localRepository.getCoursesOfStudiesNotAssociatedWithFacultyFlow()
    } else {
        localRepository.getFacultyWithCourseOfStudiesFlow(facultyId).map(FacultyWithCoursesOfStudies::coursesOfStudies::get)
    }

    fun onItemClicked(courseOfStudies: CourseOfStudies) {
        launch(IO) {
            fragmentAdminManageCourseOfStudiesEventChannel.send(NavigateToManageCourseOfStudiesMoreOptionsEvent(courseOfStudies))
        }
    }

    fun onDeleteCourseOfStudiesConfirmed(confirmation: ConfirmationType.DeleteCourseOfStudiesConfirmation) = launch(IO) {
        runCatching {
            backendRepository.deleteCourseOfStudies(confirmation.courseOfStudies.id)
        }.onSuccess { response ->
            when (response.responseType) {
                DeleteCourseOfStudiesResponseType.SUCCESSFUL -> {
                    localRepository.delete(confirmation.courseOfStudies)
                    fragmentAdminManageCourseOfStudiesEventChannel.send(ShowMessageSnackBar(R.string.deletedCourseOfStudies))
                }
                DeleteCourseOfStudiesResponseType.NOT_ACKNOWLEDGED -> {

                }
            }
        }.onFailure {

        }
    }


    fun onMoreOptionsItemSelected(item: CosMoreOptionsItem, type: SelectionType.CourseOfStudiesMoreOptionsSelection) {
        launch(IO) {
            when(item) {
                EDIT -> {
                    localRepository.getCourseOfStudiesWithFaculties(type.courseOfStudies.id).let {
                        fragmentAdminManageCourseOfStudiesEventChannel.send(NavigateToAddEditCourseOfStudiesEvent(it))
                    }
                }
                DELETE -> fragmentAdminManageCourseOfStudiesEventChannel.send(NavigateToConfirmDeletionEvent(type.courseOfStudies))
            }
        }
    }



    sealed class FragmentAdminManageCourseOfStudiesEvent {
        class NavigateToManageCourseOfStudiesMoreOptionsEvent(val courseOfStudies: CourseOfStudies) : FragmentAdminManageCourseOfStudiesEvent()
        class ShowMessageSnackBar(@StringRes val messageRes: Int) : FragmentAdminManageCourseOfStudiesEvent()
        class NavigateToConfirmDeletionEvent(val courseOfStudies: CourseOfStudies): FragmentAdminManageCourseOfStudiesEvent()
        class NavigateToAddEditCourseOfStudiesEvent(val courseOfStudies: CourseOfStudiesWithFaculties): FragmentAdminManageCourseOfStudiesEvent()
    }

    companion object {
        private const val NO_FACULTY_ID = "NO_FACULTY_ID"
        private const val NO_ABBREVIATION = "-"
    }
}