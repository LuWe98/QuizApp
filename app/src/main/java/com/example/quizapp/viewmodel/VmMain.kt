package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesManager
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.EntityMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmMain @Inject constructor(
    private val localRepository: LocalRepository,
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
}