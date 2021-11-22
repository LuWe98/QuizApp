package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.entities.relations.QuestionnaireCourseOfStudiesRelation

@Dao
abstract class QuestionnaireCourseOfStudiesRelationDao : BaseDao<QuestionnaireCourseOfStudiesRelation>(QuestionnaireCourseOfStudiesRelation.TABLE_NAME) {

    @Query("SELECT * FROM questionnaireCourseOfStudiesRelationTable WHERE questionnaireId = :questionnaireId")
    abstract suspend fun getQuestionnaireCourseOfStudiesRelationWith(questionnaireId: String) : List<QuestionnaireCourseOfStudiesRelation>

    @Query("SELECT * FROM questionnaireCourseOfStudiesRelationTable WHERE courseOfStudiesId = :courseOfStudiesId")
    abstract suspend fun getQuestionnaireCourseOfStudiesRelationWithCosId(courseOfStudiesId: String) : List<QuestionnaireCourseOfStudiesRelation>

    @Query("DELETE FROM questionnaireCourseOfStudiesRelationTable WHERE courseOfStudiesId = :courseOfStudiesId")
    abstract suspend fun deleteQuestionnaireCourseOfStudiesRelationWith(courseOfStudiesId: String)

}