package com.example.quizapp.model.room.entities.sync

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//TODO -> Diese Entity ist dazu da, wenn man Antworten zu einem Fragebogen gegeben hat, dass diese auch hochgeladen werden, auch wenn man die App verlässt
@Parcelize
@Serializable
@Entity(tableName = Constants.LOCALLY_ANSWERED_QUESTIONNAIRES_TABLE)
class LocallyAnsweredQuestionnaire(@PrimaryKey val questionnaireId: String): EntityMarker