package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.menus.MenuItemDataModel
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.view.fragments.adminscreen.BsdfUserMoreOptionsArgs
import com.example.quizapp.viewmodel.VmUserMoreOptions.UserMoreOptionsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmUserMoreOptions @Inject constructor(
    state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfUserMoreOptionsArgs.fromSavedStateHandle(state)

    private val userMoreOptionsEventChannel = Channel<UserMoreOptionsEvent>()

    val userMoreOptionsEventChannelFlow = userMoreOptionsEventChannel.receiveAsFlow()

    fun onMenuItemSelected(itemId: Int) {
        when (itemId) {
            MenuItemDataModel.DELETE_USER_ITEM_ID -> onDeleteUserItemSelected()
            MenuItemDataModel.CHANGE_USER_ROLE_ITEM_ID -> onChangeUserRoleItemSelected()
        }
    }

    private fun onDeleteUserItemSelected() = launch {
        userMoreOptionsEventChannel.send(DeleteUserEvent(args.user))
        userMoreOptionsEventChannel.send(NavigateBackEvent)
    }

    private fun onChangeUserRoleItemSelected() = launch {
        userMoreOptionsEventChannel.send(NavigateToChangeUserRoleDialogEvent(args.user))
    }

    sealed class UserMoreOptionsEvent {
        class NavigateToChangeUserRoleDialogEvent(val user: User) : UserMoreOptionsEvent()
        class DeleteUserEvent(val user: User) : UserMoreOptionsEvent()
        object NavigateBackEvent : UserMoreOptionsEvent()
    }
}