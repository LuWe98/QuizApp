package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.paging.PagingConfigValues
import com.example.quizapp.model.ktor.paging.UserPagingSource
import com.example.quizapp.model.ktor.responses.DeleteUserResponse.*
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.viewmodel.VmAdminManageUsers.FragmentAdminEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VmAdminManageUsers @Inject constructor(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val applicationScope: CoroutineScope
) : ViewModel() {

    private val fragmentAdminEventChannel = Channel<FragmentAdminEvent>()

    val fragmentAdminEventChannelFlow = fragmentAdminEventChannel.receiveAsFlow()

    private val searchQuery = MutableLiveData("")

    val filteredPagedData = searchQuery.switchMap {
        Pager(config = PagingConfig(
            pageSize = PagingConfigValues.PAGE_SIZE,
            maxSize = PagingConfigValues.MAX_SIZE
        ), pagingSourceFactory = {
            //TODO -> Hier die LocallyDeletedUsers Löschen!
            UserPagingSource(backendRepository, it)
        }).liveData.cachedIn(viewModelScope)
    }.distinctUntilChanged()


    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }


    fun onUserRoleSuccessfullyChanged(userId: String, newRole: Role) = launch(IO) {
        fragmentAdminEventChannel.send(UpdateUserRoleEvent(userId, newRole))
    }

    fun onDeleteUserConfirmed(user: User) = applicationScope.launch(IO) {
        runCatching {
            backendRepository.deleteUser(user.id)
        }.onSuccess { response ->
            if (response.responseType == DeleteUserResponseType.SUCCESSFUL) {
                fragmentAdminEventChannel.send(HideUserEvent(user.id))
            }
        }
    }


    sealed class FragmentAdminEvent{
        class UpdateUserRoleEvent(val userId: String, val newRole: Role): FragmentAdminEvent()
        class HideUserEvent(val userId: String): FragmentAdminEvent()
        class ShowUserEvent(val user: User): FragmentAdminEvent()
    }
}