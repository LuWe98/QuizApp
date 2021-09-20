package com.example.quizapp.utils

object Constants {

    const val DATASTORE_NAME = "quizDatastore"

    // Für physisches Handy 192.168.178.78 -> CMD -> ipconfig
    // Für Emulator Handy 10.0.2.2
    const val EXTERNAL_DATABASE_URL = "http://192.168.178.78:8080"


    const val ROOM_DATABASE_NAME = "quizRoomDatabase"

    const val ANSWER_TABLE_NAME = "answerTable"
    const val QUESTION_TABLE_NAME = "questionTable"
    const val QUESTIONARY_TABLE_NAME = "questionaryTable"
    const val USER_TABLE_NAME = "userTable"
    const val USER_ROLE_TABLE_NAME = "userRoleTable"
    const val SUBJECT_TABLE_NAME = "subjectTable"
    const val FACULTY_TABLE_NAME = "facultyTable"
    const val COURSE_OF_STUDIES_TABLE_NAME = "courseOfStudiesTableName"
}