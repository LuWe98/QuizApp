package com.example.quizapp.model.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.dao.BaseDao
import com.example.quizapp.model.room.entities.sync.LocallyDeletedFilledQuestionnaire

@Dao
abstract class LocallyDeletedFilledQuestionnaireDao: BaseDao<LocallyDeletedFilledQuestionnaire> {

    @Query("SELECT * FROM deletedFilledQuestionnairesTable")
    abstract suspend fun getLocallyDeletedFilledQuestionnaireIds() : List<LocallyDeletedFilledQuestionnaire>

    @Query("DELETE FROM deletedFilledQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyDeletedFilledQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM deletedFilledQuestionnairesTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteLocallyDeletedFilledQuestionnairesWith(questionnaireIds: List<String>)

    @Query("DELETE FROM deletedFilledQuestionnairesTable")
    abstract suspend fun deleteAllLocallyDeletedFilledQuestionnaires()

}