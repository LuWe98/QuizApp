package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

//
//
////TODO -> Den title des Screens Ändern von Add in Edit oder Add, je nachdem on ein CompleteQuestionnaire geparsed wurde oder nicht
//@HiltViewModel
class VmAddEdit @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {
//
//    private val args = AddNavGraphArgs.fromSavedStateHandle(state)
//
//    private val fragmentAddQuestionnaireEventChannel = Channel<FragmentAddQuestionnaireEvent>()
//
//    val fragmentAddQuestionnaireEventChannelFlow get() = fragmentAddQuestionnaireEventChannel.receiveAsFlow()
//
//    private var qId = state.get<String>(QUESTIONNAIRE_ID) ?: ObjectId().toString()
//        set(value) {
//            state.set(QUESTIONNAIRE_ID, value)
//            field = value
//        }
//
//    var qTitle = state.get<String>(QUESTIONNAIRE_TITLE) ?: ""
//        set(value) {
//            state.set(QUESTIONNAIRE_TITLE, value)
//            field = value
//        }
//
//    var qFaculties = state.get<List<Faculty>>(QUESTIONNAIRE_FACULTY) ?: emptyList()
//        set(value) {
//            state.set(QUESTIONNAIRE_FACULTY, value)
//            field = value
//        }
//
//    var qCoursesOfStudies = state.get<List<CourseOfStudies>>(QUESTIONNAIRE_COURSE_OF_STUDIES) ?: emptyList()
//        set(value) {
//            state.set(QUESTIONNAIRE_COURSE_OF_STUDIES, value)
//            field = value
//        }
//
//    var qSubject = state.get<String>(QUESTIONNAIRE_SUBJECT) ?: ""
//        set(value) {
//            state.set(QUESTIONNAIRE_SUBJECT, value)
//            field = value
//        }
//
//    private val questionsWithAnswersMutableLiveData = state.getLiveData<MutableList<QuestionWithAnswers>>(QUESTIONS, mutableListOf())
//
//    val questionsWithAnswersLiveData: LiveData<MutableList<QuestionWithAnswers>> = questionsWithAnswersMutableLiveData.map {
//        mutableListOf<QuestionWithAnswers>().apply { addAll(it) }
//    }.distinctUntilChanged()
//
//    private val questionsWithAnswersLiveDataValue get() = questionsWithAnswersMutableLiveData.value!!
//
//    init {
//        args.completeQuestionnaire?.let {
//            qId = if (args.copy) ObjectId().toString() else it.questionnaire.id
//            qTitle = it.questionnaire.title
//            qFaculties = it.allFaculties
//            qCoursesOfStudies = it.allCoursesOfStudies
//            qSubject = it.questionnaire.subject
//            setQuestionWithAnswers(it.questionsWithAnswers.sortedBy { qwa -> qwa.question.questionPosition }.toMutableList())
//        }
//    }
//
//    fun providePageTitle(): Int {
//        return if (args.completeQuestionnaire == null) R.string.addQuestionnaire else if (args.copy) R.string.copyQuestionnaire else R.string.editQuestionnaire
//    }
//
//
//    private fun setQuestionWithAnswers(list: List<QuestionWithAnswers>) {
//        questionsWithAnswersMutableLiveData.value = list.toMutableList()
//        state.set(QUESTIONS, list)
//    }
//
//    private fun addQuestionWithAnswers(qwa: QuestionWithAnswers) {
//        questionsWithAnswersLiveDataValue.apply {
//            add(qwa)
//        }.also {
//            setQuestionWithAnswers(it)
//        }
//    }
//
//    private fun removeQuestionWithAnswersFromList(qwa: QuestionWithAnswers) {
//        mutableListOf<QuestionWithAnswers>().let { lastList ->
//            questionsWithAnswersLiveDataValue.apply {
//                lastList.addAll(this)
//                remove(qwa)
//            }.also {
//                setQuestionWithAnswers(it)
//            }
//
//            launch(IO) {
//                fragmentAddQuestionnaireEventChannel.send(ShowQuestionDeletedSuccessFullySnackBar(lastList))
//            }
//        }
//    }
//
//
//    fun onSaveSpecificQuestionClicked(event: VmAddEditQuestion.FragmentEditQuestionEvent.SendUpdateRequestToVmAdd) {
//        questionsWithAnswersLiveDataValue.apply {
//            set(event.position, event.newQuestionWithAnswers)
//        }.also {
//            setQuestionWithAnswers(it)
//        }
//    }
//
//    fun onQuestionItemSwiped(position: Int) {
//        removeQuestionWithAnswersFromList(questionsWithAnswersLiveDataValue[position])
//    }
//
//    fun onQuestionItemDragReleased(questions: List<QuestionWithAnswers>) {
//        questionsWithAnswersMutableLiveData.value = questions.mapIndexed { index, qwa ->
//            qwa.copy(question = qwa.question.copy(questionPosition = index))
//        }.also {
//            setQuestionWithAnswers(it)
//        }.toMutableList()
//    }
//
//    fun onUndoDeleteQuestionClicked(event: ShowQuestionDeletedSuccessFullySnackBar) {
//        setQuestionWithAnswers(event.lastListValues)
//    }
//
//    fun onQuestionnaireTitleTextChanged(text: String) {
//        qTitle = text
//    }
//
//    fun onCourseOfStudiesSelected(coursesOfStudies: List<CourseOfStudies>) {
//        qCoursesOfStudies = coursesOfStudies
//    }
//
//    fun onFacultySelected(faculties: List<Faculty>) {
//        qFaculties = faculties
//    }
//
//    fun onQuestionnaireSubjectTextChanged(text: String) {
//        qSubject = text
//    }
//
//    fun onAddQuestionButtonClicked() {
//        addQuestionWithAnswers(QuestionWithAnswers.createEmptyQuestionWithAnswers())
//    }
//
//    //TODO --> Faculty ist noch WIP / Auch vom User ?
//    fun onFabSaveClicked() {
//        if (!isInputValid()) return
//
//        applicationScope.launch(IO) {
//            val questionnaire = Questionnaire(
//                id = qId,
//                title = qTitle,
//                authorInfo = preferencesRepository.user.asAuthorInfo,
//                lastModifiedTimestamp = getTimeMillis(),
//                subject = qSubject,
//                syncStatus = SYNCING
//            )
//
//            val questionsWithAnswersMapped = questionsWithAnswersLiveDataValue.onEachIndexed { questionIndex, qwa ->
//                val questionId = if (args.copy) ObjectId().toString() else qwa.question.id
//
//                qwa.question.apply {
//                    id = questionId
//                    questionnaireId = qId
//                    questionPosition = questionIndex
//                }
//
//                //TODO -> Schauen ob es das wirklich braucht oder nicht | Sollte immer jede Frage resettet werden?
//                val setIsSelectedToFalse = (!qwa.question.isMultipleChoice && qwa.selectedAnswerIds.size > 1) || args.copy
//
//                qwa.answers = qwa.answers.mapIndexed { answerIndex, answer ->
//                    answer.copy(
//                        id = if (args.copy) ObjectId().toString() else answer.id,
//                        questionId = questionId,
//                        isAnswerSelected = if (setIsSelectedToFalse) false else answer.isAnswerSelected,
//                        answerPosition = answerIndex
//                    )
//                }
//            }
//
//            //TODO -> Courses of Studies list mapping anschauen !
//            val coursesOfStudiesWithFaculties = mutableListOf<CourseOfStudiesWithFaculties>().apply {
//                qCoursesOfStudies.forEach {
//                    add(CourseOfStudiesWithFaculties(it, emptyList()))
//                }
//            }
//
//            val completeQuestionnaire = CompleteQuestionnaire(questionnaire, questionsWithAnswersMapped, coursesOfStudiesWithFaculties)
//
//            localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
//            fragmentAddQuestionnaireEventChannel.send(NavigateBackEvent)
//
//            runCatching {
//                backendRepository.insertQuestionnaire(DataMapper.mapRoomQuestionnaireToMongoQuestionnaire(completeQuestionnaire))
//            }.onFailure {
//                localRepository.update(questionnaire.apply { syncStatus = UNSYNCED })
//            }.onSuccess {
//                localRepository.update(questionnaire.apply { syncStatus = if (it.responseType == InsertQuestionnairesResponseType.SUCCESSFUL) SYNCED else UNSYNCED })
//            }
//        }
//    }
//
//
//    private fun isInputValid(): Boolean {
//        var position = questionsWithAnswersLiveDataValue.indexOfFirst { it.question.questionText.isEmpty() }
//        if (position != RecyclerView.NO_POSITION) {
//            launch { fragmentAddQuestionnaireEventChannel.send(ShowQuestionDoesNotHaveTitleToast(position)) }
//            return false
//        }
//
//        position = questionsWithAnswersLiveDataValue.indexOfFirst { it.answers.isEmpty() }
//        if (position != RecyclerView.NO_POSITION) {
//            launch { fragmentAddQuestionnaireEventChannel.send(ShowQuestionHasNoAnswersToast(position)) }
//            return false
//        }
//
//        return true
//    }
//
//
//    sealed class FragmentAddQuestionnaireEvent {
//        object NavigateBackEvent : FragmentAddQuestionnaireEvent()
//        data class ShowQuestionHasNoAnswersToast(val position: Int) : FragmentAddQuestionnaireEvent()
//        data class ShowQuestionDoesNotHaveTitleToast(val position: Int) : FragmentAddQuestionnaireEvent()
//        data class ShowQuestionDeletedSuccessFullySnackBar(val lastListValues: List<QuestionWithAnswers>) : FragmentAddQuestionnaireEvent()
//    }
//
//    companion object {
//        const val QUESTIONNAIRE_ID = "questionnaireId"
//        const val QUESTIONNAIRE_TITLE = "questionnaireTitle"
//        const val QUESTIONNAIRE_COURSE_OF_STUDIES = "questionnaireCourseOfStudies"
//        const val QUESTIONNAIRE_FACULTY = "questionnaireFaculty"
//        const val QUESTIONNAIRE_SUBJECT = "questionnaireSubject"
//        const val QUESTIONS = "questionsWithAnswers"
//    }
}