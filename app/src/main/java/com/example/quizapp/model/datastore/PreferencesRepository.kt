package com.example.quizapp.model.datastore

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.quizapp.model.room.entities.User
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val encryptionUtil: EncryptionUtil
) {

    private val dataFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            val theme = preferences[THEME_KEY] ?: AppCompatDelegate.MODE_NIGHT_NO
            val userEmail : String? = preferences[USER_EMAIL_KEY]
            val userPassword : String? = preferences[USER_PASSWORD_KEY]
            PreferencesWrapper(theme, userEmail, userPassword)
        }

    suspend fun getTheme(): Int = dataFlow.first().theme

    suspend fun updateTheme(newValue: Int) {
        dataStore.edit {
            it[THEME_KEY] = newValue
        }
    }


    val userFlow get() = dataFlow.mapNotNull {
        return@mapNotNull if(it.userEmail != null && it.userPassword != null) {
            val decryptedMail = encryptionUtil.decrypt(it.userEmail)
            val decryptedPassword = encryptionUtil.decrypt(it.userPassword)
            User(0L, decryptedMail, decryptedPassword, 0L, 0L, 0L)
        } else null
    }

    suspend fun updateUserEmail(newValue : String) {
        dataStore.edit {
            it[USER_EMAIL_KEY] = encryptionUtil.encrypt(newValue)
        }
    }

    suspend fun updateUserPassword(newValue: String) {
        dataStore.edit {
            it[USER_PASSWORD_KEY] = encryptionUtil.encrypt(newValue)
        }
    }


    companion object {
        val THEME_KEY = intPreferencesKey("themeKey")
        val USER_EMAIL_KEY = stringPreferencesKey("userEmailKey")
        val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
        val USER_ID_KEY = stringPreferencesKey("userIdKey")
    }

    data class PreferencesWrapper(
        val theme: Int,
        val userEmail: String?,
        val userPassword: String?
    )
}