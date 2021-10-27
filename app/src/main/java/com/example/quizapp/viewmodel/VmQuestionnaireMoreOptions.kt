package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.menudatamodels.MenuItem
import com.example.quizapp.model.menudatamodels.MenuItemDataModel
import com.example.quizapp.model.mongodb.documents.questionnaire.QuestionnaireVisibility
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.view.fragments.dialogs.BsdfQuestionnaireMoreOptionsArgs
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions.QuestionnaireMoreOptionsEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class VmQuestionnaireMoreOptions @Inject constructor(
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    preferencesRepository: PreferencesRepository,
    state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfQuestionnaireMoreOptionsArgs.fromSavedStateHandle(state)

    private val currentVisibility get() = args.questionnaire.questionnaireVisibility

    private val questionnaireId get() = args.questionnaire.id



    private val user = preferencesRepository.user

    private val questionnaireMoreOptionsEventChannel = Channel<QuestionnaireMoreOptionsEvent>()

    val questionnaireMoreOptionsEventChannelFlow = questionnaireMoreOptionsEventChannel.receiveAsFlow()

    fun onMenuItemClicked(menuItemId: Int) {
        when (menuItemId) {
            MenuItemDataModel.EDIT_QUESTIONNAIRE_ITEM_ID -> onEditQuestionnaireSelected()
            MenuItemDataModel.DELETE_CREATED_QUESTIONNAIRE_ITEM_ID -> onDeleteCreatedQuestionnaireSelected()
            MenuItemDataModel.DELETE_CACHED_QUESTIONNAIRE_ITEM_ID -> onDeleteCachedQuestionnaireSelected()
            MenuItemDataModel.DELETE_ANSWERS_QUESTIONNAIRE_ITEM_ID -> onDeleteAnswersOfQuestionnaireSelected()
            MenuItemDataModel.UPLOAD_QUESTIONNAIRE_ITEM_ID -> onUploadQuestionnaireSelected()
            MenuItemDataModel.SHARE_QUESTIONNAIRE_ITEM_ID -> onShareQuestionnaireWithOtherUserSelected()
            MenuItemDataModel.PUBLISH_QUESTIONNAIRE_ITEM_ID -> onPublishQuestionnaireSelected()
            MenuItemDataModel.COPY_QUESTIONNAIRE_ITEM_ID -> onCopyQuestionnaireSelected()
        }
    }

    fun getQuestionnaireMoreOptionsMenu() : List<MenuItem> = MenuItemDataModel.getQuestionnaireMoreOptionsMenu(args, user)

    private fun onEditQuestionnaireSelected() = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            questionnaireMoreOptionsEventChannel.send(NavigateToEditQuestionnaireScreen(it))
        }
    }

    private fun onDeleteCreatedQuestionnaireSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(DeleteCreatedQuestionnaireEvent(questionnaireId))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onDeleteCachedQuestionnaireSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(DeleteCachedQuestionnaireEvent(questionnaireId))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onDeleteAnswersOfQuestionnaireSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(DeleteGivenAnswersOfQuestionnaire(questionnaireId))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onUploadQuestionnaireSelected() = launch(IO) {

    }

    private fun onShareQuestionnaireWithOtherUserSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
        questionnaireMoreOptionsEventChannel.send(NavigateToShareQuestionnaireDialogEvent(questionnaireId))
    }

    private fun onPublishQuestionnaireSelected() = launch(IO) {
        val newVisibility = if(currentVisibility == QuestionnaireVisibility.PUBLIC) QuestionnaireVisibility.PRIVATE else QuestionnaireVisibility.PUBLIC
        questionnaireMoreOptionsEventChannel.send(PublishQuestionnaireEvent(questionnaireId, newVisibility))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onCopyQuestionnaireSelected() = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            questionnaireMoreOptionsEventChannel.send(NavigateToCopyQuestionnaireScreen(it))
        }
    }


    sealed class QuestionnaireMoreOptionsEvent {
        class NavigateToEditQuestionnaireScreen(val completeQuestionnaire: CompleteQuestionnaireJunction) : QuestionnaireMoreOptionsEvent()
        class NavigateToCopyQuestionnaireScreen(val completeQuestionnaire: CompleteQuestionnaireJunction) : QuestionnaireMoreOptionsEvent()
        class DeleteCreatedQuestionnaireEvent(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class DeleteCachedQuestionnaireEvent(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class DeleteGivenAnswersOfQuestionnaire(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class PublishQuestionnaireEvent(val questionnaireId: String, val newVisibility: QuestionnaireVisibility): QuestionnaireMoreOptionsEvent()
        class NavigateToShareQuestionnaireDialogEvent(val questionnaireId: String): QuestionnaireMoreOptionsEvent()
        object NavigateBack : QuestionnaireMoreOptionsEvent()
    }
}