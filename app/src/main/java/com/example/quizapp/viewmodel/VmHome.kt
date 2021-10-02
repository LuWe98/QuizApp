package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.example.quizapp.extensions.getPagingDataAsLiveData
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    val allQuestionnairesWithQuestionsLiveData get() = localRepository.allQuestionnairesWithQuestionsLiveData.distinctUntilChanged()

    val allQuestionnairesWithQuestionsPagingData get() = getPagingDataAsLiveData(localRepository.allQuestionnairesWithQuestionsPagingSource).distinctUntilChanged()

    suspend fun allQuestionnairesForUser() = backendRepository.getQuestionnairesOfUser().distinctUntilChanged()
}