package com.example.quizapp.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.AddEditNavGraphArgs
import com.example.quizapp.R
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.DataMapper
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.entities.questionnaire.Questionnaire
import com.example.quizapp.model.databases.room.entities.relations.QuestionnaireCourseOfStudiesRelation
import com.example.quizapp.model.databases.room.junctions.CompleteQuestionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertQuestionnairesResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus.*
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.UpdateStringType
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.AddEditQuestionnaireEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.cio.*
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
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
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = AddEditNavGraphArgs.fromSavedStateHandle(state)

    val pageTitleRes
        get() = when {
            args.completeQuestionnaire == null -> R.string.addQuestionnaire
            args.copy -> R.string.copyQuestionnaire
            else -> R.string.editQuestionnaire
        }

    private val parsedQuestionnaireId = args.completeQuestionnaire?.questionnaire?.id ?: ObjectId().toHexString()

    private val parsedQuestionnaireTitle get() = args.completeQuestionnaire?.questionnaire?.title ?: ""

    private val parsedQuestionnaireSubject get() = args.completeQuestionnaire?.questionnaire?.subject ?: ""

    private val parsedCourseOfStudiesIds
        get() = args.completeQuestionnaire?.allCoursesOfStudies
            ?.map(CourseOfStudies::id)?.toMutableSet()
            ?: runBlocking(IO) { preferencesRepository.getPreferredCourseOfStudiesId() }

    private val parsedQuestionsWithAnswers
        get() = args.completeQuestionnaire?.questionsWithAnswers
            ?.sortedBy(QuestionWithAnswers::question / Question::questionPosition)
            ?.toMutableList()
            ?: emptyList()


    private val addEditQuestionnaireEventChannel = Channel<AddEditQuestionnaireEvent>()

    val addEditQuestionnaireEventChannelFlow = addEditQuestionnaireEventChannel.receiveAsFlow()


    private var questionnaireTitleMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_TITLE_KEY, parsedQuestionnaireTitle)

    val questionnaireTitleStateFlow get() = questionnaireTitleMutableStateFlow.asStateFlow()

    private val questionnaireTitle get() = questionnaireTitleStateFlow.value

    private var questionnaireSubjectMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_SUBJECT_KEY, parsedQuestionnaireSubject)

    val questionnaireSubjectStateFlow get() = questionnaireSubjectMutableStateFlow.asStateFlow()

    private val questionnaireSubject get() = questionnaireSubjectStateFlow.value


    private var coursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(COURSES_OF_STUDIES_IDS_KEY, parsedCourseOfStudiesIds)

    val coursesOfStudiesStateFlow = coursesOfStudiesIdsMutableStateFlow
        .map(localRepository::getCoursesOfStudiesWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val courseOfStudiesIds get() = coursesOfStudiesIdsMutableStateFlow.value


    private val questionsWithAnswersMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_QUESTIONS_KEY, parsedQuestionsWithAnswers)

    val questionsWithAnswersStateFlow = questionsWithAnswersMutableStateFlow.asStateFlow()

    private val questionsWithAnswers get() = questionsWithAnswersMutableStateFlow.value


    private fun setCoursesOfStudiesIds(courseOfStudiesIds: List<String>) {
        courseOfStudiesIds.toMutableSet().let {
            state.set(COURSES_OF_STUDIES_IDS_KEY, it)
            coursesOfStudiesIdsMutableStateFlow.value = it
        }
    }

    private fun setQuestionWithAnswers(questionsWithAnswers: List<QuestionWithAnswers>) {
        questionsWithAnswers.toMutableList().let {
            state.set(QUESTIONNAIRE_QUESTIONS_KEY, it)
            questionsWithAnswersMutableStateFlow.value = it
        }
    }


    fun onCourseOfStudiesUpdated(courseOfStudiesIds: Array<String>) {
        setCoursesOfStudiesIds(courseOfStudiesIds.toList())
    }

    fun onCourseOfStudiesButtonClicked() {
        launch(IO) {
            addEditQuestionnaireEventChannel.send(NavigateToCourseOfStudiesSelector(coursesOfStudiesIdsMutableStateFlow.value.toTypedArray()))
        }
    }

    fun onTitleCardClicked() {
        launch {
            addEditQuestionnaireEventChannel.send(
                NavigateToUpdateStringDialog(
                    questionnaireTitle,
                    UpdateStringType.QUESTIONNAIRE_TITLE
                )
            )
        }
    }

    fun onSubjectCardClicked() {
        launch {
            addEditQuestionnaireEventChannel.send(
                NavigateToUpdateStringDialog(
                    questionnaireSubject,
                    UpdateStringType.QUESTIONNAIRE_SUBJECT
                )
            )
        }
    }

    fun onTitleUpdated(newTitle: String) {
        questionnaireTitleMutableStateFlow.value = newTitle
    }

    fun onSubjectUpdated(newSubject: String) {
        questionnaireSubjectMutableStateFlow.value = newSubject
    }

    fun onQuestionWithAnswerUpdated(position: Int, questionWithAnswers: QuestionWithAnswers) {
        questionsWithAnswers.toMutableList().apply {
            if (position == size) add(questionWithAnswers) else set(position, questionWithAnswers)
            setQuestionWithAnswers(this)
        }
    }

    fun onAddQuestionButtonClicked() {
        launch(IO) {
            addEditQuestionnaireEventChannel.send(NavigateToAddEditQuestionScreenEvent(questionsWithAnswers.size))
        }
    }

    fun onQuestionItemClicked(position: Int) {
        launch(IO) {
            addEditQuestionnaireEventChannel.send(NavigateToAddEditQuestionScreenEvent(position, questionsWithAnswers[position]))
        }
    }

    private fun onQuestionItemDelete(position: Int) {
        launch(IO) {
            questionsWithAnswers.toMutableList().apply {
                removeAt(position).let {
                    addEditQuestionnaireEventChannel.send(ShowQuestionDeletedSnackBarEvent(position, it))
                }
                setQuestionWithAnswers(this)
            }
        }
    }

    fun onQuestionItemDragged(from: Int, to: Int) {
        questionsWithAnswers.toMutableList().apply {
            add(to, removeAt(from))
            setQuestionWithAnswers(this)
        }
    }

    fun onQuestionItemSwiped(position: Int) {
        onQuestionItemDelete(position)
    }


    fun onUndoDeleteQuestionClicked(event: ShowQuestionDeletedSnackBarEvent) {
        launch(IO) {
            questionsWithAnswers.toMutableList().apply {
                add(event.questionPosition, event.questionWithAnswers)
                setQuestionWithAnswers(this)
            }
        }
    }

    fun onMoreOptionsClicked() {
        launch(IO) {
            addEditQuestionnaireEventChannel.send(ShowPopupMenu)
        }
    }


    fun onSaveButtonClicked() = launch(IO, applicationScope) {
        if (!isInputValid()) return@launch

        val questionnaire = Questionnaire(
            id = parsedQuestionnaireId,
            title = questionnaireTitle,
            authorInfo = preferencesRepository.user.asAuthorInfo,
            subject = questionnaireSubject,
            syncStatus = SYNCING
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
        addEditQuestionnaireEventChannel.send(NavigateBackEvent)

        runCatching {
            localRepository.findCompleteQuestionnaireWith(parsedQuestionnaireId)!!.let(DataMapper::mapRoomQuestionnaireToMongoQuestionnaire).let {
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
            addEditQuestionnaireEventChannel.send(ShowMessageSnackBarEvent(R.string.errorQuestionnaireHasNoTitle))
            return false
        }

        if (questionnaireSubject.isEmpty()) {
            addEditQuestionnaireEventChannel.send(ShowMessageSnackBarEvent(R.string.errorQuestionnaireHasNoSubject))
            return false
        }

        return true
    }


    sealed class AddEditQuestionnaireEvent {
        object NavigateBackEvent : AddEditQuestionnaireEvent()
        class NavigateToCourseOfStudiesSelector(val courseOfStudiesIds: Array<String>) : AddEditQuestionnaireEvent()
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: UpdateStringType) : AddEditQuestionnaireEvent()
        class NavigateToAddEditQuestionScreenEvent(val position: Int, val questionWithAnswers: QuestionWithAnswers? = null) : AddEditQuestionnaireEvent()
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int) : AddEditQuestionnaireEvent()
        class ShowQuestionDeletedSnackBarEvent(val questionPosition: Int, val questionWithAnswers: QuestionWithAnswers) : AddEditQuestionnaireEvent()
        object ShowPopupMenu : AddEditQuestionnaireEvent()
    }

    companion object {
        private const val QUESTIONNAIRE_TITLE_KEY = "questionnaireTitleKey"
        private const val QUESTIONNAIRE_SUBJECT_KEY = "questionnaireSubjectKey"
        private const val QUESTIONNAIRE_QUESTIONS_KEY = "questionsWithAnswersKey"
        private const val COURSES_OF_STUDIES_IDS_KEY = "coursesOfStudiesIdsKey"
    }
}