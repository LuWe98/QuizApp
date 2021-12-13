package com.example.quizapp.model.ktor.responses

import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import kotlinx.serialization.Serializable

@Serializable
data class SyncFacultiesResponse(
    val facultiesToInsert: List<MongoFaculty>,
    val facultiesToUpdate: List<MongoFaculty>,
    val facultyIdsToDelete: List<String>
) {

    fun isEmpty() = facultiesToInsert.isEmpty() && facultiesToUpdate.isEmpty() && facultyIdsToDelete.isEmpty()

}
