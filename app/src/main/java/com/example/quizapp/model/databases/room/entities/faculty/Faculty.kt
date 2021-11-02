package com.example.quizapp.model.databases.room.entities.faculty

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.model.databases.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffCallbackUtil
import io.ktor.util.date.*
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
@Entity(
    tableName = Constants.FACULTY_TABLE_NAME,
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["abbreviation"], unique = true)
    ]
)
@Parcelize
data class Faculty(
    @PrimaryKey @ColumnInfo(name = "facultyId") var id: String = ObjectId().toString(),
    var abbreviation: String,
    var name: String,
    var lastModifiedTimestamp : Long = getTimeMillis()
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffCallbackUtil.createDiffUtil<Faculty> { old, new ->  old.id == new.id}
    }

}