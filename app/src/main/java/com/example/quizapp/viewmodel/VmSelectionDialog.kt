package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.launch
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionTypeItemMarker
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent
import com.example.quizapp.view.fragments.dialogs.selection.BsdfSelectionArgs
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmSelectionDialog @Inject constructor(
    state: SavedStateHandle,
    private val applicationScope: CoroutineScope
) : BaseViewModel() {

    private val args = BsdfSelectionArgs.fromSavedStateHandle(state)

    val selectionType: SelectionRequestType<*> get() = args.selectionType

    fun onItemSelected(item: SelectionTypeItemMarker<*>) = launch(IO, applicationScope) {
        navigationDispatcher.dispatch(NavigationEvent.NavigateBack)

        selectionType.resultProvider(item).let { result ->
            fragmentResultDispatcher.dispatch(result)
        }
    }

    fun isItemSelected(item: SelectionTypeItemMarker<*>) = selectionType.isItemSelectedProvider(item)

}