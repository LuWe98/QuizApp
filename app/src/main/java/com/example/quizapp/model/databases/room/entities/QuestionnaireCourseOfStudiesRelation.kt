package com.example.quizapp.model.databases.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = QuestionnaireCourseOfStudiesRelation.TABLE_NAME,
    primaryKeys = [
        QuestionnaireCourseOfStudiesRelation.QUESTIONNAIRE_ID_COLUMN,
        QuestionnaireCourseOfStudiesRelation.COURSE_OF_STUDIES_ID_COLUMN

    ],
    foreignKeys = [
        ForeignKey(
            entity = Questionnaire::class,
            parentColumns = [Questionnaire.ID_COLUMN],
            childColumns = [QuestionnaireCourseOfStudiesRelation.QUESTIONNAIRE_ID_COLUMN],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = [QuestionnaireCourseOfStudiesRelation.QUESTIONNAIRE_ID_COLUMN]),
        Index(value = [QuestionnaireCourseOfStudiesRelation.COURSE_OF_STUDIES_ID_COLUMN])
    ]
)
@Parcelize
data class QuestionnaireCourseOfStudiesRelation(
    @ColumnInfo(name = QUESTIONNAIRE_ID_COLUMN)
    val questionnaireId: String,
    @ColumnInfo(name = COURSE_OF_STUDIES_ID_COLUMN)
    val courseOfStudiesId: String
) : EntityMarker {

    companion object {
        const val TABLE_NAME = "questionnaireCourseOfStudiesRelationTable"

        const val QUESTIONNAIRE_ID_COLUMN = "questionnaireId"
        const val COURSE_OF_STUDIES_ID_COLUMN = "courseOfStudiesId"
    }
}