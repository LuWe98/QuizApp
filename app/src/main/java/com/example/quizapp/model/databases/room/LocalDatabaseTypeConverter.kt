package com.example.quizapp.model.databases.room

import androidx.room.TypeConverter
import com.example.quizapp.model.databases.Degree
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.mongodb.documents.questionnaire.QuestionnaireVisibility
import com.example.quizapp.model.databases.mongodb.documents.user.SharedWithInfo
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalDatabaseTypeConverter {

    @TypeConverter
    fun toSyncStatus(syncStatusString : String) = SyncStatus.valueOf(syncStatusString)

    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus) = syncStatus.name

    @TypeConverter
    fun toQuestionnaireVisibility(questionnaireVisibilityString: String) = QuestionnaireVisibility.valueOf(questionnaireVisibilityString)

    @TypeConverter
    fun fromQuestionnaireVisibility(questionnaireVisibility: QuestionnaireVisibility) = questionnaireVisibility.name

    @TypeConverter
    fun toCourseOfStudiesDegree(degreeName: String) = Degree.valueOf(degreeName)

    @TypeConverter
    fun fromCourseOfStudiesDegree(degree: Degree) = degree.name

    @TypeConverter
    fun fromShareWithInfoList(list: List<SharedWithInfo>) : String = Json.encodeToString(list)

    @TypeConverter
    fun toSharedWithInfoList(json: String) : List<SharedWithInfo> = Json.decodeFromString(json)
}