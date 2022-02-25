package com.example.quizapp.model.databases.room.typeconverter

import androidx.room.TypeConverter
import com.example.quizapp.model.databases.properties.Degree
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.ktor.status.SyncStatus

class RoomTypeConverters {

    @TypeConverter
    fun toCourseOfStudiesDegree(degreeName: String) = Degree.valueOf(degreeName)

    @TypeConverter
    fun fromCourseOfStudiesDegree(degree: Degree) = degree.name


    @TypeConverter
    fun toSyncStatus(syncStatusString : String) = SyncStatus.valueOf(syncStatusString)

    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus) = syncStatus.name

    @TypeConverter
    fun toQuestionnaireVisibility(questionnaireVisibilityString: String) = QuestionnaireVisibility.valueOf(questionnaireVisibilityString)

    @TypeConverter
    fun fromQuestionnaireVisibility(questionnaireVisibility: QuestionnaireVisibility) = questionnaireVisibility.name

}