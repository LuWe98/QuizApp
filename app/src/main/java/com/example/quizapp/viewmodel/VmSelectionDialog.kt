package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.launch
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionTypeItemMarker
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent
import com.example.quizapp.view.fragments.dialogs.selection.BsdfSelectionArgs
import com.example.quizapp.view.fragments.resultdispatcher.requests.selection.SelectionRequestType
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmSelectionDialog @Inject constructor(
    state: SavedStateHandle
) : BaseViewModel<BaseViewModel.EmptyEventClass>() {

    private val args = BsdfSelectionArgs.fromSavedStateHandle(state)

    val selectionType: SelectionRequestType<*> get() = args.selectionType

    fun onItemSelected(item: SelectionTypeItemMarker<*>) = launch(IO) {
        selectionType.resultProvider(item).let { result ->
            fragmentResultDispatcher.dispatch(result)
        }
        navigationDispatcher.dispatch(NavigationEvent.NavigateBack)
    }

    fun isItemSelected(item: SelectionTypeItemMarker<*>) = selectionType.isItemSelectedProvider(item)

}