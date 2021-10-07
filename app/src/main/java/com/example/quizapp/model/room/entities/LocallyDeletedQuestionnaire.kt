package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//TODO -> Diese Entity ist dazu da, dass wenn man fragebögen lokal löscht und kein inet hat, dass man bei einem neustart diese Fragebögen dann online nachlöschen kann
//TODO -> Fragebögen, welche ihre ID hier drin haben, werden auch nicht online nachgeladen, wenn man die App startet
@Entity(tableName = Constants.DELETED_QUESTIONNAIRES_TABLE)
@Parcelize
@Serializable
data class LocallyDeletedQuestionnaire(
    @PrimaryKey val questionnaireId: String,
    val deleteWholeQuestionnaire : Boolean
) : EntityMarker