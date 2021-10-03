package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.EncryptionUtil
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmMain @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    val preferencesRepository: PreferencesRepository,
    val encryptionUtil: EncryptionUtil,
    val connectivityHelper: ConnectivityHelper
) : ViewModel() {

    val currentTheme get() = runBlocking(Dispatchers.IO) {
        preferencesRepository.getTheme()
    }

    suspend fun getQuestionnairesOfUser() = backendRepository.getQuestionnairesOfUser()
}