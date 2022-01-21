package com.example.quizapp.viewmodel.customimplementations

import androidx.lifecycle.ViewModel
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher
import javax.inject.Inject

abstract class BaseViewModel: ViewModel() {

    @Inject
    protected lateinit var navigationDispatcher: NavigationDispatcher

    @Inject
    protected lateinit var fragmentResultDispatcher: FragmentResultDispatcher

}