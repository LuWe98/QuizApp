package com.example.quizapp.model.room

import androidx.room.TypeConverter
import com.example.quizapp.model.ktor.status.SyncStatus

class LocalDatabaseTypeConverter {

    @TypeConverter
    fun toSyncStatus(syncStatusString : String) = SyncStatus.valueOf(syncStatusString)

    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus) = syncStatus.name

}