package com.example.quizapp.utils

object Secrets {

//    init { System.loadLibrary(Constants.C_NATIVE_LIB_NAME) }

    //TODO -> momentan noch statisch hier drin
    fun datastoreEncryptionSecretKey(): String = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="

    fun datastoreEncryptionSalt(): String = "QWlGNHNhMTJTQWZ2bGhpV3U="

    fun datastoreEncryptionIv(): String = "bVQzNFNhRkQ1Njc4UUFaWA=="


//    external fun hallo() : String

    //TODO -> Es external machen!
    //TODO -> mit dem CPP Zeug
//    external fun datastoreEncryptionSecretKey(): String
//
//    external fun datastoreEncryptionSalt(): String
//
//    external fun datastoreEncryptionIv(): String
}