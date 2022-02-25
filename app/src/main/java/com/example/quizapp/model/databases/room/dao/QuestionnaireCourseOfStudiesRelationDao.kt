package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.entities.QuestionnaireCourseOfStudiesRelation

@Dao
abstract class QuestionnaireCourseOfStudiesRelationDao : BaseDao<QuestionnaireCourseOfStudiesRelation>(QuestionnaireCourseOfStudiesRelation.TABLE_NAME) {

    @Query("DELETE FROM questionnaireCourseOfStudiesRelationTable WHERE courseOfStudiesId = :courseOfStudiesId")
    abstract suspend fun deleteQuestionnaireCourseOfStudiesRelationWith(courseOfStudiesId: String)

}