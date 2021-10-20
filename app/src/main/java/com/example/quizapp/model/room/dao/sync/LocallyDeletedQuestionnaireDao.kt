package com.example.quizapp.model.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.dao.BaseDao
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire

@Dao
abstract class LocallyDeletedQuestionnaireDao: BaseDao<LocallyDeletedQuestionnaire> {

    @Query("SELECT * FROM deletedQuestionnairesTable")
    abstract suspend fun getLocallyDeletedQuestionnaireIds() : List<LocallyDeletedQuestionnaire>

    @Query("DELETE FROM deletedQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM deletedQuestionnairesTable")
    abstract suspend fun deleteAllLocallyDeletedQuestionnaires()

}