package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation

@Dao
abstract class FacultyCourseOfStudiesRelationDao : BaseDao<FacultyCourseOfStudiesRelation>(FacultyCourseOfStudiesRelation.TABLE_NAME) {

    @Query("DELETE FROM facultyCourseOfStudiesRelationTable WHERE courseOfStudiesId = :courseOfStudiesId")
    abstract suspend fun deleteFacultyCourseOfStudiesRelationsWith(courseOfStudiesId: String)

}