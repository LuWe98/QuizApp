package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Subject
import com.example.quizapp.utils.Constants

@Dao
abstract class SubjectDao : BaseDao<Subject>(Constants.SUBJECT_TABLE_NAME) {

}