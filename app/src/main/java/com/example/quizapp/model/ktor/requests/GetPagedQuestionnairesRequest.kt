package com.example.quizapp.model.ktor.requests

import com.example.quizapp.model.datastore.datawrappers.BrowsableOrderBy
import kotlinx.serialization.Serializable

@Serializable
data class GetPagedQuestionnairesRequest(
    val limit: Int,
    val page: Int,
    val searchString: String,
    val questionnaireIdsToIgnore: List<String>,
    val facultyIds: List<String>,
    val courseOfStudiesIds: List<String>,
    val authorIds: List<String>,
    val browsableOrderBy: BrowsableOrderBy,
    val ascending: Boolean
)
