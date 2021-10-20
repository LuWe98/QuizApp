package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.status.Resource
import com.example.quizapp.model.mongodb.documents.user.Role
import com.example.quizapp.model.mongodb.documents.user.User
import com.example.quizapp.view.fragments.adminscreen.BsdfChangeUserRoleArgs
import com.example.quizapp.viewmodel.VmChangeUserRole.FragmentChangeUserRoleEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class VmChangeUserRole @Inject constructor(
    private val backendRepository: BackendRepository,
    state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfChangeUserRoleArgs.fromSavedStateHandle(state)

    private val fragmentChangeUserRoleEventChannel = Channel<FragmentChangeUserRoleEvent>()

    val fragmentChangeUserRoleEventChannelFlow = fragmentChangeUserRoleEventChannel.receiveAsFlow()


    fun onSaveButtonClicked(newRole: Role) = launch(IO) {
        if(newRole == args.user.role){
            fragmentChangeUserRoleEventChannel.send(NavigateBack)
            return@launch
        }

        runCatching {
            fragmentChangeUserRoleEventChannel.send(StateTest(Resource.Loading()))
            backendRepository.updateUserRole(args.user.id, newRole)
        }.onFailure {
            fragmentChangeUserRoleEventChannel.send(StateTest(Resource.Error()))
        }.onSuccess { response ->
            if(response.isSuccessful){
                fragmentChangeUserRoleEventChannel.send(StateTest(Resource.Success(data = args.user.copy(role = newRole))))
                delay(250)
                fragmentChangeUserRoleEventChannel.send(NavigateBack)
            } else {
                fragmentChangeUserRoleEventChannel.send(StateTest(Resource.Error()))
            }
        }
    }

    sealed class FragmentChangeUserRoleEvent{
        class StateTest(val resources: Resource<User>): FragmentChangeUserRoleEvent()
        object NavigateBack: FragmentChangeUserRoleEvent()
    }
}