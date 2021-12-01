package com.example.quizapp.utils

object Constants {

    const val C_NATIVE_LIB_NAME = "native-lib"

    const val DATASTORE_NAME = "quizDatastore"

    const val ROOM_DATABASE_NAME = "quizRoomDatabase"
    const val ROOM_DATABASE_VERSION = 1

    // Für physisches Handy 192.168.178.78 -> CMD -> ipconfig
    // Für Emulator Handy 10.0.2.2
    private const val BACKEND_HOSTNAME = "http://192.168.178.78"
    private const val BACKEND_PORT = 8080
    const val BACKEND_PATH = "$BACKEND_HOSTNAME:$BACKEND_PORT"
    const val REALM = "QuizApp"
}