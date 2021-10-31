package com.example.quizapp.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.input.InputManager
import android.widget.Toast

import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.utils.Constants
import com.example.quizapp.view.fragments.settingsscreen.QuizAppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import okio.IOException
import java.util.*

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_NAME)

val DataStore<Preferences>.dataflow
    get() = data.catch { exception ->
        if (exception is IOException) emit(emptyPreferences()) else throw exception
    }

@MainThread
fun Context.showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, text, duration).show()
}

@MainThread
fun Context.showToast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, textRes, duration).show()
}

val Context.statusBarHeight: Int get() = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

fun Context.isPermissionGranted(permission: String) = checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED


fun Context.getThemeColor(@AttrRes themeAttrId: Int) = TypedValue().let {
    theme.resolveAttribute(themeAttrId, it, true)
    it.data
}

fun Context.showKeyboard(view: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.hideKeyboard(view: View) {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(view.windowToken, 0)
    }
}



fun Context.setLocale() = setLocale(runBlocking(Dispatchers.IO) {
    dataStore.dataflow.map { preferences ->
        preferences[PreferencesRepository.LANGUAGE_KEY]?.let {
            QuizAppLanguage.valueOf(it)
        } ?: QuizAppLanguage.SYSTEM_DEFAULT
    }.first()
})

fun Context.setLocale(quizAppLanguage: QuizAppLanguage): Context {
    val newLocale = quizAppLanguage.locale
    Locale.setDefault(newLocale)
    val config = resources.configuration.apply {
        setLocale(newLocale)
        setLayoutDirection(newLocale)
    }
    return createConfigurationContext(config)
}