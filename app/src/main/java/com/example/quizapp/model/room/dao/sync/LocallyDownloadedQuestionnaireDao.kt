package com.example.quizapp.model.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.dao.BaseDao
import com.example.quizapp.model.room.entities.sync.LocallyDownloadedQuestionnaire

@Dao
abstract class LocallyDownloadedQuestionnaireDao : BaseDao<LocallyDownloadedQuestionnaire> {

    @Query("SELECT questionnaireId FROM downloadedQuestionnairesTable")
    abstract suspend fun getAllDownloadedQuestionnaireIds() : List<String>

    @Query("DELETE FROM downloadedQuestionnairesTable")
    abstract suspend fun deleteAllLocallyDownloadedQuestionnaires()

}