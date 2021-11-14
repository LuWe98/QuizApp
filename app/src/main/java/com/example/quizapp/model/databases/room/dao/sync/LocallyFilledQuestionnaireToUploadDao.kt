package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload

@Dao
abstract class LocallyFilledQuestionnaireToUploadDao: BaseDao<LocallyFilledQuestionnaireToUpload>(LocallyFilledQuestionnaireToUpload.TABLE_NAME) {

    @Query("SELECT * FROM locallyFilledQuestionnaireToUploadTable")
    abstract suspend fun getLocallyAnsweredQuestionnaireIds() : List<LocallyFilledQuestionnaireToUpload>

    @Query("DELETE FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyAnsweredQuestionnaireWith(questionnaireId: String)

    @Query("DELETE FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteLocallyAnsweredQuestionnaireWith(questionnaireIds: List<String>)

    @Query("SELECT COUNT(*) FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId = :questionnaireId LIMIT 1")
    abstract suspend fun isAnsweredQuestionnairePresent(questionnaireId: String) : Int

    @Query("SELECT * FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId = :questionnaireId LIMIT 1")
    abstract suspend fun getLocallyAnsweredQuestionnaire(questionnaireId: String) : LocallyFilledQuestionnaireToUpload?

}