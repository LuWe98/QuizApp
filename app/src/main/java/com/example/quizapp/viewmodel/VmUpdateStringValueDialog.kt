package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.launch
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.NavigateBack
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateStringArgs
import com.example.quizapp.viewmodel.VmUpdateStringValueDialog.UpdateStringEvent
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmUpdateStringValueDialog @Inject constructor(
    private val state: SavedStateHandle
) : BaseViewModel<UpdateStringEvent>() {

    private val args = DfUpdateStringArgs.fromSavedStateHandle(state)

    val resultType get() = args.resultType

    private var _updatedText = state.get<String>(UPDATED_TEXT_KEY) ?: args.resultType.stringValue
        set(value) {
            state.set(UPDATED_TEXT_KEY, value)
            field = value
        }

    val updatedText get() = _updatedText.trim()


    fun onEditTextChanged(newText: String) {
        _updatedText = newText.trim()
    }

    fun onConfirmButtonClicked() = launch(IO){
        fragmentResultDispatcher.dispatch(resultType.apply { stringValue = updatedText })
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onCancelButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    sealed class UpdateStringEvent: ViewModelEventMarker

    companion object {
        private const val UPDATED_TEXT_KEY = "updatedTextKey"
    }
}