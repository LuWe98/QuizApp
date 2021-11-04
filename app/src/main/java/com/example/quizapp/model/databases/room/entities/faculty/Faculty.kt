package com.example.quizapp.model.databases.room.entities.faculty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.DiffCallbackUtil
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
    var id: String = ObjectId().toString(),
    @ColumnInfo(name = ABBREVIATION_COLUMN)
    var abbreviation: String,
    @ColumnInfo(name = NAME_COLUMN)
    var name: String,
    @ColumnInfo(name = LAST_MODIFIED_TIMESTAMP_COLUMN)
    var lastModifiedTimestamp : Long = getTimeMillis()
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<Faculty> { old, new ->  old.id == new.id }

        const val TABLE_NAME = "facultyTable"

        const val ID_COLUMN = "facultyId"
        const val ABBREVIATION_COLUMN = "abbreviation"
        const val NAME_COLUMN = "name"
        const val LAST_MODIFIED_TIMESTAMP_COLUMN = "lastModifiedTimestamp"
    }

}