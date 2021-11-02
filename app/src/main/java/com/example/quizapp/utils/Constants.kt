package com.example.quizapp.utils

object Constants {

    const val C_NATIVE_LIB_NAME = "native-lib"

    const val DATASTORE_NAME = "quizDatastore"

    const val ROOM_DATABASE_NAME = "quizRoomDatabase"
    const val ROOM_DATABASE_VERSION = 1

    const val ANSWER_TABLE_NAME = "answerTable"
    const val QUESTION_TABLE_NAME = "questionTable"
    const val QUESTIONNAIRE_TABLE_NAME = "questionnaireTable"
    const val FACULTY_TABLE_NAME = "facultyTable"
    const val COURSE_OF_STUDIES_TABLE_NAME = "courseOfStudiesTable"
    const val SUBJECT_TABLE_NAME = "subjectTable"
    const val FACULTY_COURSE_OF_STUDIES_RELATION_TABLE_NAME = "facultyCourseOfStudiesRelationTable"
    const val LOCALLY_DELETED_QUESTIONNAIRES_TABLE = "deletedQuestionnairesTable"
    const val LOCALLY_CLEARED_QUESTIONNAIRES_TABLE = "deletedFilledQuestionnairesTable"
    const val LOCALLY_ANSWERED_QUESTIONNAIRES_TABLE = "locallyAnsweredQuestionnairesTable"
    const val LOCALLY_DELETED_USERS_TABLE = "locallyDeletedUsersTable"


    // Für physisches Handy 192.168.178.78 -> CMD -> ipconfig
    // Für Emulator Handy 10.0.2.2
    const val BACKEND_HOSTNAME = "http://192.168.178.78"
    const val BACKEND_PORT = 8080
    const val BACKEND_PATH = "$BACKEND_HOSTNAME:$BACKEND_PORT"
    const val REALM = "QuizApp"
}