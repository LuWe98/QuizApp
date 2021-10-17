package com.example.quizapp.model.datastore

import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.quizapp.extensions.first
import com.example.quizapp.model.datastore.PreferencesKeys.THEME_KEY
import com.example.quizapp.model.datastore.PreferencesKeys.USER_ID_KEY
import com.example.quizapp.model.datastore.PreferencesKeys.USER_NAME_KEY
import com.example.quizapp.model.datastore.PreferencesKeys.USER_PASSWORD_KEY
import com.example.quizapp.model.datastore.PreferencesKeys.USER_ROLE_KEY
import com.example.quizapp.model.mongodb.documents.user.Role
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.Credentials
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val dataStore: DataStore<Preferences>,
    private val encryptionUtil: EncryptionUtil
) {

    private val dataFlow = dataStore.data.catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }

    suspend fun resetPreferenceData() {
        dataStore.edit { preferences ->
            cachedUserFlow = null
            preferences[USER_ID_KEY] = ""
            preferences[USER_NAME_KEY] = ""
            preferences[USER_PASSWORD_KEY] = ""
            preferences[USER_ROLE_KEY] = ""
            preferences[THEME_KEY] = AppCompatDelegate.MODE_NIGHT_NO
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


    //TODO -> ENTSCHLÜSSELUNG TAKES TO LONG!
    val userInfoFlow = dataFlow.map { preferences ->
        UserInfoWrapper(
            id = preferences[USER_ID_KEY]?.let { if (it.isEmpty()) "" else encryptionUtil.decrypt(it) } ?: "",
            name = preferences[USER_NAME_KEY]?.let { if (it.isEmpty()) "" else encryptionUtil.decrypt(it) } ?: "",
            password = preferences[USER_PASSWORD_KEY]?.let { if (it.isEmpty()) "" else encryptionUtil.decrypt(it) } ?: "",
            role = preferences[USER_ROLE_KEY]?.let { if (it.isEmpty()) Role.USER else Role.valueOf(encryptionUtil.decrypt(it)) } ?: Role.USER).also {
            if (cachedUserFlow == null) {
                cachedUserFlow = it
            }
        }
    }

    private var cachedUserFlow: UserInfoWrapper? = null

    val userInfo
        get() = run {
            if (cachedUserFlow == null) { cachedUserFlow = userInfoFlow.flowOn(IO).first(IO) }
            cachedUserFlow!!
        }

    val userCredentials
        get() = userInfo.let {
            Credentials.basic(it.name, it.password, Charsets.UTF_8)
        }

    suspend fun updateUserCredentials(id: String, name: String, password: String, role: Role = Role.USER) {
        dataStore.edit { preferences ->
            cachedUserFlow = UserInfoWrapper(id, name, password, role)
            preferences[USER_ID_KEY] = encryptionUtil.encrypt(id)
            preferences[USER_NAME_KEY] = encryptionUtil.encrypt(name)
            preferences[USER_PASSWORD_KEY] = encryptionUtil.encrypt(password)
            preferences[USER_ROLE_KEY] = encryptionUtil.encrypt(role.name)
        }
    }
}