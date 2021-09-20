package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesManager
import com.example.quizapp.model.ktor.KtorRepository
import com.example.quizapp.model.room.LocalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmMain @Inject constructor(
    private val localRepository: LocalRepository,
    private val ktorRepository: KtorRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val currentTheme : Int get() = runBlocking {
        preferencesManager.getTheme()
    }

    fun updateThemeValue(newThemeValue: Int) {
        launch {
            preferencesManager.updateTheme(newThemeValue)
        }
    }


    fun getTodosKtor() = ktorRepository.getTodos()

    suspend fun getTodoKtor(todoId : Int) = ktorRepository.getTodo(todoId)

    suspend fun loginUser(email : String, password : String) = ktorRepository.loginUser(email, password)

    suspend fun registerUser(email : String, username : String, password : String) = ktorRepository.registerUser(email, username, password)

}