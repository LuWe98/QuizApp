package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

//TODO -> Hier werden die IDS von Questionnaires gespeichert, welche noch online eingetragen werden müssen
//TODO -> Also wenn man z.b. bei Browse auf download clicked und dann in dem moment kein Inet hat
//TODO -> Wenn es Online mittlerweile schon einen ausgefüllten Questionnaire mit der Id gibt, wird es ignoriert
@Entity(tableName = Constants.DOWNLOADED_QUESTIONNAIRES_TABLE)
@Parcelize
data class LocallyDownloadedQuestionnaire(@PrimaryKey val questionnaireId: String) : EntityMarker