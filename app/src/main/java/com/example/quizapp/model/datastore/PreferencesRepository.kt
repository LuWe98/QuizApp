package com.example.quizapp.model.datastore

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val encryptionUtil: EncryptionUtil
) {

    private val dataFlow = dataStore.data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }

    val themeFlow = dataFlow.map { preferences ->
        preferences[THEME_KEY] ?: AppCompatDelegate.MODE_NIGHT_NO
    }

    suspend fun getTheme(): Int = themeFlow.first()

    suspend fun updateTheme(newValue: Int) {
        dataStore.edit {
            it[THEME_KEY] = newValue
        }
    }

    val userCredentialsFlow = dataFlow.map { preferences ->
        val decryptedEmail: String = preferences[USER_EMAIL_KEY]?.let { encryptionUtil.decrypt(it) } ?: ""
        val decryptedPassword: String = preferences[USER_PASSWORD_KEY]?.let { encryptionUtil.decrypt(it) } ?: ""
        UserCredentialsWrapper(decryptedEmail, decryptedPassword)
    }

    suspend fun updateUserCredentials(email: String, password: String) {
        dataStore.edit {
            it[USER_EMAIL_KEY] = encryptionUtil.encrypt(email)
            it[USER_PASSWORD_KEY] = encryptionUtil.encrypt(password)
        }
    }

    fun getUserCredentials() = runBlocking(Dispatchers.IO) {
        userCredentialsFlow.first()
    }


    companion object {
        val THEME_KEY = intPreferencesKey("themeKey")
        val USER_EMAIL_KEY = stringPreferencesKey("userEmailKey")
        val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
    }

    data class UserCredentialsWrapper(
        val email: String,
        val password: String
    ) {
        val isValid get() = email.isNotEmpty() && password.isNotEmpty()
    }
}