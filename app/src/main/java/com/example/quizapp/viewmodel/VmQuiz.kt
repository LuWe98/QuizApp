package com.example.quizapp.viewmodel

import androidx.cardview.widget.CardView
import androidx.lifecycle.*
import com.example.quizapp.QuizNavGraphArgs
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.LocallyFilledQuestionnaireToUpload
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType
import com.example.quizapp.model.datastore.datawrappers.QuestionnaireShuffleType.*
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse
import com.example.quizapp.model.ktor.BackendResponse.InsertFilledQuestionnaireResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.view.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.viewmodel.VmQuiz.*
import com.example.quizapp.viewmodel.VmQuiz.FragmentQuizEvent.*
import com.example.quizapp.viewmodel.customimplementations.BaseViewModel
import com.example.quizapp.viewmodel.customimplementations.ViewModelEventMarker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
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
    private val dataMapper: DataMapper,
    private val state: SavedStateHandle
) : BaseViewModel<FragmentQuizEvent>() {

    private val args = QuizNavGraphArgs.fromSavedStateHandle(state)

    private val completeQuestionnaireNullableStateFlow = localRepository.findCompleteQuestionnaireAsFlowWith(args.questionnaireId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val completeQuestionnaireFlow = completeQuestionnaireNullableStateFlow
        .mapNotNull { it }
        .distinctUntilChanged()

    val completeQuestionnaire get() = completeQuestionnaireNullableStateFlow.value

    fun getQuestionWithAnswersFlow(questionId: String) = completeQuestionnaireFlow
        .map { it.getQuestionWithAnswers(questionId) }
        .distinctUntilChanged()

    val questionnaireFlow = completeQuestionnaireFlow
        .map(CompleteQuestionnaire::questionnaire::get)
        .distinctUntilChanged()

    val questionsWithAnswersFlow = completeQuestionnaireFlow
        .map(CompleteQuestionnaire::questionsWithAnswers::get)
        .distinctUntilChanged()

    val questionStatisticsFlow = completeQuestionnaireFlow
        .map(CompleteQuestionnaire::toQuizStatisticNumbers::get)
        .distinctUntilChanged()

    val areAllQuestionsAnsweredStateFlow = questionStatisticsFlow
        .map(CompleteQuestionnaire.QuizStatisticNumbers::areAllQuestionsAnswered::get)
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val areAllQuestionsAnswered get() = areAllQuestionsAnsweredStateFlow.value


    private var shuffleSeedStateFlow = preferencesRepository.shuffleSeedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, runBlocking(IO) { preferencesRepository.getShuffleSeed() })

    val shuffleSeed get() = shuffleSeedStateFlow.value


    val shuffleTypeStateFlow = preferencesRepository.shuffleTypeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, runBlocking(IO) { preferencesRepository.getShuffleType() })

    val shuffleType get() = shuffleTypeStateFlow.value


    val questionsWithAnswersCombinedStateFlow = combine(
        questionsWithAnswersFlow,
        shuffleTypeStateFlow,
        shuffleSeedStateFlow
    ) { qwa, shuffleType, shuffleSeed ->
        when (shuffleType) {
            SHUFFLED_QUESTIONS, SHUFFLED_QUESTIONS_AND_ANSWERS -> qwa.shuffled(Random(shuffleSeed))
            else -> qwa
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val questionsShuffled get() = questionsWithAnswersCombinedStateFlow.value.map(QuestionWithAnswers::question)


    private var _bottomSheetState = state.get<Int>(BOTTOMSHEET_STATE_KEY) ?: BottomSheetBehavior.STATE_COLLAPSED
        set(value) {
            state.set(BOTTOMSHEET_STATE_KEY, value)
            field = value
        }

    val bottomSheetState get() = _bottomSheetState


    fun onMenuItemOrderSelected(shuffleType: QuestionnaireShuffleType) = launch(IO) {
        preferencesRepository.updateShuffleSeed()
        preferencesRepository.updateShuffleType(shuffleType)
    }


    fun onBottomSheetStateUpdated(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED) {
            _bottomSheetState = newState
        }
    }


    fun onMenuItemShowSolutionClicked() = launch(IO) {
        completeQuestionnaire?.let {
            if (it.areAllQuestionsAnswered) {
                navigationDispatcher.dispatch(FromQuizToQuizContainerScreen(0,true))
            } else {
                eventChannel.send(ShowMessageSnackBar(R.string.pleaseAnswerAllQuestionsText))
            }
        }
    }

    fun onQuestionItemClicked(position: Int, questionId: String, card: CardView) = launch(IO) {
        val questionPosition = questionsShuffled.indexOfFirst { it.id == questionId }
        navigationDispatcher.dispatch(FromQuizToQuizContainerScreen(if (questionPosition == -1) 0 else questionPosition,false))
    }


    fun onMoreOptionsItemClicked() = launch(IO) {
        eventChannel.send(ShowPopupMenu)
    }


    fun onMenuItemClearGivenAnswersClicked() = launch(IO, applicationScope) {
        completeQuestionnaire?.apply {
            localRepository.insert(LocallyFilledQuestionnaireToUpload(questionnaire.id))
            eventChannel.send(ShowUndoDeleteGivenAnswersSnackBack(allAnswers))
            allAnswers.map { it.copy(isAnswerSelected = false) }.let {
                localRepository.update(it)
            }
        }
    }

    fun onUndoDeleteGivenAnswersClick(event: ShowUndoDeleteGivenAnswersSnackBack) = launch(IO) {
        localRepository.update(event.lastAnswerValues)
    }

    fun onStartButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromQuizToQuizContainerScreen(0,false))
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

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
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
                dataMapper.mapRoomQuestionnaireToMongoFilledQuestionnaire(it).let { mongoFilledQuestionnaire ->
                    backendRepository.insertFilledQuestionnaire(mongoFilledQuestionnaire)
                }
            }.onSuccess { response ->
                if (response.responseType != InsertFilledQuestionnaireResponseType.NOT_ACKNOWLEDGED) {
                    localRepository.delete(LocallyFilledQuestionnaireToUpload(it.questionnaire.id))
                }
            }
        }
    }


    sealed class FragmentQuizEvent: ViewModelEventMarker {
        class ShowUndoDeleteGivenAnswersSnackBack(val lastAnswerValues: List<Answer>) : FragmentQuizEvent()
        class ShowMessageSnackBar(val messageRes: Int) : FragmentQuizEvent()
        object ShowPopupMenu : FragmentQuizEvent()
    }

    companion object {
        private const val SHUFFLE_SEED_KEY = "shuffleSeedKey"
        private const val BOTTOMSHEET_STATE_KEY = "bottomSheetStateKey"
    }
}