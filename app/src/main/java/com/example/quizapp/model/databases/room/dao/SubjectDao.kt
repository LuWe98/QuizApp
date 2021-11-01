package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import com.example.quizapp.model.databases.room.entities.faculty.Subject
import com.example.quizapp.utils.Constants

@Dao
abstract class SubjectDao : BaseDao<Subject>(Constants.SUBJECT_TABLE_NAME) {

}