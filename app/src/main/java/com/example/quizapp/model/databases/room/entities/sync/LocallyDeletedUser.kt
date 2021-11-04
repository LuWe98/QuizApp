package com.example.quizapp.model.databases.room.entities.sync

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity(tableName = LocallyDeletedUser.TABLE_NAME)
@Serializable
@Parcelize
data class LocallyDeletedUser(
    @PrimaryKey
    @ColumnInfo(name = USER_ID_COLUMN)
    val userId: String
) : EntityMarker {

    companion object {
        const val TABLE_NAME = "locallyDeletedUsersTable"

        const val USER_ID_COLUMN = "userId"
    }

}