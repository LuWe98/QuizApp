package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.mongodb.documents.questionnaire.browsable.BrowsableMongoQuestionnaire
import com.example.quizapp.model.room.LocalRepository

class MongoQuestionnairePagingSource(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val searchQuery: String
) : BasicPagingSource<BrowsableMongoQuestionnaire>(
    getDataAction = { page ->
        backendRepository.getPagedQuestionnaires(
            PagingConfigValues.PAGE_SIZE,
            page,
            searchQuery,
            localRepository.getAllQuestionnaireIds())
    }
)