package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.datastore.datawrappers.BrowsableQuestionnaireOrderBy
import com.example.quizapp.model.ktor.BackendResponse.*
import com.example.quizapp.model.ktor.paging.BrowsableQuestionnairePageKeys

interface QuestionnaireApi {

    suspend fun insertQuestionnaires(mongoQuestionnaires: List<MongoQuestionnaire>): InsertQuestionnairesResponse

    suspend fun insertQuestionnaire(mongoQuestionnaires: MongoQuestionnaire): InsertQuestionnairesResponse

    suspend fun getQuestionnairesForSyncronization(
        syncedQuestionnaireIdsWithTimestamp: List<QuestionnaireIdWithTimestamp>,
        unsyncedQuestionnaireIds: List<String>,
        locallyDeletedQuestionnaire: List<LocallyDeletedQuestionnaire>
    ): SyncQuestionnairesResponse

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>): DeleteQuestionnaireResponse

    suspend fun getPagedQuestionnaires(
        lastPageKeys: BrowsableQuestionnairePageKeys,
        limit: Int,
        searchString: String,
        questionnaireIdsToIgnore: List<String>,
        facultyIds: List<String>,
        courseOfStudiesIds: List<String>,
        authorIds: List<String>,
        orderBy: BrowsableQuestionnaireOrderBy,
        ascending: Boolean
    ) : GetPagedQuestionnairesWithPageKeysResponse

    suspend fun downloadQuestionnaire(questionnaireId: String) : GetQuestionnaireResponse

    suspend fun changeQuestionnaireVisibility(questionnaireId: String, newVisibility: QuestionnaireVisibility) : ChangeQuestionnaireVisibilityResponse

    suspend fun shareQuestionnaireWithUser(questionnaireId: String, userName: String, canEdit: Boolean) : ShareQuestionnaireWithUserResponse

}