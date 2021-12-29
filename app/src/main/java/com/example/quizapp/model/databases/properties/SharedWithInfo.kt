package com.example.quizapp.model.databases.properties

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class SharedWithInfo(
    val userId: String,
    val canEdit: Boolean
) : Parcelable