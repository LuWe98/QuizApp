package com.example.quizapp.model.databases.room.typeconverter

import androidx.room.TypeConverter
import com.example.quizapp.model.databases.properties.Degree
import com.example.quizapp.model.databases.properties.QuestionType
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.properties.SharedWithInfo
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
    fun toQuestionType(questionTypeName: String) = QuestionType.valueOf(questionTypeName)

    @TypeConverter
    fun fromQuestionType(questionType: QuestionType) = questionType.name



    @TypeConverter
    fun fromShareWithInfoList(list: List<SharedWithInfo>) : String = Json.encodeToString(list)

    @TypeConverter
    fun toSharedWithInfoList(json: String) : List<SharedWithInfo> = Json.decodeFromString(json)

}