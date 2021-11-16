package com.example.quizapp.model.databases.room.entities.questionnaire

import androidx.room.*
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.dto.QuestionnaireIdWithTimestamp
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.QuestionnaireVisibility
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.DiffCallbackUtil
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Transient
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = Questionnaire.TABLE_NAME
)
@Parcelize
data class Questionnaire(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    var id: String = ObjectId().toString(),
    @ColumnInfo(name = TITLE_COLUMN)
    var title: String,
    @Embedded
    var authorInfo: AuthorInfo,
    @ColumnInfo(name = SUBJECT_COLUMN)
    var subject: String,
    @Transient
    @ColumnInfo(name = SYNC_STATUS_COLUMN)
    var syncStatus: SyncStatus = SyncStatus.UNSYNCED,
    @ColumnInfo(name = VISIBILITY_COLUMN)
    var visibility: QuestionnaireVisibility = QuestionnaireVisibility.PRIVATE,
    @ColumnInfo(name = LAST_MODIFIED_TIMESTAMP_COLUMN)
    var lastModifiedTimestamp: Long = getTimeMillis(),
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<Questionnaire> { old, new -> old.id == new.id }

        const val TABLE_NAME = "questionnaireTable"

        const val ID_COLUMN = "questionnaireId"
        const val TITLE_COLUMN = "title"
        const val USER_ID_COLUMN = "userId"
        const val USER_NAME_COLUMN = "userName"
        const val SUBJECT_COLUMN = "subject"
        const val SYNC_STATUS_COLUMN = "syncStatus"
        const val VISIBILITY_COLUMN = "visibility"
        const val LAST_MODIFIED_TIMESTAMP_COLUMN = "lastModifiedTimestamp"
    }

    val asQuestionnaireIdWithTimeStamp get() = QuestionnaireIdWithTimestamp(id, lastModifiedTimestamp)

    val timeStampAsDate get() = SimpleDateFormat.getDateInstance().format(Date(lastModifiedTimestamp)).toString()

}


/*
foreignKeys = [
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["facultyId"],
            childColumns = ["facultyId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CourseOfStudies::class,
            parentColumns = ["courseOfStudiesId"],
            childColumns = ["courseOfStudiesId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ],
*/