package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmMain @Inject constructor(
    val localRepository: LocalRepository,
    val backendRepository: BackendRepository,
    val preferencesRepository: PreferencesRepository,
    val connectivityHelper: ConnectivityHelper
) : ViewModel() {

}