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
import com.example.quizapp.model.databases.room.junctions.CourseOfStudiesWithFaculties
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.responses.InsertQuestionnairesResponse
import com.example.quizapp.model.ktor.responses.InsertQuestionnairesResponse.*
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.ktor.status.SyncStatus.*
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateStringValueType
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.FragmentAddEditEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import javax.inject.Inject


//TODO -> Den title des Screens Ändern von Add in Edit oder Add, je nachdem on ein CompleteQuestionnaire geparsed wurde oder nicht
@HiltViewModel
class VmAddEditQuestionnaire @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = AddEditNavGraphArgs.fromSavedStateHandle(state)

    private val parsedQuestionnaireId = args.completeQuestionnaire?.questionnaire?.id ?: ObjectId().toHexString()

    private val parsedQuestionnaireTitle get() = args.completeQuestionnaire?.questionnaire?.title ?: ""

    private val parsedQuestionnaireSubject get() = args.completeQuestionnaire?.questionnaire?.subject ?: ""

    private val parsedCourseOfStudiesIds get() = args.completeQuestionnaire?.allCoursesOfStudies
        ?.map(CourseOfStudies::id)?.toMutableSet()
        ?: runBlocking(IO) { preferencesRepository.getPreferredCourseOfStudiesId() }

    private val parsedQuestionsWithAnswers get() = args.completeQuestionnaire?.questionsWithAnswers
        ?.sortedBy(QuestionWithAnswers::question / Question::questionPosition)
        ?.toMutableList()
        ?: mutableListOf()


    private val fragmentAddEditEventChannel = Channel<FragmentAddEditEvent>()

    val fragmentAddEditEventChannelFlow = fragmentAddEditEventChannel.receiveAsFlow()

    private var questionnaireTitleMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_TITLE_KEY, parsedQuestionnaireTitle)

    val questionnaireTitleStateFlow get() = questionnaireTitleMutableStateFlow.asStateFlow()

    private val questionnaireTitle get() = questionnaireTitleStateFlow.value

    private var questionnaireSubjectMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_SUBJECT_KEY, parsedQuestionnaireSubject)

    val questionnaireSubjectStateFlow get() = questionnaireSubjectMutableStateFlow.asStateFlow()

    private val questionnaireSubject get() = questionnaireSubjectStateFlow.value


    //TODO -> Faculty kommt im CompleteQuestionnaire wenn man den reinladed, nachem man speichern gedrückt hat!
    //TODO -> Um sie dann online zu speichern und in MongoQuestionnaire umzuwandeln
    private var coursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(COURSES_OF_STUDIES_IDS_KEY, parsedCourseOfStudiesIds)

    val coursesOfStudiesStateFlow = coursesOfStudiesIdsMutableStateFlow
        .flatMapLatest(localRepository::getCoursesOfStudiesFlowWithIds)
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


    fun onFragmentResultReceived(courseOfStudiesIds: Array<String>) {
        setCoursesOfStudiesIds(courseOfStudiesIds.toList())
    }

    fun onCourseOfStudiesButtonClicked() {
        launch(IO) {
            fragmentAddEditEventChannel.send(NavigateToCourseOfStudiesSelector(coursesOfStudiesIdsMutableStateFlow.value))
        }
    }

    fun onTitleCardClicked() {
        launch {
            fragmentAddEditEventChannel.send(
                NavigateToUpdateStringDialog(
                    questionnaireTitle,
                    DfUpdateStringValueType.QUESTIONNAIRE_TITLE
                )
            )
        }
    }

    fun onSubjectCardClicked() {
        launch {
            fragmentAddEditEventChannel.send(
                NavigateToUpdateStringDialog(
                    questionnaireSubject,
                    DfUpdateStringValueType.QUESTIONNAIRE_SUBJECT
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
            if(position == size) add(questionWithAnswers) else set(position, questionWithAnswers)
            setQuestionWithAnswers(this)
        }
    }

    fun onAddQuestionButtonClicked(){
        launch(IO)  {
            fragmentAddEditEventChannel.send(NavigateToAddEditQuestionScreenEvent(questionsWithAnswers.size))
        }
    }

    fun onQuestionItemClicked(position: Int, questionWithAnswers:  QuestionWithAnswers) {
        launch(IO) {
            fragmentAddEditEventChannel.send(NavigateToAddEditQuestionScreenEvent(position, questionWithAnswers))
        }
    }


    fun onSaveButtonClicked(){
        launch(IO, applicationScope) {
            if(questionnaireTitle.isEmpty()) {
                fragmentAddEditEventChannel.send(ShowMessageSnackBarEvent(R.string.errorQuestionnaireHasNoTitle))
                return@launch
            }

            if(questionnaireSubject.isEmpty()) {
                fragmentAddEditEventChannel.send(ShowMessageSnackBarEvent(R.string.errorQuestionnaireHasNoSubject))
                return@launch
            }

            val questionnaire = Questionnaire(
                id = parsedQuestionnaireId,
                title = questionnaireTitle,
                authorInfo = preferencesRepository.user.asAuthorInfo,
                subject = questionnaireSubject,
                syncStatus = SYNCING
            )

            val questionsWithAnswersMapped = questionsWithAnswers.onEachIndexed { questionIndex, qwa ->
                val questionId = if (args.copy) ObjectId().toHexString() else qwa.question.id

                qwa.question.apply {
                    id = questionId
                    questionnaireId = parsedQuestionnaireId
                    questionPosition = questionIndex
                }

                val setIsSelectedToFalse = (!qwa.question.isMultipleChoice && qwa.selectedAnswerIds.size > 1) || args.copy

                qwa.answers = qwa.answers.mapIndexed { answerIndex, answer ->
                    answer.copy(
                        id = if (args.copy) ObjectId().toHexString() else answer.id,
                        questionId = questionId,
                        isAnswerSelected = if (setIsSelectedToFalse) false else answer.isAnswerSelected,
                        answerPosition = answerIndex
                    )
                }
            }

            localRepository.insertCompleteQuestionnaire(CompleteQuestionnaire(questionnaire, questionsWithAnswersMapped, emptyList()))
            localRepository.insert(courseOfStudiesIds.map { cosId -> QuestionnaireCourseOfStudiesRelation(parsedQuestionnaireId, cosId) })
            fragmentAddEditEventChannel.send(NavigateBackEvent)

            runCatching {
                localRepository.findCompleteQuestionnaireWith(parsedQuestionnaireId)!!.let(DataMapper::mapRoomQuestionnaireToMongoQuestionnaire).let {
                    backendRepository.insertQuestionnaire(it)
                }
            }.onFailure {
                localRepository.update(questionnaire.apply { syncStatus = UNSYNCED })
            }.onSuccess {
                localRepository.update(questionnaire.apply { syncStatus = if (it.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) SYNCED else UNSYNCED })
            }
        }
    }


    sealed class FragmentAddEditEvent {
        object NavigateBackEvent: FragmentAddEditEvent()
        class NavigateToCourseOfStudiesSelector(val courseOfStudiesIds: Set<String>) : FragmentAddEditEvent()
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: DfUpdateStringValueType) : FragmentAddEditEvent()
        class NavigateToAddEditQuestionScreenEvent(val position: Int, val questionWithAnswers: QuestionWithAnswers? = null): FragmentAddEditEvent()
        class ShowMessageSnackBarEvent(@StringRes val messageRes: Int): FragmentAddEditEvent()
        class ShowQuestionDeletedSnackBarEvent(val questionPosition: Int, val questionWithAnswers: QuestionWithAnswers): FragmentAddEditEvent()
    }

    companion object {
        private const val QUESTIONNAIRE_TITLE_KEY = "questionnaireTitleKey"
        private const val QUESTIONNAIRE_SUBJECT_KEY = "questionnaireSubjectKey"
        private const val QUESTIONNAIRE_QUESTIONS_KEY = "questionsWithAnswersKey"
        private const val COURSES_OF_STUDIES_IDS_KEY = "coursesOfStudiesIdsKey"
    }
}