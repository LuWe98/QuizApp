package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.AddEditNavGraphArgs
import com.example.quizapp.extensions.div
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.view.fragments.dialogs.stringupdatedialog.DfUpdateStringValueType
import com.example.quizapp.viewmodel.VmAddEditQuestionnaire.FragmentAddEditEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
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

    val questionnaireTitle get() = questionnaireTitleStateFlow.value

    private var questionnaireSubjectMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_SUBJECT_KEY, parsedQuestionnaireSubject)

    val questionnaireSubjectStateFlow get() = questionnaireSubjectMutableStateFlow.asStateFlow()

    val questionnaireSubject get() = questionnaireSubjectStateFlow.value


    //TODO -> Faculty kommt im CompleteQuestionnaire wenn man den reinladed, nachem man speichern gedrückt hat!
    //TODO -> Um sie dann online zu speichern und in MongoQuestionnaire umzuwandeln
    private var coursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(COURSES_OF_STUDIES_IDS_KEY, parsedCourseOfStudiesIds)

    val coursesOfStudiesStateFlow = coursesOfStudiesIdsMutableStateFlow
        .flatMapLatest(localRepository::getCoursesOfStudiesFlowWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    private val questionsWithAnswersMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_QUESTIONS_KEY, parsedQuestionsWithAnswers)

    val questionsWithAnswersStateFlow = questionsWithAnswersMutableStateFlow.asStateFlow()

    val questionsWithAnswers get() = questionsWithAnswersMutableStateFlow.value



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


    sealed class FragmentAddEditEvent {
        class NavigateToCourseOfStudiesSelector(val courseOfStudiesIds: Set<String>) : FragmentAddEditEvent()
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: DfUpdateStringValueType) : FragmentAddEditEvent()
        class NavigateToAddEditQuestionScreenEvent(val position: Int, val questionWithAnswers: QuestionWithAnswers? = null): FragmentAddEditEvent()
    }

    companion object {
        private const val QUESTIONNAIRE_TITLE_KEY = "questionnaireTitleKey"
        private const val QUESTIONNAIRE_SUBJECT_KEY = "questionnaireSubjectKey"
        private const val QUESTIONNAIRE_QUESTIONS_KEY = "questionsWithAnswersKey"
        private const val COURSES_OF_STUDIES_IDS_KEY = "coursesOfStudiesIdsKey"
    }
}