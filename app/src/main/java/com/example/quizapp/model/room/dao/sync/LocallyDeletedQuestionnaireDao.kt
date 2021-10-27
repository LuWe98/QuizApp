package com.example.quizapp.model.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.dao.BaseDao
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.utils.Constants

@Dao
abstract class LocallyDeletedQuestionnaireDao: BaseDao<LocallyDeletedQuestionnaire>(Constants.LOCALLY_DELETED_QUESTIONNAIRES_TABLE) {

    @Query("SELECT * FROM deletedQuestionnairesTable")
    abstract suspend fun getLocallyDeletedQuestionnaireIds() : List<LocallyDeletedQuestionnaire>

    @Query("DELETE FROM deletedQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String)

}