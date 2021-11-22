package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.BsdfManageCourseOfStudiesMoreOptions
import com.example.quizapp.view.fragments.adminscreens.managecourseofstudies.BsdfManageCourseOfStudiesMoreOptionsArgs
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudiesMoreOptions.FragmentAdminManageCoursesOfStudiesMoreOptionsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmAdminManageCoursesOfStudiesMoreOptions @Inject constructor(
    private val localRepository: LocalRepository,
    state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfManageCourseOfStudiesMoreOptionsArgs.fromSavedStateHandle(state)

    val courseOfStudiesName get() = args.courseOfStudies.name

    private val fragmentAdminManageCoursesOfStudiesMoreOptionsEventChannel = Channel<FragmentAdminManageCoursesOfStudiesMoreOptionsEvent>()

    val fragmentAdminManageCoursesOfStudiesMoreOptionsEventChannelFlow = fragmentAdminManageCoursesOfStudiesMoreOptionsEventChannel.receiveAsFlow()


    fun onMenuItemClicked(itemId: Int) {
        when(itemId) {
            MenuItemDataModel.COURSE_OF_STUDIES_DELETE_ITEM_ID -> onDeleteItemClicked()
            MenuItemDataModel.COURSE_OF_STUDIES_EDIT_ITEM_ID -> onEditItemClicked()
        }
    }

    private fun onEditItemClicked(){
        launch(IO) {
            localRepository.getCourseOfStudiesWithFaculties(args.courseOfStudies.id).let {
                fragmentAdminManageCoursesOfStudiesMoreOptionsEventChannel.send(NavigateToAddEditCourseOfStudiesScreenEvent(it))
            }
        }
    }

    private fun onDeleteItemClicked(){
        launch {
            fragmentAdminManageCoursesOfStudiesMoreOptionsEventChannel.send(ShowDeletionConfirmationDialog(args.courseOfStudies))
        }
    }

    sealed class FragmentAdminManageCoursesOfStudiesMoreOptionsEvent {
        class NavigateToAddEditCourseOfStudiesScreenEvent(val courseOfStudiesWithFaculties: CourseOfStudiesWithFaculties): FragmentAdminManageCoursesOfStudiesMoreOptionsEvent()
        class ShowDeletionConfirmationDialog(val courseOfStudies: CourseOfStudies): FragmentAdminManageCoursesOfStudiesMoreOptionsEvent()
    }
}