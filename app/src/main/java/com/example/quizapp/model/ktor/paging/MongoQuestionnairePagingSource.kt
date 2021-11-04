package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.room.LocalRepository

class MongoQuestionnairePagingSource(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val searchQuery: String
) : BasicPagingSource<BrowsableQuestionnaire>(
    getDataAction = { page ->
        backendRepository.getPagedQuestionnaires(
            PagingConfigValues.PAGE_SIZE,
            page,
            searchQuery,
            localRepository.getAllQuestionnaireIds())
    }
)