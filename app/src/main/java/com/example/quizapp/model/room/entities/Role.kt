package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId

@Entity(
    tableName = Constants.USER_ROLE_TABLE_NAME,
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
@Parcelize
data class Role(
    @PrimaryKey var id: String = ObjectId().toString(),
    var name: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Role> { old, new ->  old.id == new.id}
    }

}