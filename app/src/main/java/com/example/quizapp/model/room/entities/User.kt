package com.example.quizapp.model.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.quizapp.utils.Constants
import kotlinx.parcelize.Parcelize

@Entity(
    tableName = Constants.USER_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = UserRole::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Faculty::class,
            parentColumns = ["id"],
            childColumns = ["facultyId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CourseOfStudies::class,
            parentColumns = ["id"],
            childColumns = ["courseOfStudiesId"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["userName"], unique = true),
        Index(value = ["roleId"]),
        Index(value = ["facultyId"]),
        Index(value = ["courseOfStudiesId"])
    ]
)
@Parcelize
data class User(
    @PrimaryKey(autoGenerate = true) override val id: Long,
    val email: String,
    val userName: String,
    val roleId: Long?,
    val facultyId: Long?,
    val courseOfStudiesId: Long?,
) : EntityMarker(id) {

    companion object {
        val DIFF_CALLBACK = createBasicDiffUtil<User>()
    }

}