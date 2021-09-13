package com.example.quizapp.model.datastore

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.quizapp.utils.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_NAME)
    private val dataStore: DataStore<Preferences> get() = context.dataStore

    val dataFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { preferences ->
            val testKey = preferences[THEME_KEY] ?: AppCompatDelegate.MODE_NIGHT_NO
            PreferencesWrapper(testKey)
        }

    suspend fun getTheme(): Int = dataFlow.first().theme

    suspend fun updateTheme(newValue: Int) {
        dataStore.edit {
            it[THEME_KEY] = newValue
        }
    }


    companion object {
        val THEME_KEY = intPreferencesKey("themeKey")
        val USER_ID_KEY = stringPreferencesKey("userIdKey")
        val USER_NAME_KEY = stringPreferencesKey("userNameKey")
        val USER_FACULTY_KEY = stringPreferencesKey("userFacultyKey")
    }

    data class PreferencesWrapper(val theme: Int)
}