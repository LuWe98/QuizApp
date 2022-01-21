package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.quizapp.AddEditNavGraphArgs
import com.example.quizapp.QuizApplication
import com.example.quizapp.R
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.properties.QuestionnaireVisibility.*
import com.example.quizapp.model.databases.mongodb.documents.User
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.asRoomListLoadStatus
import com.example.quizapp.model.databases.room.entities.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.entities.QuestionnaireCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.BackendResponse.*
import com.example.quizapp.model.ktor.BackendResponse.InsertQuestionnairesResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus.*
import com.example.quizapp.utils.CsvDocumentFilePicker.*
import com.example.quizapp.view.dispatcher.fragmentresult.FragmentResultDispatcher.*
import com.example.quizapp.view.dispatcher.navigation.NavigationDispatcher.NavigationEvent.*
import com.example.quizapp.view.dispatcher.fragmentresult.requests.ConfirmationRequestType
import com.example.quizapp.view.fragments.dialogs.loadingdialog.DfLoading
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.SelectionRequestType
import com.example.quizapp.view.dispatcher.fragmentresult.requests.selection.datawrappers.AddEditQuestionMoreOptionsItem
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.*
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.AddEditQuestionnaireEvent.*
import com.example.quizapp.viewmodel.customimplementations.EventViewModel
import com.example.quizapp.viewmodel.customimplementations.UiEventMarker
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.cio.*
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import javax.inject.Inject

