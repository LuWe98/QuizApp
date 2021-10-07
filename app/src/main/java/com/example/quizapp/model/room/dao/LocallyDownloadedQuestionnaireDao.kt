package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.LocallyDownloadedQuestionnaire

@Dao
abstract class LocallyDownloadedQuestionnaireDao : BaseDao<LocallyDownloadedQuestionnaire> {

    @Query("SELECT questionnaireId FROM downloadedQuestionnairesTable")
    abstract suspend fun getAllDownloadedQuestionnaireIds() : List<String>

}