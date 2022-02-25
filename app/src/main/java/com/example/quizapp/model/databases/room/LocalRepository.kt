package com.example.quizapp.model.databases.room

import com.example.quizapp.model.databases.dto.CourseOfStudiesIdWithTimeStamp
import com.example.quizapp.model.databases.dto.FacultyIdWithTimeStamp
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.entities.*
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.datastore.datawrappers.LocalQuestionnaireOrderBy
import kotlinx.coroutines.flow.Flow

interface LocalRepository {

    suspend fun <T : EntityMarker> insert(entity: T): Long?

    suspend fun <T : EntityMarker> insert(entities: Collection<T>): LongArray?

    suspend fun <T : EntityMarker> update(entity: T): Int?

    suspend fun <T : EntityMarker> update(entities: Collection<T>): Int?

    suspend fun <T : EntityMarker> delete(entity: T) : Unit?

    suspend fun <T : EntityMarker> delete(entities: Collection<T>): Unit?


    suspend fun insertCompleteQuestionnaire(completeQuestionnaire: CompleteQuestionnaire)

    suspend fun insertCompleteQuestionnaires(completeQuestionnaires: List<CompleteQuestionnaire>)

    suspend fun deleteAllUserData()

    suspend fun deleteQuestionnairesWith(questionnaireIds: List<String>)

    suspend fun deleteQuestionnaireWith(questionnaireId: String)

    suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String)

    suspend fun deleteFacultiesWith(facultyIds: List<String>)

    suspend fun deleteWhereAbbreviation(abb: String)

    suspend fun deleteCoursesOfStudiesWith(courseOfStudiesIds: List<String>)

    suspend fun deleteFacultyCourseOfStudiesRelationsWith(courseOfStudiesId: String)

    suspend fun setStatusToSynced(questionnaireIdsToSync: List<String>)

    suspend fun unsyncAllSyncingQuestionnaires()



    fun getAllLocalAuthorsFlow() : Flow<List<AuthorInfo>>

    fun getAllFacultiesFlow() : Flow<List<Faculty>>

    fun getAllCoursesOfStudiesFlow(): Flow<List<CourseOfStudies>>

    fun getAllCompleteQuestionnairesFlow() : Flow<List<CompleteQuestionnaire>>

    suspend fun findAllUnsyncedQuestionnaireIds() : List<String>

    suspend fun findAllSyncedQuestionnaires() : List<CompleteQuestionnaire>

    suspend fun findAllSyncingQuestionnaires() : List<Questionnaire>

    suspend fun getAllQuestionnaireIds() : List<String>

    suspend fun getLocallyDeletedQuestionnaireIds() : List<LocallyDeletedQuestionnaire>

    suspend fun getLocallyAnsweredCompleteQuestionnaires(): List<CompleteQuestionnaire>

    suspend fun getCourseOfStudiesIdsWithTimestamp() : List<CourseOfStudiesIdWithTimeStamp>

    suspend fun getFacultyIdsWithTimestamp() : List<FacultyIdWithTimeStamp>



    fun findCompleteQuestionnaireAsFlowWith(questionnaireId: String) : Flow<CompleteQuestionnaire>

    suspend fun findCompleteQuestionnaireWith(questionnaireId: String) : CompleteQuestionnaire?

    suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>) : List<CompleteQuestionnaire>

    suspend fun findCompleteQuestionnairesWith(questionnaireIds: List<String>, userId: String) : List<CompleteQuestionnaire>

    suspend fun findQuestionnaireWith(questionnaireId: String) : Questionnaire?

    suspend fun findQuestionnairesWith(questionnaireIds: List<String>) : List<Questionnaire>

    suspend fun getAuthorInfosWithName(searchQuery: String): List<AuthorInfo>

    suspend fun getAuthorInfosWithIds(authorIds: Set<String>): List<AuthorInfo>

    suspend fun isLocallyFilledQuestionnaireToUploadPresent(questionnaireId: String) : Boolean

    suspend fun getFacultiesWithIds(facultyIds: List<String>) : List<Faculty>

    fun findFacultiesWithNameFlow(nameToSearch: String) : Flow<List<Faculty>>

    suspend fun getCoursesOfStudiesWithIds(courseOfStudiesIds: Collection<String>) : List<CourseOfStudies>

    suspend fun getCoursesOfStudiesNameWithIds(courseOfStudiesIds: List<String>): List<String>

    suspend fun getCourseOfStudiesWithFaculties(courseOfStudiesId: String) : CourseOfStudiesWithFaculties

    fun getCoursesOfStudiesNotAssociatedWithFacultyFlow(searchQuery: String) : Flow<List<CourseOfStudies>>

    fun getCoursesOfStudiesAssociatedWithFacultyFlow(facultyId: String, searchQuery: String) : Flow<List<CourseOfStudies>>


    fun getFilteredCompleteQuestionnaireFlow(
        searchQuery: String,
        orderBy: LocalQuestionnaireOrderBy,
        ascending: Boolean,
        authorIds: Set<String>,
        cosIds: Set<String>,
        facultyIds: Set<String>,
        hideCompleted: Boolean
    ): Flow<List<CompleteQuestionnaire>>
}