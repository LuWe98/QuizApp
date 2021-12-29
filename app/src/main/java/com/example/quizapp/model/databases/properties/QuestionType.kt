package com.example.quizapp.model.databases.properties

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//TODO -> Mal schauen ob Ich das Wirklich brauch
@Parcelize
enum class QuestionType: Parcelable {
    QUIZ_MULTIPLE_CHOICE,
    QUIZ_SINGLE_CHOICE
}