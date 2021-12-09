package com.example.quizapp.di

import androidx.fragment.app.Fragment
import com.example.quizapp.utils.CsvDocumentFilePicker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object FragmentModule {

    @Provides
    fun provideCsvDocumentFilePicker(fragment: Fragment) = CsvDocumentFilePicker(fragment)

}