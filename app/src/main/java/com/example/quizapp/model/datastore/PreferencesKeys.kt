package com.example.quizapp.model.datastore

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val THEME_KEY = intPreferencesKey("themeKey")
    val USER_ID_KEY = stringPreferencesKey("userIdKey")
    val USER_NAME_KEY = stringPreferencesKey("userNameKey")
    val USER_PASSWORD_KEY = stringPreferencesKey("userPasswordKey")
    val USER_ROLE_KEY = stringPreferencesKey("userRoleKey")
}