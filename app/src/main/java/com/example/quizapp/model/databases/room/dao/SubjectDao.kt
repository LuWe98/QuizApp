package com.example.quizapp.model.databases.room.dao

import androidx.room.Dao
import com.example.quizapp.model.databases.room.entities.faculty.Subject

@Dao
abstract class SubjectDao : BaseDao<Subject>(Subject.TABLE_NAME) {

}