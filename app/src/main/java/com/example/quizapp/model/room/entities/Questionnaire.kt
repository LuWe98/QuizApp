package com.example.quizapp.model.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.ktor.mongo.documents.questionnaire.AuthorInfo
import com.example.quizapp.model.ktor.requests.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Transient
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.QUESTIONARY_TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey var id: String = ObjectId().toString(),
    var title: String,
    @Embedded
    var authorInfo: AuthorInfo,
    var lastModifiedTimestamp: Long = getTimeMillis(),
    @Transient
    var syncStatus: SyncStatus = SyncStatus.UNSYNCED,
    var courseOfStudies: String,
    var faculty: String,
    var subject: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Questionnaire> { old, new -> old.id == new.id }
    }

    val asQuestionnaireIdWithTimeStamp get() = QuestionnaireIdWithTimestamp(id, lastModifiedTimestamp)

}