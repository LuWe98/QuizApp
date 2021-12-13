package com.example.quizapp.model.databases.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.extensions.generateDiffItemCallback
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.mongodb.documents.MongoFaculty
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
@Entity(
    tableName = Faculty.TABLE_NAME,
    indices = [
        Index(value = [Faculty.NAME_COLUMN], unique = true),
        Index(value = [Faculty.ABBREVIATION_COLUMN], unique = true)
    ]
)
@Parcelize
data class Faculty(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    val id: String = ObjectId().toHexString(),
    @ColumnInfo(name = ABBREVIATION_COLUMN)
    val abbreviation: String,
    @ColumnInfo(name = NAME_COLUMN)
    val name: String,
    @ColumnInfo(name = LAST_MODIFIED_TIMESTAMP_COLUMN)
    val lastModifiedTimestamp : Long = getTimeMillis()
) : EntityMarker {

    val asMongoFaculty: MongoFaculty get() = DataMapper.mapRoomFacultyToMongoFaculty(this)

    companion object {
        val DIFF_CALLBACK = generateDiffItemCallback(Faculty::id)

        const val TABLE_NAME = "facultyTable"

        const val ID_COLUMN = "facultyId"
        const val ABBREVIATION_COLUMN = "abbreviation"
        const val NAME_COLUMN = "name"
        const val LAST_MODIFIED_TIMESTAMP_COLUMN = "lastModifiedTimestamp"
    }

}