package com.example.quizapp.utils

object Constants {

    const val DATASTORE_NAME = "quizDatastore"

    const val ROOM_DATABASE_NAME = "quizRoomDatabase"
    const val ROOM_DATABASE_VERSION = 1

    const val ANSWER_TABLE_NAME = "answerTable"
    const val QUESTION_TABLE_NAME = "questionTable"
    const val QUESTIONARY_TABLE_NAME = "questionaryTable"
    const val SUBJECT_TABLE_NAME = "subjectTable"
    const val FACULTY_TABLE_NAME = "facultyTable"
    const val COURSE_OF_STUDIES_TABLE_NAME = "courseOfStudiesTableName"
    const val LOCALLY_DELETED_QUESTIONNAIRES_TABLE = "deletedQuestionnairesTable"
    const val LOCALLY_DELETED_FILLED_QUESTIONNAIRES_TABLE = "deletedFilledQuestionnairesTable"
    const val LOCALLY_ANSWERED_QUESTIONNAIRES_TABLE = "locallyAnsweredQuestionnairesTable"
    const val LOCALLY_DOWNLOADED_QUESTIONNAIRES_TABLE = "downloadedQuestionnairesTable"

    // Für physisches Handy 192.168.178.78 -> CMD -> ipconfig
    // Für Emulator Handy 10.0.2.2
    const val BACKEND_URL = "http://192.168.178.78:8080"
}