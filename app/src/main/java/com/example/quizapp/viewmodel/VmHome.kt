package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.quizapp.extensions.first
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val userId = preferencesRepository.userCredentialsFlow.first(viewModelScope).id

    val allQuestionnairesWithQuestionsLD =
        localRepository.findAllQuestionnairesWithQuestionsNotForUserFlow(userId).asLiveData().distinctUntilChanged()

    val allQuestionnairesWithQuestionsForUserLD =
        localRepository.findAllQuestionnairesWithQuestionsForUserFlow(userId).asLiveData().distinctUntilChanged()

    val allQuestionnairesFromDatabase = flow { emit(backendRepository.getAllQuestionnaires()) }.flowOn(Dispatchers.IO).asLiveData().distinctUntilChanged()

    suspend fun allQuestionnairesForUser() = backendRepository.getQuestionnairesOfUser()

}