package com.example.quizapp.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*


object LocaleUtil {

//    fun getLanguage(context: Context): String? {
//        return getPersistedData(context, Locale.getDefault().language)
//    }
//
//    fun setLocale(context: Context, language: String): Context {
//        persist(context, language)
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            updateResources(context, language)
//        } else updateResourcesLegacy(context, language)
//
//    }

//    fun onAttach(context: Context): Context {
//        val lang = getPersistedData(context, Locale.getDefault().language)
//        return setLocale(context, lang)
//    }
//
//    fun onAttach(context: Context, defaultLanguage: String): Context {
//        val lang = getPersistedData(context, defaultLanguage)
//        return setLocale(context, lang)
//    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration: Configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}