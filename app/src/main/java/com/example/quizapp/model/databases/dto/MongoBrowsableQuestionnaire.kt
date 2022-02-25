package com.example.quizapp.model.databases.dto

import android.os.Parcelable
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.ktor.status.DownloadStatus
import com.example.quizapp.model.databases.properties.AuthorInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * This Entity is a lightweight version of the regular MongoQuestionnaire in order to have less size
 */
@Parcelize
@Serializable
data class MongoBrowsableQuestionnaire(
    val id: String,
    val title: String,
    val authorInfo: AuthorInfo,
    val facultyIds: List<String> = emptyList(),
    val courseOfStudiesIds: List<String> = emptyList(),
    val subject: String = "",
    val questionCount: Int = 0,
    val lastModifiedTimestamp: Long,
    var downloadStatus: DownloadStatus = DownloadStatus.NOT_DOWNLOADED
): Parcelable {

    val timeStampAsDate get() = SimpleDateFormat.getDateInstance().format(Date(lastModifiedTimestamp)).toString()

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(MongoBrowsableQuestionnaire::id)
    }

}