package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation

@Dao
abstract class FacultyCourseOfStudiesRelationDao : BaseDao<FacultyCourseOfStudiesRelation>(FacultyCourseOfStudiesRelation.TABLE_NAME)