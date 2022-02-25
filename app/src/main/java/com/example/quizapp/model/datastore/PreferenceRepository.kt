package com.example.quizapp.model.datastore

import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.properties.Role
import com.example.quizapp.model.datastore.datawrappers.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.sql.Timestamp
import kotlin.random.Random

interface PreferenceRepository {

    val themeFlow: Flow<QuizAppTheme>

    val languageFlow: Flow<QuizAppLanguage>

    val shuffleTypeFlow: Flow<QuestionnaireShuffleType>

    val shuffleSeedFlow: Flow<Long>

    val browsableOrderByFlow: Flow<BrowsableQuestionnaireOrderBy>

    val browsableAscendingOrderFlow: Flow<Boolean>

    val browsableCosIdsFlow: Flow<Set<String>>

    val browsableFacultyIdsFlow: Flow<Set<String>>

    val localOrderByFlow: Flow<LocalQuestionnaireOrderBy>

    val localAscendingOrderFlow: Flow<Boolean>

    val localFilteredAuthorIdsFlow: Flow<Set<String>>

    val localFilterHideCompletedFlow: Flow<Boolean>

    val localFilteredCosIdsFlow: Flow<Set<String>>

    val localFilteredFacultyIdsFlow: Flow<Set<String>>

    val manageUsersOrderByFlow: Flow<ManageUsersOrderBy>

    val manageUsersAscendingOrderFlow: Flow<Boolean>

    val preferredCourseOfStudiesIdFlow: Flow<Set<String>>

    val usePreferredCourseOfStudiesForSearchFlow: Flow<Boolean>

    val jwtTokenFlow: Flow<String?>

    val userIdFlow: Flow<String>

    val userNameFlow: Flow<String>

    val userPasswordFlow: Flow<String>

    val userRoleFlow: Flow<Role>

    val userLastModifiedTimestampFlow: Flow<Long>

    val userCanShareQuestionnaireWith: Flow<Boolean>

    val userFlow: Flow<User>


    suspend fun clearPreferenceDataOnLogout()

    suspend fun wipePreferenceData()


    suspend fun updateLanguage(quizAppLanguage: QuizAppLanguage)

    suspend fun updateShuffleType(shuffleType: QuestionnaireShuffleType)

    suspend fun updateShuffleSeed(newSeed: Long = Random.nextLong(Long.MAX_VALUE))

    suspend fun updateLocalFilteredAuthorIds(authorIds: Collection<String>)

    suspend fun updateLocalFilteredCoursesOfStudiesIds(cosIds: Collection<String>)

    suspend fun updateLocalFilteredFacultyIds(facultyIds: Collection<String>)

    suspend fun updateRemoteFilteredCoursesOfStudiesIds(cosIds: Collection<String>)

    suspend fun updateRemoteFilteredFacultyIds(facultyIds: Collection<String>)

    suspend fun updateLocalFilters(
        localOrderBy: LocalQuestionnaireOrderBy,
        ascending: Boolean,
        authorIds: Collection<String>,
        cosIds: Collection<String>,
        facultyIds: Collection<String>,
        hideCompleted: Boolean
    )

    suspend fun updateRemoteFilters(
        remoteQuestionnaireOrderBy: BrowsableQuestionnaireOrderBy,
        ascending: Boolean,
        cosIds: Collection<String>,
        facultyIds: Collection<String>
    )

    suspend fun updateTheme(newTheme: QuizAppTheme)

    suspend fun updateManageUsersOrderBy(manageUsersOrderBy: ManageUsersOrderBy)

    suspend fun updateManageUsersAscendingOrder(ascending: Boolean)

    suspend fun updatePreferredCourseOfStudiesIds(courseOfStudiesIds: List<String>)

    suspend fun updateUsePreferredCosForSearch(newValue: Boolean)

    suspend fun updateJwtToken(token: String?)

    suspend fun updateUserRole(newRole: Role)

    suspend fun updateUserLastModifiedTimeStamp(timestamp: Long)

    suspend fun updateUserCanShareQuestionnaireWith(canShare: Boolean)

    suspend fun updateUserPassword(newPassword: String)

    suspend fun updateUserCredentials(user: User)

    suspend fun getTheme() = themeFlow.first()

    suspend fun getLanguage() = languageFlow.first()

    suspend fun getShuffleType() = shuffleTypeFlow.first()

    suspend fun getShuffleSeed() = shuffleSeedFlow.first()

    suspend fun getBrowsableOrderBy() = browsableOrderByFlow.first()

    suspend fun getBrowsableAscendingOrder() = browsableAscendingOrderFlow.first()

    suspend fun getBrowsableFilteredCosIds() = browsableCosIdsFlow.first()

    suspend fun getBrowsableFilteredFacultyIds() = browsableFacultyIdsFlow.first()

    suspend fun getLocalQuestionnaireOrderBy() = localOrderByFlow.first()

    suspend fun getLocalAscendingOrder() = localAscendingOrderFlow.first()

    suspend fun getLocalFilteredAuthorIds() = localFilteredAuthorIdsFlow.first()

    suspend fun getLocalFilterHideCompleted() = localFilterHideCompletedFlow.first()

    suspend fun getLocalFilteredCosIds() = localFilteredCosIdsFlow.first()

    suspend fun getLocalFilteredFacultyIds() = localFilteredFacultyIdsFlow.first()

    suspend fun getManageUsersOrderBy() = manageUsersOrderByFlow.first()

    suspend fun getManageUsersAscendingOrder() = manageUsersAscendingOrderFlow.first()

    suspend fun getPreferredCourseOfStudiesId() = preferredCourseOfStudiesIdFlow.first()

    suspend fun usePreferredCourseOfStudiesForSearch() = usePreferredCourseOfStudiesForSearchFlow.first()

    suspend fun getJwtToken() = jwtTokenFlow.first()?.ifEmpty { null }

    suspend fun getUserId() = userIdFlow.first()

    suspend fun getUserName() = userNameFlow.first()

    suspend fun getUserPassword() = userPasswordFlow.first()

    suspend fun getOwnAuthorInfo() = userFlow.first().asAuthorInfo

    suspend fun isUserLoggedIn() = userFlow.first().isNotEmpty

}