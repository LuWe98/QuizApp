package com.example.quizapp.model.ktor.apiclasses

import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.MongoQuestionnaire
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.ktor.requests.*
import com.example.quizapp.model.ktor.responses.*
import com.example.quizapp.model.ktor.ApiPaths.QuestionnairePaths
import com.example.quizapp.model.datastore.datawrappers.BrowsableOrderBy
import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionnaireApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun insertQuestionnaires(mongoQuestionnaires: List<MongoQuestionnaire>): InsertQuestionnairesResponse =
        client.post(QuestionnairePaths.INSERT) {
            body = InsertQuestionnairesRequest(mongoQuestionnaires)
        }

    suspend fun getQuestionnairesForSyncronization(
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

    suspend fun deleteQuestionnaire(questionnaireIds: List<String>): DeleteQuestionnaireResponse =
        client.delete(QuestionnairePaths.DELETE) {
            body = DeleteQuestionnaireRequest(questionnaireIds)
        }

    //TODO -> RÜCKGABEWERT ZU RESPONSE SEALED CLASS UMÄNDERN!
    suspend fun getPagedQuestionnaires(
        limit: Int,
        page: Int,
        searchString: String,
        questionnaireIdsToIgnore: List<String>,
        facultyIds: List<String>,
        courseOfStudiesIds: List<String>,
        authorIds: List<String>,
        browsableOrderBy: BrowsableOrderBy,
        ascending: Boolean
    ) : List<BrowsableQuestionnaire> =
        client.post(QuestionnairePaths.PAGED){
            body = GetPagedQuestionnairesRequest(
                limit = limit,
                page = page,
                searchString = searchString,
                questionnaireIdsToIgnore = questionnaireIdsToIgnore,
                facultyIds = facultyIds,
                courseOfStudiesIds = courseOfStudiesIds,
                authorIds = authorIds,
                browsableOrderBy = browsableOrderBy,
                ascending = ascending
            )
        }

    suspend fun downloadQuestionnaire(questionnaireId: String) : GetQuestionnaireResponse =
        client.post(QuestionnairePaths.DOWNLOAD) {
            body = GetQuestionnaireRequest(questionnaireId)
        }

    suspend fun changeQuestionnaireVisibility(questionnaireId: String, newVisibility: QuestionnaireVisibility) : ChangeQuestionnaireVisibilityResponse =
        client.post(QuestionnairePaths.UPDATE_VISIBILITY){
            body = ChangeQuestionnaireVisibilityRequest(questionnaireId, newVisibility)
        }

    suspend fun shareQuestionnaireWithUser(questionnaireId: String, userName: String, canEdit: Boolean) : ShareQuestionnaireWithUserResponse =
        client.post(QuestionnairePaths.SHARE) {
            body = ShareQuestionnaireWithUserRequest(questionnaireId, userName, canEdit)
        }
}