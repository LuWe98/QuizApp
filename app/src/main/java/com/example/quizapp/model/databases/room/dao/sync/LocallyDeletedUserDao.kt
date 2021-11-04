package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedUser

@Dao
abstract class LocallyDeletedUserDao: BaseDao<LocallyDeletedUser>(LocallyDeletedUser.TABLE_NAME) {

    @Query("SELECT * FROM locallyDeletedUsersTable")
    abstract suspend fun getAllLocallyDeletedUserIds() : List<LocallyDeletedUser>

}