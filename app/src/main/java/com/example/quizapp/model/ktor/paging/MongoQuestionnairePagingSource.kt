package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.MongoQuestionnaire

class MongoQuestionnairePagingSource(
    private val backendRepository: BackendRepository,
    private val searchQuery: String
) : BasicPagingSource<MongoQuestionnaire>(
    getDataAction = { page -> backendRepository.getPagedQuestionnaires(PagingConfigValues.PAGE_SIZE, page, searchQuery) }
)