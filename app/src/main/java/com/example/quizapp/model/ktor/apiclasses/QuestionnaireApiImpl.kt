package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.databases.mongodb.documents.MongoQuestionnaire
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire
import com.example.quizapp.model.datastore.datawrappers.BrowsableQuestionnaireOrderBy
import com.example.quizapp.model.ktor.ApiPaths.QuestionnairePaths
import com.example.quizapp.model.ktor.BackendRequest.*
import com.example.quizapp.model.ktor.BackendResponse.*
import com.example.quizapp.model.ktor.paging.BrowsableQuestionnairePageKeys
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireApiImpl @Inject constructor(
    private val client: HttpClient
) : QuestionnaireApi {

    override suspend fun insertQuestionnaires(mongoQuestionnaires: List<MongoQuestionnaire>): InsertQuestionnairesResponse =
        client.post(QuestionnairePaths.INSERT) {
            body = InsertQuestionnairesRequest(mongoQuestionnaires)
        }

    override suspend fun insertQuestionnaire(mongoQuestionnaires: MongoQuestionnaire) = insertQuestionnaires(listOf(mongoQuestionnaires))

    override suspend fun getQuestionnairesForSyncronization(
        syncedQuestionnaireIdsWithTimestamp: List<QuestionnaireIdWithTimestamp>,
        unsyncedQuestionnaireIds: List<String>,
        locallyDeletedQuestionnaire: List<LocallyDeletedQuestionnaire>
    ): SyncQuestionnairesResponse =
        client.post(QuestionnairePaths.SYNC) {
            body = SyncQuestionnairesRequest(
                syncedQuestionnaireIdsWithTimestamp,
                unsyncedQuestionnaireIds,
                locallyDeletedQuestionnaire.map(LocallyDeletedQuestionnaire::questionnaireId)
            )
        }

    override suspend fun deleteQuestionnaire(questionnaireIds: List<String>): DeleteQuestionnaireResponse =
        client.delete(QuestionnairePaths.DELETE) {
            body = DeleteQuestionnaireRequest(questionnaireIds)
        }

    override suspend fun getPagedQuestionnaires(
        lastPageKeys: BrowsableQuestionnairePageKeys,
        limit: Int,
        searchString: String,
        questionnaireIdsToIgnore: List<String>,
        facultyIds: List<String>,
        courseOfStudiesIds: List<String>,
        authorIds: List<String>,
        orderBy: BrowsableQuestionnaireOrderBy,
        ascending: Boolean
    ) : GetPagedQuestionnairesWithPageKeysResponse =
        client.post(QuestionnairePaths.PAGED){
            body = GetPagedQuestionnairesRequest(
                lastPageKeys = lastPageKeys,
                limit = limit,
                searchString = searchString,
                questionnaireIdsToIgnore = questionnaireIdsToIgnore,
                facultyIds = facultyIds,
                courseOfStudiesIds = courseOfStudiesIds,
                authorIds = authorIds,
                orderBy = orderBy,
                ascending = ascending
            )
        }

    override suspend fun downloadQuestionnaire(questionnaireId: String) : GetQuestionnaireResponse =
        client.post(QuestionnairePaths.DOWNLOAD) {
            body = GetQuestionnaireRequest(questionnaireId)
        }

    override suspend fun changeQuestionnaireVisibility(questionnaireId: String, newVisibility: QuestionnaireVisibility) : ChangeQuestionnaireVisibilityResponse =
        client.post(QuestionnairePaths.UPDATE_VISIBILITY){
            body = ChangeQuestionnaireVisibilityRequest(questionnaireId, newVisibility)
        }

    override suspend fun shareQuestionnaireWithUser(questionnaireId: String, userName: String, canEdit: Boolean) : ShareQuestionnaireWithUserResponse =
        client.post(QuestionnairePaths.SHARE) {
            body = ShareQuestionnaireWithUserRequest(questionnaireId, userName, canEdit)
        }
}