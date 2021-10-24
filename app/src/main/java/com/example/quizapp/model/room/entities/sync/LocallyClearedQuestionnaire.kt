package com.example.quizapp.model.room.entities.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//TODO -> Diese Entity ist dazu da, dass wenn man Antworten lokal gelöscht hat. Es wird hier nur eingetragen, wenn
//TODO -> es nicht synchronisiert werden konnte. Beim nächsten Syncen, werden die werte dann online gelöscht
@Entity(tableName = Constants.LOCALLY_DELETED_FILLED_QUESTIONNAIRES_TABLE)
@Parcelize
@Serializable
data class LocallyClearedQuestionnaire(
    @PrimaryKey val questionnaireId: String
) : EntityMarker