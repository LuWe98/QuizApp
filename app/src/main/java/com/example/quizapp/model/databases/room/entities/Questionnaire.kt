package com.example.quizapp.model.databases.room.entities

import androidx.room.*
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.QuestionnaireVisibility
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Transient
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = Questionnaire.TABLE_NAME)
@Parcelize
data class Questionnaire(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = ObjectId().toHexString(),
    @ColumnInfo(name = TITLE_COLUMN)
    val title: String,
    @Embedded
    val authorInfo: AuthorInfo,
    @ColumnInfo(name = SUBJECT_COLUMN)
    val subject: String,
    @Transient
    @ColumnInfo(name = SYNC_STATUS_COLUMN)
    val syncStatus: SyncStatus = SyncStatus.UNSYNCED,
    @ColumnInfo(name = VISIBILITY_COLUMN)
    val visibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    @ColumnInfo(name = LAST_MODIFIED_TIMESTAMP_COLUMN)
    val lastModifiedTimestamp: Long = getTimeMillis(),
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Questionnaire::id)

        const val TABLE_NAME = "questionnaireTable"

        const val ID_COLUMN = "questionnaireId"
        const val TITLE_COLUMN = "title"
        const val USER_ID_COLUMN = "userId"
        const val USER_NAME_COLUMN = "userName"
        const val SUBJECT_COLUMN = "subject"
        const val SYNC_STATUS_COLUMN = "syncStatus"
        const val VISIBILITY_COLUMN = "visibility"
        const val LAST_MODIFIED_TIMESTAMP_COLUMN = "lastModifiedTimestamp"

        const val UNKNOWN_QUESTIONNAIRE_ID = ""
    }

    val asQuestionnaireIdWithTimeStamp get() = QuestionnaireIdWithTimestamp(id, lastModifiedTimestamp)

    val timeStampAsDate get() = SimpleDateFormat.getDateInstance().format(Date(lastModifiedTimestamp)).toString()

}