package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyAnsweredQuestionnaire

@Dao
abstract class LocallyAnsweredQuestionnaireDao: BaseDao<LocallyAnsweredQuestionnaire>(LocallyAnsweredQuestionnaire.TABLE_NAME) {

    @Query("SELECT * FROM locallyAnsweredQuestionnairesTable")
    abstract suspend fun getLocallyAnsweredQuestionnaireIds() : List<LocallyAnsweredQuestionnaire>

    @Query("DELETE FROM locallyAnsweredQuestionnairesTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyAnsweredQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM locallyAnsweredQuestionnairesTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteLocallyAnsweredQuestionnaireWith(questionnaireIds: List<String>)

    @Query("SELECT COUNT(*) FROM locallyAnsweredQuestionnairesTable WHERE questionnaireId = :questionnaireId LIMIT 1")
    abstract suspend fun isAnsweredQuestionnairePresent(questionnaireId: String) : Int

    @Query("SELECT * FROM locallyAnsweredQuestionnairesTable WHERE questionnaireId = :questionnaireId LIMIT 1")
    abstract suspend fun getLocallyAnsweredQuestionnaire(questionnaireId: String) : LocallyAnsweredQuestionnaire?

}