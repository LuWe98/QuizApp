package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.example.quizapp.extensions.getPagingDataAsLiveData
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmHome @Inject constructor(
    private val localRepository: LocalRepository
) : ViewModel() {

    val allQuestionnairesWithQuestionsLiveData get() = localRepository.allQuestionnairesWithQuestionsLiveData.distinctUntilChanged()

    val allQuestionnairesWithQuestionsPagingData get() = getPagingDataAsLiveData(localRepository.allQuestionnairesWithQuestionsPagingSource).distinctUntilChanged()

}