package com.example.quizapp.model.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.User
import com.example.quizapp.model.room.entities.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserRoleDao : BaseDao<UserRole> {

}