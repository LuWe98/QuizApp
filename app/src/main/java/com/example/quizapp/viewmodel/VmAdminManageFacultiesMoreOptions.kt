package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.view.fragments.adminscreens.managefaculties.BsdfManageFacultiesMoreOptionsArgs
import com.example.quizapp.viewmodel.VmAdminManageFacultiesMoreOptions.FragmentAdminManageFacultiesMoreOptionsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmAdminManageFacultiesMoreOptions @Inject constructor(
    state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfManageFacultiesMoreOptionsArgs.fromSavedStateHandle(state)

    val facultyName get() = args.faculty.name

    private val fragmentAdminManageFacultiesMoreOptionsEventChannel = Channel<FragmentAdminManageFacultiesMoreOptionsEvent>()

    val fragmentAdminManageFacultiesMoreOptionsEventChannelFlow = fragmentAdminManageFacultiesMoreOptionsEventChannel.receiveAsFlow()


    fun onMenuItemClicked(itemId: Int) {
        when(itemId) {
            MenuItemDataModel.FACULTY_DELETE_ITEM_ID -> onDeleteItemClicked()
            MenuItemDataModel.FACULTY_EDIT_ITEM_ID -> onEditItemClicked()
        }
    }

    private fun onEditItemClicked(){
        launch {
            fragmentAdminManageFacultiesMoreOptionsEventChannel.send(NavigateToAddEditFacultyScreenEvent(args.faculty))
        }
    }

    private fun onDeleteItemClicked(){
        launch {
            fragmentAdminManageFacultiesMoreOptionsEventChannel.send(ShowDeletionConfirmationDialog(args.faculty))
        }
    }

    sealed class FragmentAdminManageFacultiesMoreOptionsEvent {
        class NavigateToAddEditFacultyScreenEvent(val faculty: Faculty): FragmentAdminManageFacultiesMoreOptionsEvent()
        class ShowDeletionConfirmationDialog(val faculty: Faculty): FragmentAdminManageFacultiesMoreOptionsEvent()
    }
}