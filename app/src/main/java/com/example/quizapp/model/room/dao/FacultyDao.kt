package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Faculty

@Dao
abstract class FacultyDao : BaseDao<Faculty> {

    @Query("DELETE FROM facultyTable")
    abstract suspend fun deleteAllFaculties()

}