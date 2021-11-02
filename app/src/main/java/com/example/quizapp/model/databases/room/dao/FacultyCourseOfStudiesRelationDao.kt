package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import com.example.quizapp.model.databases.room.entities.relations.FacultyCourseOfStudiesRelation
import com.example.quizapp.utils.Constants

@Dao
abstract class FacultyCourseOfStudiesRelationDao : BaseDao<FacultyCourseOfStudiesRelation>(Constants.FACULTY_COURSE_OF_STUDIES_RELATION_TABLE_NAME)