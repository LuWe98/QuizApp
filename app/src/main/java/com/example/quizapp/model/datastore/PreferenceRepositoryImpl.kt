package com.example.quizapp.model.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.quizapp.extensions.combine
import com.example.quizapp.extensions.dataflow
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.datastore.datawrappers.*
import com.example.quizapp.model.ktor.client.KtorClientAuth
import com.example.quizapp.model.ktor.client.claimAsBoolean
import com.example.quizapp.model.ktor.client.claimAsString
import com.example.quizapp.utils.EncryptionUtil.decrypt
import com.example.quizapp.utils.EncryptionUtil.encrypt
import io.ktor.util.date.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferenceRepository {

    private val dataFlow = dataStore.dataflow

    private suspend fun <T> Preferences.Key<T>.update(newValue: T) {
        dataStore.edit { preferences ->
            preferences[this] = newValue
        }
    }

    private fun <T> Preferences.Key<T>.mapTo() {

    }

    override val themeFlow = dataFlow.map { preferences ->
        preferences[THEME_KEY]?.let(QuizAppTheme::valueOf) ?: QuizAppTheme.LIGHT
    }

    override val languageFlow = dataFlow.map { preferences ->
        preferences[LANGUAGE_KEY]?.let(QuizAppLanguage::valueOf) ?: QuizAppLanguage.ENGLISH
    }

    override val shuffleTypeFlow = dataFlow.map { preferences ->
        preferences[SHUFFLE_TYPE_KEY]?.let(QuestionnaireShuffleType::valueOf) ?: QuestionnaireShuffleType.NONE
    }

    override val shuffleSeedFlow = dataFlow.map { preferences ->
        preferences[SHUFFLE_SEED_KEY] ?: getTimeMillis()
    }

    override val browsableOrderByFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_ORDER_BY_KEY]?.let(BrowsableQuestionnaireOrderBy::valueOf) ?: BrowsableQuestionnaireOrderBy.TITLE
    }

    override val browsableAscendingOrderFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_ASCENDING_ORDER_KEY] ?: true
    }

    override val browsableCosIdsFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS] ?: preferredCourseOfStudiesIdFlow.first()
    }

    override val browsableFacultyIdsFlow = dataFlow.map { preferences ->
        preferences[BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS] ?: emptySet()
    }

    override val localOrderByFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_ORDER_BY_KEY]?.let(LocalQuestionnaireOrderBy::valueOf) ?: LocalQuestionnaireOrderBy.TITLE
    }

    override val localAscendingOrderFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY] ?: true
    }

    override val localFilteredAuthorIdsFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] ?: emptySet()
    }

    override val localFilterHideCompletedFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED] ?: false
    }

    override val localFilteredCosIdsFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_COS_IDS] ?: emptySet()
    }

    override val localFilteredFacultyIdsFlow = dataFlow.map { preferences ->
        preferences[LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS] ?: emptySet()
    }

    override val manageUsersOrderByFlow = dataFlow.map { preferences ->
        preferences[MANAGE_USER_ORDER_BY_KEY]?.let(ManageUsersOrderBy::valueOf) ?: ManageUsersOrderBy.USER_NAME
    }

    override val preferredCourseOfStudiesIdFlow = dataFlow.map { preferences ->
        preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] ?: emptySet()
    }

    override val usePreferredCourseOfStudiesForSearchFlow = dataFlow.map { preferences ->
        preferences[PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH] ?: false
    }

    override val manageUsersAscendingOrderFlow = dataFlow.map { preferences ->
        preferences[MANAGE_USER_ASCENDING_ORDER_KEY] ?: true
    }

    override val jwtTokenFlow = dataFlow.map { preferences ->
        preferences[JWT_TOKEN_KEY]
    }

    override val userIdFlow = dataFlow.map { preferences ->
        preferences[USER_ID_KEY]?.let {
            if (it.isEmpty()) EMPTY_STRING else it.decrypt()
        } ?: EMPTY_STRING
    }.distinctUntilChanged()

    override val userNameFlow = dataFlow.map { preferences ->
        preferences[USER_NAME_KEY]?.let {
            if (it.isEmpty()) EMPTY_STRING else it.decrypt()
        } ?: EMPTY_STRING
    }.distinctUntilChanged()

    override val userPasswordFlow = dataFlow.map { preferences ->
        preferences[USER_PASSWORD_KEY]?.let {
            if (it.isEmpty()) EMPTY_STRING else it.decrypt()
        } ?: EMPTY_STRING
    }.distinctUntilChanged()

    override val userRoleFlow = dataFlow.map { preferences ->
        preferences[USER_ROLE_KEY]?.let {
            if (it.isEmpty()) Role.USER else Role.valueOf(it.decrypt())
        } ?: Role.USER
    }.distinctUntilChanged()

    override val userLastModifiedTimestampFlow = dataFlow.map { preferences ->
        preferences[USER_LAST_MODIFIED_TIMESTAMP_KEY] ?: UNKNOWN_TIMESTAMP
    }.distinctUntilChanged()

    override val userCanShareQuestionnaireWith = dataFlow.map { preferences ->
        preferences[USER_CAN_SHARE_QUESTIONNAIRES_WITH_KEY] ?: false
    }.distinctUntilChanged()


    override val userFlow = combine(
        userIdFlow,
        userNameFlow,
        userPasswordFlow,
        userRoleFlow,
        userLastModifiedTimestampFlow,
        userCanShareQuestionnaireWith
    ) { id, name, password, role, timestamp, canShareQuestionnaireWith ->
        User(
            id = id,
            name = name,
            password = password,
            role = role,
            lastModifiedTimestamp = timestamp,
            canShareQuestionnairesWith = canShareQuestionnaireWith
        )
    }.distinctUntilChanged()


    override suspend fun clearPreferenceDataOnLogout() {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = EMPTY_STRING
            preferences[USER_PASSWORD_KEY] = EMPTY_STRING
            preferences[USER_ROLE_KEY] = EMPTY_STRING
            preferences[JWT_TOKEN_KEY] = EMPTY_STRING
        }
    }

    override suspend fun wipePreferenceData() {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = EMPTY_STRING
            preferences[USER_PASSWORD_KEY] = EMPTY_STRING
            preferences[USER_ROLE_KEY] = EMPTY_STRING
            preferences[JWT_TOKEN_KEY] = EMPTY_STRING
            preferences[LANGUAGE_KEY] = QuizAppLanguage.ENGLISH.name
            preferences[THEME_KEY] = QuizAppTheme.LIGHT.name
            preferences[SHUFFLE_TYPE_KEY] = QuestionnaireShuffleType.NONE.name
            preferences[BROWSABLE_ORDER_BY_KEY] = BrowsableQuestionnaireOrderBy.TITLE.name
            preferences[BROWSABLE_ASCENDING_ORDER_KEY] = true
            preferences[MANAGE_USER_ORDER_BY_KEY] = ManageUsersOrderBy.USER_NAME.name
            preferences[MANAGE_USER_ASCENDING_ORDER_KEY] = true
            preferences[LOCAL_QUESTIONNAIRE_ORDER_BY_KEY] = LocalQuestionnaireOrderBy.TITLE.name
            preferences[LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY] = true
            preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] = emptySet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_COS_IDS] = emptySet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS] = emptySet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED] = false
            preferences[PREFERRED_COURSE_OF_STUDIES_ID_KEY] = emptySet()
            preferences[PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH] = true
        }
    }

    override suspend fun updateTheme(newTheme: QuizAppTheme) = THEME_KEY.update(newTheme.name)

    override suspend fun updateLanguage(quizAppLanguage: QuizAppLanguage) = LANGUAGE_KEY.update(quizAppLanguage.name)

    override suspend fun updateShuffleType(shuffleType: QuestionnaireShuffleType) = SHUFFLE_TYPE_KEY.update(shuffleType.name)

    override suspend fun updateShuffleSeed(newSeed: Long) = SHUFFLE_SEED_KEY.update(newSeed)

    override suspend fun updateLocalFilteredAuthorIds(authorIds: Collection<String>) = LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS.update(authorIds.toSet())

    override suspend fun updateLocalFilteredCoursesOfStudiesIds(cosIds: Collection<String>) = LOCAL_QUESTIONNAIRE_FILTER_COS_IDS.update(cosIds.toSet())

    override suspend fun updateLocalFilteredFacultyIds(facultyIds: Collection<String>) = LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS.update(facultyIds.toSet())

    override suspend fun updateRemoteFilteredCoursesOfStudiesIds(cosIds: Collection<String>) = BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS.update(cosIds.toSet())

    override suspend fun updateRemoteFilteredFacultyIds(facultyIds: Collection<String>) = BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS.update(facultyIds.toSet())

    override suspend fun updateRemoteFilters(
        remoteQuestionnaireOrderBy: BrowsableQuestionnaireOrderBy,
        ascending: Boolean,
        cosIds: Collection<String>,
        facultyIds: Collection<String>
    ) {
        dataStore.edit { preferences ->
            preferences[BROWSABLE_ORDER_BY_KEY] = remoteQuestionnaireOrderBy.name
            preferences[BROWSABLE_ASCENDING_ORDER_KEY] = ascending
            preferences[BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS] = cosIds.toSet()
            preferences[BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS] = facultyIds.toSet()
        }
    }

    override suspend fun updateLocalFilters(
        localOrderBy: LocalQuestionnaireOrderBy,
        ascending: Boolean,
        authorIds: Collection<String>,
        cosIds: Collection<String>,
        facultyIds: Collection<String>,
        hideCompleted: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[LOCAL_QUESTIONNAIRE_ORDER_BY_KEY] = localOrderBy.name
            preferences[LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY] = ascending
            preferences[LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS] = authorIds.toSet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_COS_IDS] = cosIds.toSet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS] = facultyIds.toSet()
            preferences[LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED] = hideCompleted
        }
    }


    override suspend fun updateManageUsersOrderBy(manageUsersOrderBy: ManageUsersOrderBy) = MANAGE_USER_ORDER_BY_KEY.update(manageUsersOrderBy.name)

    override suspend fun updateManageUsersAscendingOrder(ascending: Boolean) = MANAGE_USER_ASCENDING_ORDER_KEY.update(ascending)

    override suspend fun updatePreferredCourseOfStudiesIds(courseOfStudiesIds: List<String>) = PREFERRED_COURSE_OF_STUDIES_ID_KEY.update(courseOfStudiesIds.toSet())

    override suspend fun updateUsePreferredCosForSearch(newValue: Boolean) = PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH.update(newValue)

    override suspend fun updateJwtToken(token: String?) {
        dataStore.edit { preferences ->
            preferences[JWT_TOKEN_KEY] = token ?: EMPTY_STRING
            token?.let {
                preferences[USER_ROLE_KEY] = token.claimAsString(KtorClientAuth.CLAIM_USER_ROLE).encrypt()
                preferences[USER_CAN_SHARE_QUESTIONNAIRES_WITH_KEY] = token.claimAsBoolean(KtorClientAuth.CLAIM_CAN_SHARE_QUESTIONNAIRE_WITH)
            }
        }
    }

    override suspend fun updateUserRole(newRole: Role) = USER_ROLE_KEY.update(newRole.name.encrypt())

    override suspend fun updateUserLastModifiedTimeStamp(timestamp: Long) = USER_LAST_MODIFIED_TIMESTAMP_KEY.update(timestamp)

    override suspend fun updateUserCanShareQuestionnaireWith(canShare: Boolean) = USER_CAN_SHARE_QUESTIONNAIRES_WITH_KEY.update(canShare)

    override suspend fun updateUserPassword(newPassword: String) = USER_PASSWORD_KEY.update(newPassword.encrypt())

    override suspend fun updateUserCredentials(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = if (user.id.isEmpty()) EMPTY_STRING else user.id.encrypt()
            preferences[USER_NAME_KEY] = if (user.name.isEmpty()) EMPTY_STRING else user.name.encrypt()
            preferences[USER_PASSWORD_KEY] = if (user.password.isEmpty()) EMPTY_STRING else user.password.encrypt()
            preferences[USER_ROLE_KEY] = user.role.name.encrypt()
            preferences[USER_LAST_MODIFIED_TIMESTAMP_KEY] = user.lastModifiedTimestamp
        }
    }

    companion object PreferencesKeys {
        val LANGUAGE_KEY = stringPreferencesKey("languageKey")
        private val THEME_KEY = stringPreferencesKey("themeKey")
        private val SHUFFLE_TYPE_KEY = stringPreferencesKey("shuffleTypePreference")
        private val SHUFFLE_SEED_KEY = longPreferencesKey("shuffleSeedKey")

        private val MANAGE_USER_ORDER_BY_KEY = stringPreferencesKey("userOrderByKey")
        private val MANAGE_USER_ASCENDING_ORDER_KEY = booleanPreferencesKey("userOrderAscendingKey")

        private val BROWSABLE_ORDER_BY_KEY = stringPreferencesKey("sortByPreferenceKey")
        private val BROWSABLE_ASCENDING_ORDER_KEY = booleanPreferencesKey("browsableAscendingKey")
        private val BROWSABLE_QUESTIONNAIRE_FILTER_COS_IDS = stringSetPreferencesKey("browsableQuestionnairesFilterCosIdsKey")
        private val BROWSABLE_QUESTIONNAIRE_FILTER_FACULTY_IDS = stringSetPreferencesKey("browsableQuestionnairesFilterFacultyIdsKey")

        private val LOCAL_QUESTIONNAIRE_ORDER_BY_KEY = stringPreferencesKey("localQuestionnaireOrderByKey")
        private val LOCAL_QUESTIONNAIRE_ASCENDING_ORDER_KEY = booleanPreferencesKey("localQuestionnaireAscendingOrderKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_AUTHOR_IDS = stringSetPreferencesKey("localQuestionnairesFilterAuthorIdsKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_COS_IDS = stringSetPreferencesKey("localQuestionnairesFilterCosIdsKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_FACULTY_IDS = stringSetPreferencesKey("localQuestionnairesFilterFacultyIdsKey")
        private val LOCAL_QUESTIONNAIRE_FILTER_HIDE_COMPLETED = booleanPreferencesKey("localQuestionnairesFilterHideCompletedKey")

        private val PREFERRED_COURSE_OF_STUDIES_ID_KEY = stringSetPreferencesKey("preferredCosKey")
        private val PREFERRED_COURSE_OF_STUDIES_USE_FOR_QUESTIONNAIRE_SEARCH = booleanPreferencesKey("preferredCosFprQuestionnaireKey")

        private val JWT_TOKEN_KEY = stringPreferencesKey("jwtTokenKey")
        private val USER_ID_KEY = stringPreferencesKey("userIdKey")
        private val USER_NAME_KEY = stringPreferencesKey("userNameKey")
        private val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
        private val USER_ROLE_KEY = stringPreferencesKey("userRoleKey")
        private val USER_LAST_MODIFIED_TIMESTAMP_KEY = longPreferencesKey("userLastModifiedTimeStampKey")
        private val USER_CAN_SHARE_QUESTIONNAIRES_WITH_KEY = booleanPreferencesKey("userCanShareQuestionnaireWithKey")

        const val EMPTY_STRING = ""
        const val UNKNOWN_TIMESTAMP = -1L
    }
}