package com.example.quizapp.model.datastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//@Entity(
//    tableName = Constants.USER_TABLE_NAME,
//    foreignKeys = [
//        ForeignKey(
//            entity = Role::class,
//            parentColumns = ["id"],
//            childColumns = ["roleId"],
//            onUpdate = ForeignKey.CASCADE,
//            onDelete = ForeignKey.SET_NULL
//        ),
//        ForeignKey(
//            entity = Faculty::class,
//            parentColumns = ["id"],
//            childColumns = ["facultyId"],
//            onUpdate = ForeignKey.CASCADE,
//            onDelete = ForeignKey.SET_NULL
//        ),
//        ForeignKey(
//            entity = CourseOfStudies::class,
//            parentColumns = ["id"],
//            childColumns = ["courseOfStudiesId"],
//            onUpdate = ForeignKey.CASCADE,
//            onDelete = ForeignKey.SET_NULL
//        )
//    ],
//    indices = [
//        Index(value = ["email"], unique = true),
//        Index(value = ["roleId"]),
//        Index(value = ["facultyId"]),
//        Index(value = ["courseOfStudiesId"])
//    ]
//)
@Parcelize
data class User(
    val id: Long,
    val email: String,
    val password : String,
    val roleId: Long?,
    val facultyId: Long?,
    val courseOfStudiesId: Long?,
) : Parcelable