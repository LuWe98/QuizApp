package com.example.quizapp.utils

object Secrets {

    init { System.loadLibrary(Constants.C_NATIVE_LIB_NAME) }

    external fun datastoreEncryptionSecretKey(): String

    external fun datastoreEncryptionSalt(): String

    external fun datastoreEncryptionIv(): String

}