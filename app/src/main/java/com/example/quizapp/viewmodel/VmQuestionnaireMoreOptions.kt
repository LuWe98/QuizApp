package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.menudatamodels.MenuItem
import com.example.quizapp.model.menudatamodels.MenuItemDataModel
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
    private val preferencesRepository: PreferencesRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = BsdfQuestionnaireMoreOptionsArgs.fromSavedStateHandle(state)

    private val info = preferencesRepository.user

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

    fun getQuestionnaireMoreOptionsMenu() : List<MenuItem> =
        MenuItemDataModel.getQuestionnaireMoreOptionsMenu(args.authorId == info.id, info.role)

    private fun onEditQuestionnaireSelected() = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(args.questionnaireId).let {
            questionnaireMoreOptionsEventChannel.send(NavigateToEditQuestionnaireScreen(it))
        }
    }

    private fun onDeleteCreatedQuestionnaireSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(DeleteCreatedQuestionnaireEvent(args.questionnaireId))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onDeleteCachedQuestionnaireSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(DeleteCachedQuestionnaireEvent(args.questionnaireId))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onDeleteAnswersOfQuestionnaireSelected() = launch(IO) {
        questionnaireMoreOptionsEventChannel.send(DeleteGivenAnswersOfQuestionnaire(args.questionnaireId))
        questionnaireMoreOptionsEventChannel.send(NavigateBack)
    }

    private fun onUploadQuestionnaireSelected() = launch(IO) {

    }

    private fun onShareQuestionnaireWithOtherUserSelected() = launch(IO) {

    }

    private fun onPublishQuestionnaireSelected() = launch(IO) {

    }

    private fun onCopyQuestionnaireSelected() = launch(IO) {

    }


    sealed class QuestionnaireMoreOptionsEvent {
        class NavigateToEditQuestionnaireScreen(val completeQuestionnaire: CompleteQuestionnaireJunction?) : QuestionnaireMoreOptionsEvent()
        class DeleteCreatedQuestionnaireEvent(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class DeleteCachedQuestionnaireEvent(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        class DeleteGivenAnswersOfQuestionnaire(val questionnaireId: String) : QuestionnaireMoreOptionsEvent()
        object NavigateBack : QuestionnaireMoreOptionsEvent()
    }
}