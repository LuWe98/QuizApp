package com.example.quizapp.model.room.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class BaseDao<T>(private val tableName: String) {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entities: List<T>): LongArray?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: T): Long?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(entities: List<T>): Int?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun update(entity: T): Int?

    @Delete
    abstract suspend fun delete(entities: List<T>?)

    @Delete
    abstract suspend fun delete(entity: T)

    @RawQuery
    abstract suspend fun executeQuery(query: SupportSQLiteQuery) : Any

    suspend fun deleteAll(){
        executeQuery(SimpleSQLiteQuery("DELETE FROM $tableName"))
    }
}