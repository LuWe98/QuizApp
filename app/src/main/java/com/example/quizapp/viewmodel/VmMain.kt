package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.EncryptionUtil
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmMain @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val preferencesRepository: PreferencesRepository,
    val encryptionUtil: EncryptionUtil,
    val connectivityHelper: ConnectivityHelper
) : ViewModel() {

    val userFlow get() = preferencesRepository.userFlow

    val currentTheme get() = runBlocking {
        preferencesRepository.getTheme()
    }

    fun updateThemeValue(newThemeValue: Int) {
        launch {
            preferencesRepository.updateTheme(newThemeValue)
        }
    }

    fun updateUserEmail(newValue: String) {
        launch {
            preferencesRepository.updateUserEmail(newValue)
        }
    }

    fun updateUserPassword(newValue: String) {
        launch {
            preferencesRepository.updateUserPassword(newValue)
        }
    }


    fun getTodosKtor() = backendRepository.getTodos()

    suspend fun getTodoKtor(todoId: Int) = backendRepository.getTodo(todoId)

    suspend fun loginUser(email: String, password: String) = backendRepository.loginUser(email, password)

    suspend fun registerUser(email: String, username: String, password: String) = backendRepository.registerUser(email, username, password)

}