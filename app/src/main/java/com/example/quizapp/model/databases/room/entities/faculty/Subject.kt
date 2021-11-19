package com.example.quizapp.model.databases.room.entities.faculty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.DiffCallbackUtil
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(tableName = Subject.TABLE_NAME)
@Parcelize
data class Subject(
    @PrimaryKey
    @ColumnInfo(name = ID_COLUMN)
    var id: String = ObjectId().toHexString(),
    @ColumnInfo(name = NAME_COLUMN)
    var name: String,
    @ColumnInfo(name = LAST_MODIFIED_TIMESTAMP_COLUMN)
    var lastModifiedTimestamp : Long = getTimeMillis()
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<Subject> { old, new ->  old.id == new.id }

        const val TABLE_NAME = "subjectTable"

        const val ID_COLUMN = "id"
        const val NAME_COLUMN = "name"
        const val LAST_MODIFIED_TIMESTAMP_COLUMN = "lastModifiedTimestamp"
    }

}