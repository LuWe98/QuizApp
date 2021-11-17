package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateStringValueArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VmUpdateStringValueDialog @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = DfUpdateStringValueArgs.fromSavedStateHandle(state)

    val updateType get() = args.updateType

    private var _updatedText = state.get<String>(UPDATED_TEXT_KEY) ?: args.initialValue
        set(value) {
            state.set(UPDATED_TEXT_KEY, value)
            field = value
        }

    val updatedText get() = _updatedText.trim()


    fun onEditTextChanged(newText: String) {
        _updatedText = newText.trim()
    }

    companion object {
        private const val UPDATED_TEXT_KEY = "updatedTextKey"
    }
}