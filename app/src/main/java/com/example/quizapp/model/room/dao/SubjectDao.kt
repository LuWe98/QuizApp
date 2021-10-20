package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Subject

@Dao
abstract class SubjectDao : BaseDao<Subject> {

    @Query("DELETE FROM subjectTable")
    abstract suspend fun deleteAllSubjects()

}