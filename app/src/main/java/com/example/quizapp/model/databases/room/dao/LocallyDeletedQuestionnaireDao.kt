package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.LocallyDeletedQuestionnaire

@Dao
abstract class LocallyDeletedQuestionnaireDao: BaseDao<LocallyDeletedQuestionnaire>(LocallyDeletedQuestionnaire.TABLE_NAME) {

    @Query("SELECT * FROM deletedQuestionnairesTable")
    abstract suspend fun getLocallyDeletedQuestionnaireIds() : List<LocallyDeletedQuestionnaire>

    @Query("DELETE FROM deletedQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyDeletedQuestionnaireWith(questionnaireId: String)

}