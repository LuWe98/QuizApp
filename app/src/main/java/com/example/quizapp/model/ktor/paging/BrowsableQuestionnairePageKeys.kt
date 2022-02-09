package com.example.quizapp.model.ktor.paging

import kotlinx.serialization.Serializable

@Serializable
data class BrowsableQuestionnairePageKeys(
    val id: String = "",
    val title: String = "",
    val timeStamp: Long = 0
)