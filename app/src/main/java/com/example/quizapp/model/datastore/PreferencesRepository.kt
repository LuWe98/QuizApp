package com.example.quizapp.model.datastore

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.quizapp.extensions.first
import com.example.quizapp.model.mongodb.documents.user.Role
import com.example.quizapp.model.mongodb.documents.user.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
    private val encryptionUtil: EncryptionUtil
) {

    private val dataFlow = dataStore.data.catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }

    private var cachedUser: User? = null

    suspend fun resetPreferenceData() {
        dataStore.edit { preferences ->
            cachedUser = null
            preferences[USER_ID_KEY] = ""
            preferences[USER_NAME_KEY] = ""
            preferences[USER_PASSWORD_KEY] = ""
            preferences[USER_ROLE_KEY] = ""
            preferences[THEME_KEY] = AppCompatDelegate.MODE_NIGHT_NO
            preferences[LANGUAGE_KEY] = Locale.getDefault().toLanguageTag()
        }
    }

    private val themeFlow = dataFlow.map { preferences ->
        preferences[THEME_KEY] ?: AppCompatDelegate.MODE_NIGHT_NO
    }

    suspend fun getTheme(): Int = themeFlow.first()

    suspend fun updateTheme(newValue: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = newValue
        }
    }


    private val languageFlow = dataFlow.map { preferences ->
        preferences[LANGUAGE_KEY]?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    }

    suspend fun getLanguage(): Locale = languageFlow.first()

    suspend fun updateLanguage(locale: Locale){
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = locale.toLanguageTag()
        }
    }




    val userFlow = dataFlow.map { preferences ->
        User(
            id = preferences[USER_ID_KEY]?.let { if (it.isEmpty()) "" else encryptionUtil.decrypt(it) } ?: "",
            userName = preferences[USER_NAME_KEY]?.let { if (it.isEmpty()) "" else encryptionUtil.decrypt(it) } ?: "",
            password = preferences[USER_PASSWORD_KEY]?.let { if (it.isEmpty()) "" else encryptionUtil.decrypt(it) } ?: "",
            role = preferences[USER_ROLE_KEY]?.let { if (it.isEmpty()) Role.USER else Role.valueOf(encryptionUtil.decrypt(it)) } ?: Role.USER).also {
                cachedUser = it
        }
    }

    val user
        get() = run {
            if (cachedUser == null) { cachedUser = userFlow.flowOn(IO).first(IO) }
            cachedUser!!
        }

    suspend fun getUserAsync() = userFlow.first()

    suspend fun updateUserCredentials(id: String, name: String, password: String, role: Role = Role.USER) {
        dataStore.edit { preferences ->
            cachedUser = User(id, name, password, role)
            preferences[USER_ID_KEY] = encryptionUtil.encrypt(id)
            preferences[USER_NAME_KEY] = encryptionUtil.encrypt(name)
            preferences[USER_PASSWORD_KEY] = encryptionUtil.encrypt(password)
            preferences[USER_ROLE_KEY] = encryptionUtil.encrypt(role.name)
        }
    }




    companion object PreferencesKeys {
        val THEME_KEY = intPreferencesKey("themeKey")
        val LANGUAGE_KEY = stringPreferencesKey("languageKey")
        val USER_ID_KEY = stringPreferencesKey("userIdKey")
        val USER_NAME_KEY = stringPreferencesKey("userNameKey")
        val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
        val USER_ROLE_KEY = stringPreferencesKey("userRoleKey")
    }
}