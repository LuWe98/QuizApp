package com.example.quizapp.model.room.entities.faculty

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.model.room.entities.EntityMarker
import com.example.quizapp.utils.Constants
import com.example.quizapp.utils.DiffUtilHelper
import kotlinx.parcelize.Parcelize
import org.bson.types.ObjectId


//TODO -> Sollte auf COS verweisen über
// 1) Beziehungstabelle: Wenn Subjects zu mehreren COS gehören können
// 2) Über einfachen FK wenn nicht
@Entity(
    tableName = Constants.SUBJECT_TABLE_NAME
)
@Parcelize
data class Subject(
    @PrimaryKey var id: String = ObjectId().toString(),
    var name: String
) : EntityMarker {

    companion object {
        val DIFF_CALLBACK = DiffUtilHelper.createDiffUtil<Subject> { old, new ->  old.id == new.id}
    }

}