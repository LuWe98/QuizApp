package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import com.example.quizapp.QuizNavGraphArgs
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertFilledQuestionnaireResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.questionnaire.Answer
import com.example.quizapp.model.databases.room.entities.sync.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.QuestionnaireShuffleType.*
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VmQuiz @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val preferencesRepository: PreferencesRepository,
    private val localRepository: LocalRepository,
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = QuizNavGraphArgs.fromSavedStateHandle(state)

    private val fragmentEventChannel = Channel<FragmentQuizEvent>()

    val fragmentEventChannelFlow get() = fragmentEventChannel.receiveAsFlow()

    val completeQuestionnaireStateFlow = localRepository.findCompleteQuestionnaireAsFlowWith(args.questionnaireId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val completeQuestionnaire get() = completeQuestionnaireStateFlow.value

    val questionList
        get() = when (shuffleType) {
            SHUFFLED_QUESTIONS, SHUFFLED_QUESTIONS_AND_ANSWERS -> completeQuestionnaire?.allQuestions?.shuffled(Random(shuffleSeed))
            else -> completeQuestionnaire?.allQuestions
        } ?: emptyList()

    fun getQuestionWithAnswersFlow(questionId: String) = completeQuestionnaireStateFlow
        .mapNotNull { it?.getQuestionWithAnswers(questionId) }
        .distinctUntilChanged()

    val questionnaireFlow = completeQuestionnaireStateFlow
        .mapNotNull { it?.questionnaire }
        .distinctUntilChanged()

    val questionsWithAnswersFlow = completeQuestionnaireStateFlow
        .mapNotNull { it?.questionsWithAnswers }
        .distinctUntilChanged()

    val questionStatisticsFlow = completeQuestionnaireStateFlow
        .mapNotNull { it?.toQuizStatisticNumbers }
        .distinctUntilChanged()

    val areAllQuestionsAnsweredFlow = questionStatisticsFlow
        .map { it.areAllQuestionsAnswered }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val areAllQuestionsAnswered get() = areAllQuestionsAnsweredFlow.value

    private var _shuffleSeed = state.get<Long>(SHUFFLE_SEED_KEY) ?: getTimeMillis()
        set(value) {
            state.set(SHUFFLE_SEED_KEY, value)
            field = value
        }

    private var _bottomSheetState = state.get<Int>(BOTTOMSHEET_STATE_KEY) ?: BottomSheetBehavior.STATE_COLLAPSED
        set(value) {
            state.set(BOTTOMSHEET_STATE_KEY, value)
            field = value
        }

    val shuffleSeed get() = _shuffleSeed

    val bottomSheetState get() = _bottomSheetState

    val shuffleTypeStateFlow = preferencesRepository.shuffleTypeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, runBlocking { preferencesRepository.getShuffleType() })

    val shuffleType get() = shuffleTypeStateFlow.value

    suspend fun onMenuItemOrderSelected(shuffleType: QuestionnaireShuffleType) {
        preferencesRepository.updateShuffleType(shuffleType)
        updateShuffleTypeSeed()
    }

    fun updateShuffleTypeSeed(newSeed: Long = getTimeMillis()) {
        _shuffleSeed = newSeed
    }

    fun onBottomSheetStateUpdated(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED) {
            _bottomSheetState = newState
        }
    }


    fun onMenuItemShowSolutionClicked() {
        completeQuestionnaire?.let {
            launch(IO) {
                if (it.areAllQuestionsAnswered) {
                    fragmentEventChannel.send(NavigateToQuizScreen(true))
                } else {
                    fragmentEventChannel.send(ShowMessageSnackBar(R.string.pleaseAnswerAllQuestionsText))
                }
            }
        }
    }


    fun onMoreOptionsItemClicked() = launch(IO) {
        fragmentEventChannel.send(ShowPopupMenu)
    }


    fun onMenuItemClearGivenAnswersClicked() = launch(IO, applicationScope) {
        completeQuestionnaire?.apply {
            localRepository.insert(LocallyFilledQuestionnaireToUpload(questionnaire.id))
            fragmentEventChannel.send(ShowUndoDeleteGivenAnswersSnackBack(allAnswers))
            allAnswers.map { it.copy(isAnswerSelected = false) }.let {
                localRepository.update(it)
            }
        }
    }

    fun onUndoDeleteGivenAnswersClick(event: ShowUndoDeleteGivenAnswersSnackBack) = launch(IO) {
        localRepository.update(event.lastAnswerValues)
    }

    fun onStartButtonClicked() {
        launch(IO) {
            fragmentEventChannel.send(NavigateToQuizScreen(false))
        }
    }

    fun onAnswerItemClicked(selectedAnswerId: String, questionId: String) = launch(IO) {
        completeQuestionnaire?.let {
            val question = it.getQuestionWithAnswers(questionId)

            if (question.question.isMultipleChoice) {
                question.answers.firstOrNull { answer -> answer.id == selectedAnswerId }?.let { answer ->
                    localRepository.update(answer.copy(isAnswerSelected = !answer.isAnswerSelected))
                }
            } else {
                localRepository.update(question.answers.map { answer -> answer.copy(isAnswerSelected = answer.id == selectedAnswerId) })
            }

            localRepository.insert(LocallyFilledQuestionnaireToUpload(it.questionnaire.id))
        }
    }


    override fun onCleared() {
        super.onCleared()
        uploadFilledQuestionnaire()
    }

    private fun uploadFilledQuestionnaire() = launch(IO, applicationScope) {
        completeQuestionnaire?.let {
            if (it.questionnaire.syncStatus != SyncStatus.SYNCED) return@launch
            if (!localRepository.isLocallyFilledQuestionnaireToUploadPresent(it.questionnaire.id)) return@launch

            runCatching {
                backendRepository.insertFilledQuestionnaire(DataMapper.mapRoomQuestionnaireToMongoFilledQuestionnaire(it))
            }.onSuccess { response ->
                if (response.responseType != InsertFilledQuestionnaireResponseType.ERROR) {
                    localRepository.delete(LocallyFilledQuestionnaireToUpload(it.questionnaire.id))
                }
            }
        }
    }


    sealed class FragmentQuizEvent {
        class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizEvent()
        class ShowMessageSnackBar(val messageRes: Int) : FragmentQuizEvent()
        class NavigateToQuizScreen(val isShowSolutionScreen: Boolean) : FragmentQuizEvent()
        object ShowPopupMenu : FragmentQuizEvent()
    }

    companion object {
        const val SHUFFLE_SEED_KEY = "shuffleSeedKey"
        const val BOTTOMSHEET_STATE_KEY = "bottomSheetStateKey"
    }
}