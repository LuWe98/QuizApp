package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload

@Dao
abstract class LocallyFilledQuestionnaireToUploadDao: BaseDao<LocallyFilledQuestionnaireToUpload>(LocallyFilledQuestionnaireToUpload.TABLE_NAME) {

    @Query("SELECT * FROM locallyFilledQuestionnaireToUploadTable")
    abstract suspend fun getAllLocallyFilledQuestionnairesToUploadIds() : List<LocallyFilledQuestionnaireToUpload>

    @Query("DELETE FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun deleteLocallyFilledQuestionnaireToUploadWith(questionnaireId: String)

    @Query("DELETE FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId IN(:questionnaireIds)")
    abstract suspend fun deleteLocallyFilledQuestionnaireToUploadWith(questionnaireIds: List<String>)

    @Query("SELECT COUNT(*) FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId = :questionnaireId LIMIT 1")
    abstract suspend fun isLocallyFilledQuestionnaireToUploadPresent(questionnaireId: String) : Int

    @Query("SELECT * FROM locallyFilledQuestionnaireToUploadTable WHERE questionnaireId = :questionnaireId LIMIT 1")
    abstract suspend fun getLocallyFilledQuestionnaireToUploadId(questionnaireId: String) : LocallyFilledQuestionnaireToUpload?

}