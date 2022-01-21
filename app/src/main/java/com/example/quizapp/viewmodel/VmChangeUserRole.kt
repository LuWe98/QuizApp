package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.UpdateUserResponse.*
import com.example.quizapp.model.ktor.status.Resource
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.adminscreens.manageusers.BsdfUserRoleChangeArgs
import com.example.quizapp.viewmodel.VmChangeUserRole.*
import com.example.quizapp.viewmodel.VmChangeUserRole.FragmentChangeUserRoleEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class VmChangeUserRole @Inject constructor(
    private val backendRepository: BackendRepository,
    state: SavedStateHandle
) : EventViewModel<FragmentChangeUserRoleEvent>() {

    private val args = BsdfUserRoleChangeArgs.fromSavedStateHandle(state)

    fun onSaveButtonClicked(newRole: Role) = launch(IO) {
        if(newRole == args.user.role){
            navigationDispatcher.dispatch(NavigateBack)
            return@launch
        }

        runCatching {
            eventChannel.send(StateTest(Resource.Loading()))
            backendRepository.updateUserRole(args.user.id, newRole)
        }.onFailure {
            eventChannel.send(StateTest(Resource.Error()))
        }.onSuccess { response ->
            if(response.responseType == UpdateUserResponseType.UPDATE_SUCCESSFUL){
                eventChannel.send(StateTest(Resource.Success(data = args.user.copy(role = newRole))))
                delay(250)
                navigationDispatcher.dispatch(NavigateBack)
            } else {
                eventChannel.send(StateTest(Resource.Error()))
            }
        }
    }

    sealed class FragmentChangeUserRoleEvent: UiEventMarker {
        class StateTest(val resources: Resource<User>): FragmentChangeUserRoleEvent()
    }
}