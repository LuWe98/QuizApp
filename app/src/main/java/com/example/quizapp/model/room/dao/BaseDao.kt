package com.example.quizapp.model.room.dao

import androidx.room.*

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: List<T>): LongArray?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: T): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(t: List<T>): Int?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(t: T): Int?

    @Delete
    suspend fun delete(t: List<T>?)

    @Delete
    suspend fun delete(t: T)

}