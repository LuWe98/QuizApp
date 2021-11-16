package com.example.quizapp.model.datastore

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.quizapp.extensions.dataStore
import com.example.quizapp.extensions.dataflow
import com.example.quizapp.model.databases.mongodb.documents.user.Role
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.utils.EncryptionUtil.decrypt
import com.example.quizapp.utils.EncryptionUtil.encrypt
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object PreferencesKeys {
        val LANGUAGE_KEY = stringPreferencesKey("languageKey")
        private val THEME_KEY = intPreferencesKey("themeKey")
        private val SHUFFLE_TYPE_KEY = stringPreferencesKey("shuffleTypePreference")
        private val PREFERRED_COURSE_OF_STUDIES_ID_KEY = stringSetPreferencesKey("preferredCosKey")

        private val JWT_TOKEN_KEY = stringPreferencesKey("jwtTokenKey")
        private val USER_ID_KEY = stringPreferencesKey("userIdKey")
        private val USER_NAME_KEY = stringPreferencesKey("userNameKey")
        private val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
        private val USER_ROLE_KEY = stringPreferencesKey("userRoleKey")
        private val USER_LAST_MODIFIED_TIMESTAMP_KEY = longPreferencesKey("userLastModifiedTimeStampKey")

        const val EMPTY_STRING = ""
        const val UNKNOWN_TIMESTAMP = -1L
        val DEFAULT_ROLE = Role.USER
    }

    private val dataFlow = dataStore.dataflow

    suspend fun clearPreferenceData() {
        dataStore.edit { preferences ->
            cachedUser = null
            preferences[USER_NAME_KEY] = EMPTY_STRING
            preferences[USER_PASSWORD_KEY] = EMPTY_STRING
            preferences[USER_ROLE_KEY] = EMPTY_STRING
            preferences[JWT_TOKEN_KEY] = EMPTY_STRING
        }
    }



    val themeFlow = dataFlow.map { preferences ->
        preferences[THEME_KEY] ?: AppCompatDelegate.MODE_NIGHT_NO
    }

    suspend fun getTheme(): Int = themeFlow.first()

    suspend fun updateTheme(newValue: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = newValue
        }
    }



    val languageFlow = dataFlow.map { preferences ->
        preferences[LANGUAGE_KEY]?.let { QuizAppLanguage.valueOf(it) } ?: QuizAppLanguage.ENGLISH
    }

    suspend fun getLanguage(): QuizAppLanguage = languageFlow.first()

    suspend fun updateLanguage(quizAppLanguage: QuizAppLanguage){
        dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = quizAppLanguage.name
        }
    }

    val shuffleTypeFlow = dataFlow.map { preferences ->
        preferences[SHUFFLE_TYPE_KEY]?.let { QuestionnaireShuffleType.valueOf(it) } ?: QuestionnaireShuffleType.NONE
    }

    suspend fun getShuffleType() : QuestionnaireShuffleType = shuffleTypeFlow.first()

    suspend fun updateShuffleType(shuffleType: QuestionnaireShuffleType) {
        dataStore.edit { preferences ->
            preferences[SHUFFLE_TYPE_KEY] = shuffleType.name
        }
    }




    //TODO -> Mehr options einbauen:
    // Use preferredCos for filtering -> Also bei der Suche von Fragebögen
    // Use preferredCos for Questionnaire creation -> Dass das Feld prefilled wird mit der COS
    val preferredCourseOfStudiesIdFlow = dataFlow.map { preferences ->
        preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] ?: emptySet()
    }

    suspend fun getPreferredCourseOfStudiesId() = preferredCourseOfStudiesIdFlow.first()

    suspend fun updatePreferredCourseOfStudiesIds(courseOfStudiesIds: List<String>) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] = courseOfStudiesIds.toSet()
        }
    }






    private val jwtTokenFlow = dataFlow.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    suspend fun getJwtToken() = jwtTokenFlow.first()?.let {
        if(it.isEmpty()) null else it
    }

    suspend fun updateJwtToken(token: String?){
        dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token ?: EMPTY_STRING
        }
    }


    private var cachedUser: User? = null

    val userFlow = dataFlow.map { preferences ->
        withContext(IO) {
            val id = async {
                preferences[USER_ID_KEY]?.let {
                    if (it.isEmpty()) EMPTY_STRING else it.decrypt()
                } ?: EMPTY_STRING
            }
            val userName = async {
                preferences[USER_NAME_KEY]?.let {
                    if (it.isEmpty()) EMPTY_STRING else it.decrypt()
                } ?: EMPTY_STRING
            }
            val password = async {
                preferences[USER_PASSWORD_KEY]?.let {
                    if (it.isEmpty()) EMPTY_STRING else it.decrypt()
                } ?: EMPTY_STRING
            }
            val role = async {
                preferences[USER_ROLE_KEY]?.let {
                    if (it.isEmpty()) DEFAULT_ROLE else Role.valueOf(it.decrypt())
                } ?: DEFAULT_ROLE
            }
            val lastModifiedTimestamp = async {
                preferences[USER_LAST_MODIFIED_TIMESTAMP_KEY] ?: UNKNOWN_TIMESTAMP
            }

            User(id = id.await(),
                userName = userName.await(),
                password = password.await(),
                role = role.await(),
                lastModifiedTimestamp = lastModifiedTimestamp.await()).also {
                cachedUser = it
            }
        }
    }

    suspend fun isUserLoggedIn() = userFlow.first().isNotEmpty

    val user
        get() = run {
            if (cachedUser == null) {
                cachedUser = runBlocking(IO) { userFlow.first() }
            }
            cachedUser!!
        }

    val userIdFlow get() = dataFlow.map {
        it[USER_ID_KEY]?.decrypt() ?: EMPTY_STRING
    }

    suspend fun getUserId() = userIdFlow.first()

    suspend fun updateUserCredentials(user: User) {
        dataStore.edit { preferences ->
            cachedUser = user
            preferences[USER_ID_KEY] = if(user.id.isEmpty()) "" else user.id.encrypt()
            preferences[USER_NAME_KEY] = if(user.userName.isEmpty()) "" else user.userName.encrypt()
            preferences[USER_PASSWORD_KEY] = if(user.password.isEmpty()) "" else user.password.encrypt()
            preferences[USER_ROLE_KEY] = user.role.name.encrypt()
            preferences[USER_LAST_MODIFIED_TIMESTAMP_KEY] = user.lastModifiedTimestamp
        }
    }

    suspend fun updateUserRole(newRole: Role) {
        dataStore.edit { preferences ->
            cachedUser?.apply { role = newRole }
            preferences[USER_ROLE_KEY] = newRole.name.encrypt()
        }
    }
}