@HiltViewModel
class VmAddEditQuestionnaire @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val dataMapper: DataMapper,
    private val state: SavedStateHandle,
    private val app: QuizApplication
) : EventViewModel<AddEditQuestionnaireEvent>() {

    private val args = AddEditNavGraphArgs.fromSavedStateHandle(state)

    private val questionListEventChannel = Channel<AddEditQuestionnaireQuestionListEvent>()

    val questionListEventChannelFlow = questionListEventChannel.receiveAsFlow()


    val pageTitleRes
        get() = when {
            args.completeQuestionnaire == null -> R.string.create
            args.copy -> R.string.copy
            else -> R.string.edit
        }

    private val parsedQuestionnaireId = if (args.copy) ObjectId().toHexString() else args.completeQuestionnaire?.questionnaire?.id ?: ObjectId().toHexString()

    private val parsedQuestionnaireTitle get() = args.completeQuestionnaire?.questionnaire?.title ?: ""

    private val parsedQuestionnaireSubject get() = args.completeQuestionnaire?.questionnaire?.subject ?: ""

    private val parsedQuestionnaireVisibility get() = args.completeQuestionnaire?.questionnaire?.visibility ?: PRIVATE

    private val parsedCourseOfStudiesIds
        get() = args.completeQuestionnaire?.allCoursesOfStudies
            ?.map(CourseOfStudies::id)?.toMutableSet()
            ?: runBlocking(IO) { preferencesRepository.getPreferredCourseOfStudiesId() }

    private val parsedQuestionsWithAnswers
        get() = args.completeQuestionnaire?.questionsWithAnswers
            ?.sortedBy(QuestionWithAnswers::question / Question::questionPosition)
            ?.toMutableList()
            ?: emptyList()


    private val questionSearchQueryMutableStateFlow = state.getMutableStateFlow(QUESTION_SEARCH_QUERY_KEY, "")

    val questionSearchQueryStateFlow = questionSearchQueryMutableStateFlow.asStateFlow()

    val questionSearchQuery get() = questionSearchQueryMutableStateFlow.value



    private var _questionnaireTitle = state.get<String>(QUESTIONNAIRE_TITLE_KEY) ?: parsedQuestionnaireTitle
        set(value) {
            state.set(QUESTIONNAIRE_TITLE_KEY, value)
            field = value
        }

    val questionnaireTitle get() = _questionnaireTitle

    private var _questionnaireSubject = state.get<String>(QUESTIONNAIRE_SUBJECT_KEY) ?: parsedQuestionnaireSubject
        set(value) {
            state.set(QUESTIONNAIRE_SUBJECT_KEY, value)
            field = value
        }

    val questionnaireSubject get() = _questionnaireSubject



    private var coursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(COURSES_OF_STUDIES_IDS_KEY, parsedCourseOfStudiesIds)

    val coursesOfStudiesStateFlow = coursesOfStudiesIdsMutableStateFlow
        .map(localRepository::getCoursesOfStudiesWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val courseOfStudiesIds get() = coursesOfStudiesIdsMutableStateFlow.value.toMutableSet()



    private val questionsWithAnswersMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_QUESTIONS_KEY, parsedQuestionsWithAnswers)

    val questionsWithAnswersStateFlow = questionsWithAnswersMutableStateFlow.asStateFlow()

    private val questionsWithAnswers get() = questionsWithAnswersMutableStateFlow.value.toMutableList()

    val filteredQuestionsWithAnswersFlow = combine(questionsWithAnswersMutableStateFlow, questionSearchQueryMutableStateFlow) { qwa, query ->
        qwa.filter { it.question.questionText.lowercase().contains(query.lowercase()) }.asRoomListLoadStatus(query::isNotEmpty)
    }.distinctUntilChanged()



    private val publishQuestionMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_PUBLISH_KEY, parsedQuestionnaireVisibility == PUBLIC)

    val publishQuestionStateFlow = publishQuestionMutableStateFlow.asStateFlow()

    private val publishQuestionnaire get() = publishQuestionMutableStateFlow.value



    val userRoleFlow = preferencesRepository.userFlow.map(User::role::get).stateIn(viewModelScope, SharingStarted.Lazily, null)


    private fun setQuestionWithAnswersWithoutPositionUpdate(questionsWithAnswers: List<QuestionWithAnswers>) {
        questionsWithAnswers.toMutableList().let {
            state.set(QUESTIONNAIRE_QUESTIONS_KEY, it)
            questionsWithAnswersMutableStateFlow.value = it
        }
    }

    private fun setQuestionWithAnswers(questionsWithAnswers: List<QuestionWithAnswers>) {
        setQuestionWithAnswersWithoutPositionUpdate(questionsWithAnswers.mapIndexed { index, qwa ->
            qwa.copy(question = qwa.question.copy(questionPosition = index))
        })
    }

    private fun deleteQuestionItem(position: Int) = launch(IO) {
        questionsWithAnswers.apply {
            questionListEventChannel.send(AddEditQuestionnaireQuestionListEvent.ShowQuestionDeletedSnackBarEvent(position, removeAt(position)))
            setQuestionWithAnswers(this)
        }
    }

    private fun setCoursesOfStudiesIds(courseOfStudiesIds: Set<String>) {
        courseOfStudiesIds.let {
            state.set(COURSES_OF_STUDIES_IDS_KEY, it)
            coursesOfStudiesIdsMutableStateFlow.value = it
        }
    }

    fun onCourseOfStudiesDeleteButtonClicked(courseOfStudies: CourseOfStudies) {
        courseOfStudiesIds.apply {
            remove(courseOfStudies.id)
            setCoursesOfStudiesIds(this)
        }
    }

    fun onCourseOfStudiesSelectionResultReceived(result: FragmentResult.CourseOfStudiesSelectionResult) {
        setCoursesOfStudiesIds(result.courseOfStudiesIds)
    }

    fun onCourseOfStudiesButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToCourseOfStudiesSelectionDialog(coursesOfStudiesIdsMutableStateFlow.value))
    }

    fun onClearCourseOfStudiesClicked() {
        setCoursesOfStudiesIds(emptySet())
    }

    fun onPublishCardClicked() {
        state.set(QUESTIONNAIRE_PUBLISH_KEY, !publishQuestionnaire)
        publishQuestionMutableStateFlow.value = !publishQuestionnaire
    }

    fun onTitleUpdated(newTitle: String) {
        _questionnaireTitle = newTitle
    }

    fun onSubjectUpdated(newSubject: String) {
        _questionnaireSubject = newSubject
    }

    fun onQuestionWithAnswerUpdated(position: Int, questionWithAnswers: QuestionWithAnswers) {
        questionsWithAnswers.apply {
            if (position == size) add(questionWithAnswers) else set(position, questionWithAnswers)
            setQuestionWithAnswers(this)
        }
    }

    fun onAddQuestionButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromAddEditQuestionnaireToAddEditQuestion(questionsWithAnswers.size))
    }

    fun onAddQuestionButtonInQuestionListDialogClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromAddEditQuestionnaireQuestionListToAddEditQuestion(questionsWithAnswers.size))
    }

    fun onQuestionItemClicked(questionWithAnswer: QuestionWithAnswers) = launch(IO) {
        navigationDispatcher.dispatch(FromAddEditQuestionnaireQuestionListToAddEditQuestion(questionsWithAnswers.indexOf(questionWithAnswer), questionWithAnswer))
    }

    fun onQuestionLongClicked(questionWithAnswer: QuestionWithAnswers) = launch(IO) {
        navigationDispatcher.dispatch(ToSelectionDialog(SelectionRequestType.AddEditQuestionMoreOptionsSelection(questionWithAnswer)))
    }

    fun onQuestionMoreOptionsSelectionResultReceived(result: SelectionResult.AddEditQuestionMoreOptionsSelectionResult) {
        when(result.selectedItem) {
            AddEditQuestionMoreOptionsItem.EDIT -> onQuestionItemClicked(result.calledOnQuestionWithAnswers)
            AddEditQuestionMoreOptionsItem.DELETE -> deleteQuestionItem(questionsWithAnswers.indexOf(result.calledOnQuestionWithAnswers))
        }
    }

    fun onQuestionItemDragged(from: Int, to: Int) {
        questionsWithAnswers.apply {
            add(to, removeAt(from))
            setQuestionWithAnswersWithoutPositionUpdate(this)
        }
    }

    fun onQuestionItemDragReleased(position: Int) {
        setQuestionWithAnswers(questionsWithAnswers)
    }

    fun onQuestionItemSwiped(position: Int) {
        deleteQuestionItem(position)
    }

    fun onQuestionCardClicked() = launch(IO) {
        navigationDispatcher.dispatch(FromAddEditQuestionnaireToBsdfQuestionList)
    }


    fun onUndoDeleteQuestionClicked(event: AddEditQuestionnaireQuestionListEvent.ShowQuestionDeletedSnackBarEvent) = launch(IO) {
        questionsWithAnswers.apply {
            add(event.questionPosition, event.questionWithAnswers)
            setQuestionWithAnswers(this)
        }
    }

    fun onMoreOptionsClicked() = launch(IO) {
        eventChannel.send(ShowPopupMenu)
    }

    fun onBackButtonClicked() = launch(IO) {
        navigationDispatcher.dispatch(NavigateBack)
    }

    fun onSearchQueryChanged(query: String) {
        state.set(QUESTION_SEARCH_QUERY_KEY, query)
        questionSearchQueryMutableStateFlow.value = query
    }

    fun onDeleteSearchQueryClicked() = launch(IO) {
        if(questionSearchQuery.isNotEmpty()) {
            questionListEventChannel.send(AddEditQuestionnaireQuestionListEvent.ClearSearchQueryEvent)
        }
    }



    fun onSaveButtonClicked() = launch(IO, applicationScope) {
        if (!isInputValid()) return@launch

        val questionnaire = Questionnaire(
            id = parsedQuestionnaireId,
            title = questionnaireTitle,
            authorInfo = preferencesRepository.getOwnAuthorInfo(),
            subject = questionnaireSubject,
            syncStatus = SYNCING,
            visibility = if (publishQuestionnaire) PUBLIC else PRIVATE
        )

        val questionsWithAnswersMapped = questionsWithAnswers.mapIndexed { questionIndex, qwa ->
            val questionId = if (args.copy) ObjectId().toHexString() else qwa.question.id
            val setIsSelectedToFalse = (!qwa.question.isMultipleChoice && qwa.selectedAnswerIds.size > 1) || args.copy

            qwa.apply {
                question = qwa.question.copy(
                    id = questionId,
                    questionnaireId = parsedQuestionnaireId,
                    questionPosition = questionIndex
                )
                answers = qwa.answers.mapIndexed { answerIndex, answer ->
                    answer.copy(
                        id = if (args.copy) ObjectId().toHexString() else answer.id,
                        questionId = questionId,
                        isAnswerSelected = if (setIsSelectedToFalse) false else answer.isAnswerSelected,
                        answerPosition = answerIndex
                    )
                }
            }
        }

        val completeQuestionnaire = CompleteQuestionnaire(
            questionnaire,
            questionsWithAnswersMapped,
            emptyList()
        )

        localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
        localRepository.insert(courseOfStudiesIds.map { cosId -> QuestionnaireCourseOfStudiesRelation(parsedQuestionnaireId, cosId) })
        navigationDispatcher.dispatch(NavigateBack)

        runCatching {
            localRepository.findCompleteQuestionnaireWith(parsedQuestionnaireId)!!.let(dataMapper::mapRoomQuestionnaireToMongoQuestionnaire).let {
                backendRepository.insertQuestionnaire(it)
            }
        }.onSuccess {
            localRepository.update(questionnaire.copy(syncStatus = if (it.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) SYNCED else UNSYNCED))
        }.onFailure {
            localRepository.update(questionnaire.copy(syncStatus = UNSYNCED))
        }
    }

    private suspend fun isInputValid(): Boolean {
        if (questionnaireTitle.isEmpty()) {
            eventChannel.send(ShowMessageSnackBarEvent(R.string.errorQuestionnaireHasNoTitle))
            return false
        }

        if (questionnaireSubject.isEmpty()) {
            eventChannel.send(ShowMessageSnackBarEvent(R.string.errorQuestionnaireHasNoSubject))
            return false
        }

        return true
    }


    fun onLoadCsvFilePopupMenuItemClicked() = launch(IO) {
        navigationDispatcher.dispatch(ToConfirmationDialog(ConfirmationRequestType.LoadCsvFileConfirmationRequest))
    }

    fun onCsvLoadingConfirmationResultReceived(result: ConfirmationResult.LoadCsvFileConfirmationResult) = launch(IO) {
        if(result.confirmed) {
            eventChannel.send(StartCsvDocumentFilePicker)
        }
    }


    fun onValidCsvFileSelected() = launch(IO) {
        navigationDispatcher.dispatch(ToLoadingDialog(R.string.loadingCsvData))
    }

    fun onCsvFilePickerResultReceived(result: CsvDocumentFilePickerResult) = launch(IO) {
        delay(DfLoading.LOADING_DIALOG_LONG_DISMISS_DELAY)
        navigationDispatcher.dispatch(PopLoadingDialog)

        when (result) {
            is CsvDocumentFilePickerResult.Success -> {
                eventChannel.send(SetQuestionnaireTitle(result.questionnaire.title))
                eventChannel.send(SetQuestionnaireSubject(result.questionnaire.subject))
                setQuestionWithAnswers(result.qwa)
                eventChannel.send(ShowMessageSnackBarEvent(R.string.successfullyLoadedCsvData))
            }
            is CsvDocumentFilePickerResult.Error -> {
                eventChannel.send(ShowMessageSnackBarWithStringEvent(result.type.getErrorMessage(app)))
            }
        }
    }

    sealed class AddEditQuestionnaireEvent: UiEventMarker {
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int) : AddEditQuestionnaireEvent()
        class ShowMessageSnackBarWithStringEvent(val message: String) : AddEditQuestionnaireEvent()
        object ShowPopupMenu : AddEditQuestionnaireEvent()
        object StartCsvDocumentFilePicker : AddEditQuestionnaireEvent()
        class SetQuestionnaireTitle(val title: String) : AddEditQuestionnaireEvent()
        class SetQuestionnaireSubject(val subject: String) : AddEditQuestionnaireEvent()
    }

    sealed class AddEditQuestionnaireQuestionListEvent {
        class ShowQuestionDeletedSnackBarEvent(val questionPosition: Int, val questionWithAnswers: QuestionWithAnswers) : AddEditQuestionnaireQuestionListEvent()
        object ClearSearchQueryEvent: AddEditQuestionnaireQuestionListEvent()
    }

    companion object {
        private const val QUESTIONNAIRE_TITLE_KEY = "questionnaireTitleKey"
        private const val QUESTIONNAIRE_SUBJECT_KEY = "questionnaireSubjectKey"
        private const val QUESTIONNAIRE_QUESTIONS_KEY = "questionsWithAnswersKey"
        private const val COURSES_OF_STUDIES_IDS_KEY = "coursesOfStudiesIdsKey"
        private const val QUESTIONNAIRE_PUBLISH_KEY = "questionnairePublishKey"
        private const val QUESTION_SEARCH_QUERY_KEY = "questionSearchQueryKey"
    }
}