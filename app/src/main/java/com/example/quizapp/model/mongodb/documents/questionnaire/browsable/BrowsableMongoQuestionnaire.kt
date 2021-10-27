package com.example.quizapp.model.mongodb.documents.questionnaire.browsable

import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.mongodb.documents.questionnaire.MongoQuestion
import com.example.quizapp.model.mongodb.documents.user.AuthorInfo
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.serialization.Serializable

/**
 * This Entity is a lightweight version of the regular MongoQuestionnaire in order to have less size
 */
@Serializable
data class BrowsableMongoQuestionnaire(
    val questionnaireId: String,
    val title: String,
    val authorInfo: AuthorInfo,
    val faculty: String,
    val courseOfStudies: String,
    val subject: String,
    val questionCount: Int,
    val questionsPreview: List<MongoQuestion>,
    val lastModifiedTimestamp: Long,
    var downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED
) {
    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<BrowsableMongoQuestionnaire> { old, new -> old.questionnaireId == new.questionnaireId }
    }
}