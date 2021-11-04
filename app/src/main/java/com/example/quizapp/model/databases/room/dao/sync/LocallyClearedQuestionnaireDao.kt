package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyClearedQuestionnaire

@Dao
abstract class LocallyClearedQuestionnaireDao: BaseDao<LocallyClearedQuestionnaire>(LocallyClearedQuestionnaire.TABLE_NAME) {

    @Query("SELECT * FROM deletedFilledQuestionnairesTable")
    abstract suspend fun getLocallyDeletedFilledQuestionnaireIds() : List<LocallyClearedQuestionnaire>

    @Query("DELETE FROM deletedFilledQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyDeletedFilledQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM deletedFilledQuestionnairesTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteLocallyDeletedFilledQuestionnairesWith(questionnaireIds: List<String>)

}