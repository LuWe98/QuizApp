package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.utils.Constants

@Dao
abstract class LocallyDeletedQuestionnaireDao: BaseDao<LocallyDeletedQuestionnaire>(Constants.LOCALLY_DELETED_QUESTIONNAIRES_TABLE) {

    @Query("SELECT * FROM deletedQuestionnairesTable")
    abstract suspend fun getLocallyDeletedQuestionnaireIds() : List<LocallyDeletedQuestionnaire>

    @Query("DELETE FROM deletedQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String)

}