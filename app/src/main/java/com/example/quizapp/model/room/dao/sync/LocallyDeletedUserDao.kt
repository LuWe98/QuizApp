package com.example.quizapp.model.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.dao.BaseDao
import com.example.quizapp.model.room.entities.sync.LocallyDeletedUser

@Dao
abstract class LocallyDeletedUserDao: BaseDao<LocallyDeletedUser> {

    @Query("SELECT * FROM locallyDeletedUsersTable")
    abstract suspend fun getAllLocallyDeletedUserIds() : List<LocallyDeletedUser>

    @Query("DELETE FROM locallyDeletedUsersTable")
    abstract suspend fun deleteAllLocallyDeletedUsers()

}