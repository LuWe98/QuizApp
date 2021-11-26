package com.example.quizapp.model.ktor.paging

import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.databases.dto.BrowsableQuestionnaire
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.menus.SortBy

class MongoQuestionnairePagingSource(
    private val backendRepository: BackendRepository,
    private val localRepository: LocalRepository,
    private val searchQuery: String,
    private val facultyIds: List<String>,
    private val courseOfStudiesIds: List<String>,
    private val authorIds: List<String>,
    private val sortBy: SortBy
) : BasicPagingSource<BrowsableQuestionnaire>(
    getDataAction = { page ->
        backendRepository.getPagedQuestionnaires(
            limit = PagingConfigValues.PAGE_SIZE,
            page = page,
            searchString = searchQuery,
            questionnaireIdsToIgnore = localRepository.getAllQuestionnaireIds(),
            facultyIds = facultyIds,
            courseOfStudiesIds = courseOfStudiesIds,
            authorIds = authorIds,
            sortBy = sortBy
        )
    }
)