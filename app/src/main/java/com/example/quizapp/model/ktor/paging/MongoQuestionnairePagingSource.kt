package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestionnaire

class MongoQuestionnairePagingSource(
    private val backendRepository: BackendRepository,
    private val searchQuery: String
) : BasicPagingSource<MongoQuestionnaire>(
    getRefreshKeyAction = { null },
    getDataAction = { page -> backendRepository.getPagedQuestionnaires(PagingConfigValues.PAGE_SIZE, page, searchQuery) }
)