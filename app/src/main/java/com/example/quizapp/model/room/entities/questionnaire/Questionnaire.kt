package com.example.quizapp.model.room.entities.questionnaire

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.mongodb.documents.questionnaire.QuestionnaireVisibility
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Transient
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = Constants.QUESTIONNAIRE_TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey var id: String = ObjectId().toString(),
    var title: String,
    @Embedded var authorInfo: AuthorInfo,
    var courseOfStudies: String,
    var faculty: String,
    var subject: String,
    @Transient var syncStatus: SyncStatus = SyncStatus.UNSYNCED,
    var questionnaireVisibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    var lastModifiedTimestamp: Long = getTimeMillis(),
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Questionnaire> { old, new -> old.id == new.id }
    }

    val asQuestionnaireIdWithTimeStamp get() = QuestionnaireIdWithTimestamp(id, lastModifiedTimestamp)

    val timeStampAsDate get() = SimpleDateFormat.getDateInstance().format(Date(lastModifiedTimestamp)).toString()

}