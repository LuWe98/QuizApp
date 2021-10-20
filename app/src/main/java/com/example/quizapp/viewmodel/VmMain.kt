package com.example.quizapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.quizapp.model.datastore.EncryptionUtil
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.utils.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.statement.*
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class VmMain @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    val preferencesRepository: PreferencesRepository,
    val encryptionUtil: EncryptionUtil,
    val connectivityHelper: ConnectivityHelper
) : ViewModel() {

}