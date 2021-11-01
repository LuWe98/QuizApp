package com.example.quizapp.model.databases.room.dao.sync

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.databases.room.dao.BaseDao
import com.example.quizapp.model.databases.room.entities.sync.LocallyDeletedUser
import com.example.quizapp.utils.Constants

@Dao
abstract class LocallyDeletedUserDao: BaseDao<LocallyDeletedUser>(Constants.LOCALLY_DELETED_USERS_TABLE) {

    @Query("SELECT * FROM locallyDeletedUsersTable")
    abstract suspend fun getAllLocallyDeletedUserIds() : List<LocallyDeletedUser>

}