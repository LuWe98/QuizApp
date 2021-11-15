package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.model.databases.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmFacultySelection @Inject constructor(
    val localRepository: LocalRepository
) : ViewModel() {


}