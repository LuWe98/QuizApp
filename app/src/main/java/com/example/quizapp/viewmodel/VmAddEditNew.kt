package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.quizapp.view.fragments.test.FragmentTestingArgs
import com.example.quizapp.viewmodel.VmAddEditNew.FragmentAddEditEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


//TODO -> Den title des Screens Ändern von Add in Edit oder Add, je nachdem on ein CompleteQuestionnaire geparsed wurde oder nicht
@HiltViewModel
class VmAddEditNew @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = FragmentTestingArgs.fromSavedStateHandle(state)

    private val fragmentAddEditEventChannel = Channel<FragmentAddEditEvent>()

    val fragmentAddEditEventChannelFlow = fragmentAddEditEventChannel.receiveAsFlow()

    private var questionnaireTitleMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_TITLE_KEY, "-")

    val questionnaireTitleStateFlow get() = questionnaireTitleMutableStateFlow.asStateFlow()

    val questionnaireTitle get() = questionnaireTitleStateFlow.value

    private var questionnaireSubjectMutableStateFlow = state.getMutableStateFlow(QUESTIONNAIRE_SUBJECT_KEY, "-")

    val questionnaireSubjectStateFlow get() = questionnaireSubjectMutableStateFlow.asStateFlow()

    val questionnaireSubject get() = questionnaireSubjectStateFlow.value


    //TODO -> Faculty kommt im CompleteQuestionnaire wenn man den reinladed, nachem man speichern gedrückt hat!
    //TODO -> Um sie dann online zu speichern und in MongoQuestionnaire umzuwandeln
    private var coursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow<MutableSet<String>>(COURSES_OF_STUDIES_IDS_KEY, mutableSetOf())

    private val questionsWithAnswersMutableStateFlow = state.getMutableStateFlow<MutableList<QuestionWithAnswers>>(QUESTIONNAIRE_QUESTIONS_KEY, mutableListOf())

    init {
        runBlocking {
            localRepository.findCompleteQuestionnaireWith("6192426746a8092ef8dcf0e8")?.let {
                questionnaireTitleMutableStateFlow.value = it.questionnaire.title
                questionnaireSubjectMutableStateFlow.value = it.questionnaire.subject
                setCoursesOfStudiesIds(it.allCoursesOfStudies.map(CourseOfStudies::id))
                setQuestionWithAnswers(it.questionsWithAnswers.sortedBy(QuestionWithAnswers::question / Question::questionPosition).toMutableList())
            }
        }

//        args.completeQuestionnaire?.let {
//            qTitle = it.questionnaire.title
//            qSubject = it.questionnaire.subject
//            setCoursesOfStudiesIds(it.allCoursesOfStudies.map(CourseOfStudies::id))
//            setQuestionWithAnswers(it.questionsWithAnswers.sortedBy { qwa -> qwa.question.questionPosition }.toMutableList())
//        } ?: run {
//            setCoursesOfStudiesIds(runBlocking(IO) {
//                preferencesRepository.getPreferredCourseOfStudiesId().toList()
//            })
//        }
    }

    val coursesOfStudiesStateFlow = coursesOfStudiesIdsMutableStateFlow
        .flatMapLatest(localRepository::getCoursesOfStudiesFlowWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val questionsWithAnswersStateFlow = questionsWithAnswersMutableStateFlow.asStateFlow()

    val questionsWithAnswersValue get() = questionsWithAnswersMutableStateFlow.value


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


    sealed class FragmentAddEditEvent {
        class NavigateToCourseOfStudiesSelector(val courseOfStudiesIds: Set<String>) : FragmentAddEditEvent()
        class NavigateToUpdateStringDialog(val initialValue: String, val updateType: DfUpdateStringValueType) : FragmentAddEditEvent()
    }

    companion object {
        private const val QUESTIONNAIRE_TITLE_KEY = "questionnaireTitleKey"
        private const val QUESTIONNAIRE_SUBJECT_KEY = "questionnaireSubjectKey"
        private const val QUESTIONNAIRE_QUESTIONS_KEY = "questionsWithAnswersKey"
        private const val COURSES_OF_STUDIES_IDS_KEY = "coursesOfStudiesIdsKey"
    }
}