package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.MenuIntIdItem
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.MenuItemDataModel
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.fragments.dialogs.localquestionnairemoreoptions.BsdfQuestionnaireMoreOptionsArgs
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions.QuestionnaireMoreOptionsEvent
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions.QuestionnaireMoreOptionsEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import javax.inject.Inject

@HiltViewModel
class VmQuestionnaireMoreOptions @Inject constructor(
    private val localRepository: LocalRepository,
    preferencesRepository: PreferencesRepository,
    state: SavedStateHandle
) : EventViewModel<QuestionnaireMoreOptionsEvent>() {

    private val args = BsdfQuestionnaireMoreOptionsArgs.fromSavedStateHandle(state)

    private val currentVisibility get() = args.questionnaire.visibility

    private val questionnaireId get() = args.questionnaire.id

    private val user = preferencesRepository.user

    fun onMenuItemClicked(menuItemId: Int) {
        when (menuItemId) {
            MenuItemDataModel.EDIT_QUESTIONNAIRE_ITEM_ID -> onEditQuestionnaireSelected()
            MenuItemDataModel.DELETE_CREATED_QUESTIONNAIRE_ITEM_ID -> onDeleteCreatedQuestionnaireSelected()
            MenuItemDataModel.DELETE_CACHED_QUESTIONNAIRE_ITEM_ID -> onDeleteCachedQuestionnaireSelected()
            MenuItemDataModel.DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID -> onDeleteAnswersOfQuestionnaireSelected()
            MenuItemDataModel.SHARE_QUESTIONNAIRE_ITEM_ID -> onShareQuestionnaireWithOtherUserSelected()
            MenuItemDataModel.PUBLISH_QUESTIONNAIRE_ITEM_ID -> onPublishQuestionnaireSelected()
            MenuItemDataModel.COPY_QUESTIONNAIRE_ITEM_ID -> onCopyQuestionnaireSelected()
        }
    }

    fun getQuestionnaireMoreOptionsMenu() : List<MenuIntIdItem> =
        MenuItemDataModel.getQuestionnaireMoreOptionsMenu(args, user)

    private fun onEditQuestionnaireSelected() = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            navigationDispatcher.dispatch(FromHomeToAddEditQuestionnaire(it))
        }
    }

    private fun onDeleteCreatedQuestionnaireSelected() = launch(IO) {
        eventChannel.send(DeleteCreatedQuestionnaireEvent(questionnaireId))
        navigationDispatcher.dispatch(NavigateBack)
    }

    private fun onDeleteCachedQuestionnaireSelected() = launch(IO) {
        eventChannel.send(DeleteCachedQuestionnaireEvent(questionnaireId))
        navigationDispatcher.dispatch(NavigateBack)
    }

    private fun onDeleteAnswersOfQuestionnaireSelected() = launch(IO) {
        eventChannel.send(DeleteGivenAnswersOfQuestionnaire(questionnaireId))
        navigationDispatcher.dispatch(NavigateBack)
    }

    private fun onShareQuestionnaireWithOtherUserSelected() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
        navigationDispatcher.dispatch(ToShareQuestionnaireDialog(questionnaireId))
    }

    private fun onPublishQuestionnaireSelected() = launch(IO) {
        val newVisibility = if(currentVisibility == QuestionnaireVisibility.PUBLIC) QuestionnaireVisibility.PRIVATE else QuestionnaireVisibility.PUBLIC
        navigationDispatcher.dispatch(NavigateBack)
        eventChannel.send(PublishQuestionnaireEvent(questionnaireId, newVisibility))
    }

    private fun onCopyQuestionnaireSelected() = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            navigationDispatcher.dispatch(FromHomeToAddEditQuestionnaire(it, true))
        }
    }


    sealed class QuestionnaireMoreOptionsEvent: UiEventMarker {
        class DeleteCreatedQuestionnaireEvent(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class DeleteCachedQuestionnaireEvent(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class DeleteGivenAnswersOfQuestionnaire(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class PublishQuestionnaireEvent(val questionnaireId: String, val newVisibility: QuestionnaireVisibility): QuestionnaireMoreOptionsEvent()
    }
}