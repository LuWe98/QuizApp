package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.AddNavGraphArgs
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.viewmodel.VmAdd.FragmentAddQuestionnaireEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class VmAdd @Inject constructor(
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = AddNavGraphArgs.fromSavedStateHandle(state)

    private val fragmentAddQuestionnaireEventChannel = Channel<FragmentAddQuestionnaireEvent>()

    val fragmentAddQuestionnaireEventChannelFlow get() = fragmentAddQuestionnaireEventChannel.receiveAsFlow()

    private var qId = state.get<String>(QUESTIONNAIRE_ID) ?: ""
        set(value) {
            state.set(QUESTIONNAIRE_ID, value)
            field = value
        }

    var qTitle = state.get<String>(QUESTIONNAIRE_TITLE) ?: ""
        set(value) {
            state.set(QUESTIONNAIRE_TITLE, value)
            field = value
        }

    var qCourseOfStudies = state.get<String>(QUESTIONNAIRE_COURSE_OF_STUDIES) ?: ""
        set(value) {
            state.set(QUESTIONNAIRE_COURSE_OF_STUDIES, value)
            field = value
        }

    var qSubject = state.get<String>(QUESTIONNAIRE_SUBJECT) ?: ""
        set(value) {
            state.set(QUESTIONNAIRE_SUBJECT, value)
            field = value
        }

    private val questionsWithAnswersMutableLiveData = state.getLiveData<MutableList<QuestionWithAnswers>>(QUESTIONS, mutableListOf())

    val questionsWithAnswersLiveData: LiveData<MutableList<QuestionWithAnswers>> = questionsWithAnswersMutableLiveData.map {
        mutableListOf<QuestionWithAnswers>().apply { addAll(it) }
    }.distinctUntilChanged()

    private val questionsWithAnswersLiveDataValue get() = questionsWithAnswersMutableLiveData.value!!

    init {
        args.questionnaireId?.let { id ->
            runBlocking {
                localRepository.getCompleteQuestionnaireWithId(id).apply {
                    qId = id
                    qTitle = questionnaire.title
                    qCourseOfStudies = questionnaire.courseOfStudies
                    qSubject = questionnaire.subject
                    setQuestionWithAnswers(questionsWithAnswers.sortedBy { it.question.questionPosition }.toMutableList())
                }
            }
        }
    }


    private fun setQuestionWithAnswers(list: List<QuestionWithAnswers>) {
        questionsWithAnswersMutableLiveData.value = list.toMutableList()
        state.set(QUESTIONS, list)
    }

    private fun addQuestionWithAnswers(qwa: QuestionWithAnswers) {
        questionsWithAnswersLiveDataValue.apply {
            add(qwa)
        }.also {
            setQuestionWithAnswers(it)
        }
    }

    private fun removeQuestionWithAnswersFromList(qwa: QuestionWithAnswers) {
        mutableListOf<QuestionWithAnswers>().let { lastList ->
            questionsWithAnswersLiveDataValue.apply {
                lastList.addAll(this)
                remove(qwa)
            }.also {
                setQuestionWithAnswers(it)
            }

            launch {
                fragmentAddQuestionnaireEventChannel.send(ShowQuestionDeletedSuccessFullySnackBar(lastList))
            }
        }
    }

    fun onSaveSpecificQuestionClicked(event: VmEditQuestion.FragmentEditQuestionEvent.SendUpdateRequestToVmAdd) {
        questionsWithAnswersLiveDataValue.apply {
            set(event.position, event.newQuestionWithAnswers)
        }.also {
            setQuestionWithAnswers(it)
        }
    }

    fun onQuestionItemSwiped(position: Int) {
        removeQuestionWithAnswersFromList(questionsWithAnswersLiveDataValue[position])
    }

    fun onQuestionItemDragReleased(questions: List<QuestionWithAnswers>) {
        questionsWithAnswersMutableLiveData.value = questions.mapIndexed { index, qwa ->
            qwa.copy(question = qwa.question.copy(questionPosition = index))
        }.also {
            setQuestionWithAnswers(it)
        }.toMutableList()
    }

    fun onUndoDeleteQuestionClicked(event: ShowQuestionDeletedSuccessFullySnackBar) {
        setQuestionWithAnswers(event.lastListValues)
    }

    fun onQuestionnaireTitleTextChanged(text : String){
        qTitle = text
    }

    fun onQuestionnaireCourseOfStudiesTextChanged(text : String){
        qCourseOfStudies = text
    }

    fun onQuestionnaireSubjectTextChanged(text : String){
        qSubject = text
    }

    fun onAddQuestionButtonClicked() {
        addQuestionWithAnswers(QuestionWithAnswers.createEmptyQuestionWithAnswers())
    }

    fun onFabSaveClicked() {
        if(!isInputValid()){ return }

        //TODO --> Author ist der momentan User der in der App angemeldet ist
        //TODO --> Faculty ist noch WIP / Auch vom User ?
        launch(viewModelScope, Dispatchers.IO) {
            val questionnaire = Questionnaire(
                id = qId,
                title = qTitle,
                author = "Luca",
                faculty = "WIB",
                courseOfStudies = qCourseOfStudies,
                subject = qSubject)

            localRepository.deleteQuestionsWith(qId)
            localRepository.insert(questionnaire)?.let { questionnaireId ->
                questionsWithAnswersLiveDataValue.forEachIndexed { index, qwa ->
                    localRepository.insert(qwa.question.copy(questionnaireId = qId, questionPosition = index))?.let {  questionId ->
                        localRepository.insert(qwa.answers.map { it.copy(questionId = qwa.question.id, isAnswerSelected = false) })
                    }
                }
            }

            fragmentAddQuestionnaireEventChannel.send(NavigateBackEvent)
        }
    }

    private fun isInputValid() : Boolean{
        var position = questionsWithAnswersLiveDataValue.indexOfFirst { it.question.questionText.isEmpty() }
        if (position != RecyclerView.NO_POSITION) {
            launch { fragmentAddQuestionnaireEventChannel.send(ShowQuestionDoesNotHaveTitleToast(position)) }
            return false
        }

        position = questionsWithAnswersLiveDataValue.indexOfFirst { it.answers.isEmpty() }
        if (position != RecyclerView.NO_POSITION) {
            launch { fragmentAddQuestionnaireEventChannel.send(ShowQuestionHasNoAnswersToast(position)) }
            return false
        }

        return true
    }


    sealed class FragmentAddQuestionnaireEvent {
        object NavigateBackEvent : FragmentAddQuestionnaireEvent()
        data class ShowQuestionHasNoAnswersToast(val position: Int) : FragmentAddQuestionnaireEvent()
        data class ShowQuestionDoesNotHaveTitleToast(val position: Int) : FragmentAddQuestionnaireEvent()
        data class ShowQuestionDeletedSuccessFullySnackBar(val lastListValues: List<QuestionWithAnswers>) : FragmentAddQuestionnaireEvent()
    }

    companion object {
        const val QUESTIONNAIRE_ID = "questionnaireId"
        const val QUESTIONNAIRE_TITLE = "questionnaireTitle"
        const val QUESTIONNAIRE_COURSE_OF_STUDIES = "questionnaireCourseOfStudies"
        const val QUESTIONNAIRE_SUBJECT = "questionnaireSubject"
        const val QUESTIONS = "questionsWithAnswers"
    }
}