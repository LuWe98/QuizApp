package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmSearch @Inject constructor(
    val localRepository: LocalRepository,
    val backendRepository: BackendRepository
) : ViewModel() {


}