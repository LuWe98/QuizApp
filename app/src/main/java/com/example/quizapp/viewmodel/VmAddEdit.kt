package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.AddNavGraphArgs
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Questionnaire
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.model.room.junctions.QuestionWithAnswers
import com.example.quizapp.viewmodel.VmAddEdit.FragmentAddQuestionnaireEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.date.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import javax.inject.Inject


//TODO -> Den title des Screens Ändern von Add in Edit oder Add, je nachdem on ein CompleteQuestionnaire geparsed wurde oder nicht
@HiltViewModel
class VmAddEdit @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    private val args = AddNavGraphArgs.fromSavedStateHandle(state)

    private val fragmentAddQuestionnaireEventChannel = Channel<FragmentAddQuestionnaireEvent>()

    val fragmentAddQuestionnaireEventChannelFlow get() = fragmentAddQuestionnaireEventChannel.receiveAsFlow()

    private var qId = state.get<String>(QUESTIONNAIRE_ID) ?: ObjectId().toString()
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
        args.completeQuestionnaire?.let {
            qId = it.questionnaire.id
            qTitle = it.questionnaire.title
            qCourseOfStudies = it.questionnaire.courseOfStudies
            qSubject = it.questionnaire.subject
            setQuestionWithAnswers(it.questionsWithAnswers.sortedBy { qwa -> qwa.question.questionPosition }.toMutableList())
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

            launch(IO) {
                fragmentAddQuestionnaireEventChannel.send(ShowQuestionDeletedSuccessFullySnackBar(lastList))
            }
        }
    }


    fun onSaveSpecificQuestionClicked(event: VmAddEditQuestion.FragmentEditQuestionEvent.SendUpdateRequestToVmAdd) {
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

    fun onQuestionnaireTitleTextChanged(text: String) {
        qTitle = text
    }

    fun onQuestionnaireCourseOfStudiesTextChanged(text: String) {
        qCourseOfStudies = text
    }

    fun onQuestionnaireSubjectTextChanged(text: String) {
        qSubject = text
    }

    fun onAddQuestionButtonClicked() {
        addQuestionWithAnswers(QuestionWithAnswers.createEmptyQuestionWithAnswers())
    }

    //TODO --> Faculty ist noch WIP / Auch vom User ?
    fun onFabSaveClicked() {
        if (!isInputValid()) return

        applicationScope.launch(IO) {
            val questionnaire = Questionnaire(
                id = qId,
                title = qTitle,
                authorInfo = preferencesRepository.user.asAuthorInfo,
                lastModifiedTimestamp = getTimeMillis(),
                faculty = "WIB",
                courseOfStudies = qCourseOfStudies,
                subject = qSubject,
                syncStatus = SyncStatus.SYNCING
            )

            val questionsWithAnswersMapped = questionsWithAnswersLiveDataValue.onEachIndexed { questionIndex, qwa ->
                qwa.question.apply {
                    questionnaireId = qId
                    questionPosition = questionIndex
                }

                //TODO -> Schauen ob es das wirklich braucht oder nicht | Sollte immer jede Frage resettet werden?
                val setIsSelectedToFalse = !qwa.question.isMultipleChoice && qwa.selectedAnswerIds.size > 1

                qwa.answers = qwa.answers.mapIndexed { answerIndex, answer ->
                    answer.copy(
                        questionId = qwa.question.id,
                        isAnswerSelected = if(setIsSelectedToFalse) false else answer.isAnswerSelected,
                        answerPosition = answerIndex)
                }
            }

            val completeQuestionnaire = CompleteQuestionnaireJunction(questionnaire, questionsWithAnswersMapped)

            localRepository.insertCompleteQuestionnaire(completeQuestionnaire)
            fragmentAddQuestionnaireEventChannel.send(NavigateBackEvent)

            runCatching {
                backendRepository.insertQuestionnaire(DataMapper.mapSqlEntitiesToMongoEntity(completeQuestionnaire))
            }.onFailure {
                localRepository.update(questionnaire.apply { syncStatus = SyncStatus.UNSYNCED })
            }.onSuccess {
                localRepository.update(questionnaire.apply { syncStatus = if (it.isSuccessful) SyncStatus.SYNCED else SyncStatus.UNSYNCED })
            }
        }
    }


    private fun isInputValid(): Boolean {
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