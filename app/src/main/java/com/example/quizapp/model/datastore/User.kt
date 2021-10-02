package com.example.quizapp.model.datastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Long,
    val name: String,
    val password : String,
    val roleId: Long?,
    val facultyId: Long?,
    val courseOfStudiesId: Long?,
) : Parcelable