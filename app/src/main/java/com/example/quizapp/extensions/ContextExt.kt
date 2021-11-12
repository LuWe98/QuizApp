package com.example.quizapp.extensions

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.QuizAppLanguage
import com.example.quizapp.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
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

fun Context.isPermissionGranted(permission: String) = checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED


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
        } ?: QuizAppLanguage.ENGLISH
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


val Context.statusBarHeight: Int
    get() = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))

@Suppress("DEPRECATION")
val Context.navigationBarHeight: Int
    get() = if (Build.VERSION.SDK_INT >= 30) {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .currentWindowMetrics
            .windowInsets
            .getInsets(WindowInsets.Type.navigationBars())
            .bottom

    } else {
        val appUsableSize = Point()
        val realScreenSize = Point()

        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.apply {
            getSize(appUsableSize)
            getRealSize(realScreenSize)
        }
        when {
            appUsableSize.x < realScreenSize.x -> realScreenSize.x - appUsableSize.x
            appUsableSize.y < realScreenSize.y -> realScreenSize.y - appUsableSize.y
            else -> 0
        }
    }


val Fragment.isConnectedToInternet
    get() = context?.isConnectedToInternet ?: false

val Context.isConnectedToInternet
    get() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).let { manager ->
        val activeNetworks = manager.activeNetwork ?: return@let false
        val capabilities = manager.getNetworkCapabilities(activeNetworks) ?: return@let false
        return@let when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            else -> false
        }
    }