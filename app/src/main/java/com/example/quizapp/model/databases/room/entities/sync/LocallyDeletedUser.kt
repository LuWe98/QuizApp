package com.example.quizapp.model.databases.room.entities.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(tableName = Constants.LOCALLY_DELETED_USERS_TABLE)
@Serializable
@Parcelize
data class LocallyDeletedUser(
    @PrimaryKey val userId: String
) : EntityMarker