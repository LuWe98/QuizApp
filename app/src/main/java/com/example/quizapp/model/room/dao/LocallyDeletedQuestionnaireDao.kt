package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.LocallyDeletedQuestionnaire

@Dao
abstract class LocallyDeletedQuestionnaireDao: BaseDao<LocallyDeletedQuestionnaire> {

    @Query("SELECT * FROM deletedQuestionnairesTable")
    abstract suspend fun getAllDeletedQuestionnaireIds() : List<LocallyDeletedQuestionnaire>

}