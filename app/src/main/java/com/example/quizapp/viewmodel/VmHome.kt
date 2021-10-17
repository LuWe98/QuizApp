package com.example.quizapp.viewmodel

import androidx.lifecycle.*
import com.example.quizapp.R
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.DataMapper
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.model.ktor.status.SyncStatus
import com.example.quizapp.model.room.LocalRepository
import com.example.quizapp.model.room.entities.Answer
import com.example.quizapp.model.room.entities.sync.LocallyDeletedFilledQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDeletedQuestionnaire
import com.example.quizapp.model.room.entities.sync.LocallyDownloadedQuestionnaire
import com.example.quizapp.model.room.junctions.CompleteQuestionnaireJunction
import com.example.quizapp.utils.BackendSyncHelper
import com.example.quizapp.viewmodel.VmHome.FragmentHomeCachedEvent.*
import com.example.quizapp.viewmodel.VmHome.FragmentHomeCreatedEvent.*
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@HiltViewModel
class VmHome @Inject constructor(
    private val applicationScope: CoroutineScope,
    private val localRepository: LocalRepository,
    private val preferencesRepository: PreferencesRepository,
    private val backendRepository: BackendRepository,
    private val backendSyncHelper: BackendSyncHelper
) : ViewModel() {

    private val fragmentHomeEventChannel = Channel<FragmentHomeEvent>()
    private val fragmentHomeCachedEventChannel = Channel<FragmentHomeCachedEvent>()
    private val fragmentHomeCreatedEventChannel = Channel<FragmentHomeCreatedEvent>()

    val fragmentHomeEventChannelLD get() = fragmentHomeEventChannel.receiveAsFlow().asLiveData()
    val fragmentHomeCachedEventChannelLD get() = fragmentHomeCachedEventChannel.receiveAsFlow().asLiveData()
    val fragmentHomeCreatedEventChannelLD get() = fragmentHomeCreatedEventChannel.receiveAsFlow().asLiveData()

    init {
        applicationScope.launch(IO) {
//            fragmentHomeEventChannel.send(ChangeProgressVisibility(true))
            fragmentHomeCachedEventChannel.send(ChangeCachedSwipeRefreshLayoutVisibility(true))
            fragmentHomeCreatedEventChannel.send(ChangeCreatedSwipeRefreshLayoutVisibility(true))
            backendSyncHelper.syncData()
            delay(500)
            fragmentHomeCachedEventChannel.send(ChangeCachedSwipeRefreshLayoutVisibility(false))
            fragmentHomeCreatedEventChannel.send(ChangeCreatedSwipeRefreshLayoutVisibility(false))
//            fragmentHomeEventChannel.send(ChangeProgressVisibility(false))
        }
    }


//    private val userInfo = preferencesRepository.userInfoFlow.first(IO)
//    val allCachedQuestionnairesLD = localRepository.findAllCompleteQuestionnairesNotForUserFlow(userInfo.id).asLiveData().distinctUntilChanged()
//    val allCreatedQuestionnairesLD = localRepository.findAllCompleteQuestionnairesForUserFlow(userInfo.id).asLiveData().distinctUntilChanged()

    private val userInfoFlow = preferencesRepository.userInfoFlow

    val allCachedQuestionnairesLD = userInfoFlow.flatMapLatest {
        localRepository.findAllCompleteQuestionnairesNotForUserFlow(it.id)
    }.asLiveData().distinctUntilChanged()

    val allCreatedQuestionnairesLD = userInfoFlow.flatMapLatest {
        localRepository.findAllCompleteQuestionnairesForUserFlow(it.id)
    }.asLiveData().distinctUntilChanged()






    fun onSwipeRefreshCachedQuestionnairesList() = launch(IO) {
        backendSyncHelper.synAllQuestionnaireData()
        fragmentHomeCachedEventChannel.send(ChangeCachedSwipeRefreshLayoutVisibility(false))
    }

    fun onSwipeRefreshCreatedQuestionnairesList() = launch(IO) {
        backendSyncHelper.synAllQuestionnaireData()
        fragmentHomeCreatedEventChannel.send(ChangeCreatedSwipeRefreshLayoutVisibility(false))
    }


    fun onCreatedItemSyncButtonClicked(questionnaireId: String) {
        applicationScope.launch(IO) {
            val completeQuestionnaire = localRepository.findCompleteQuestionnaireWith(questionnaireId)!!
            localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.SYNCING })

            val result = try {
                backendRepository.insertQuestionnaire(completeQuestionnaire)
            } catch (e: Exception) {
                null
            }

            if (result != null && result.isSuccessful) {
                localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.SYNCED })
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.syncSuccessful))
            } else {
                localRepository.update(completeQuestionnaire.questionnaire.apply { syncStatus = SyncStatus.UNSYNCED })
                fragmentHomeEventChannel.send(ShowSnackBarMessageBar(R.string.syncUnsuccessful))
            }
        }
    }



    // DELETE CREATED QUESTIONNAIRE
    fun deleteCreatedQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            fragmentHomeEventChannel.send(ShowUndoDeleteCreatedQuestionnaireSnackBar(it))
        }
        localRepository.insert(LocallyDeletedQuestionnaire.asOwner(questionnaireId))
        localRepository.deleteQuestionnaireWith(questionnaireId)
    }

    fun onDeleteCreatedQuestionnaireConfirmed(event: ShowUndoDeleteCreatedQuestionnaireSnackBar) = launch(IO) {
        val questionnaireId = event.completeQuestionnaire.questionnaire.id

        runCatching {
            backendRepository.deleteQuestionnaire(listOf(questionnaireId))
        }.onSuccess {
            if (it.isSuccessful) {
                localRepository.delete(LocallyDeletedQuestionnaire.asOwner(questionnaireId))
            }
        }
    }

    fun onUndoDeleteCreatedQuestionnaireClicked(event: ShowUndoDeleteCreatedQuestionnaireSnackBar) = launch(IO) {
        localRepository.insertCompleteQuestionnaire(event.completeQuestionnaire)
        localRepository.delete(LocallyDeletedQuestionnaire.asOwner(event.completeQuestionnaire.questionnaire.id))
    }



    // DELETE FILLED QUESTIONNAIRE
    fun deleteCachedQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let {
            fragmentHomeEventChannel.send(ShowUndoDeleteCachedQuestionnaireSnackBar(it))
        }
        localRepository.insert(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
        localRepository.deleteQuestionnaireWith(questionnaireId)
    }

    fun onDeleteCachedQuestionnaireConfirmed(event: ShowUndoDeleteCachedQuestionnaireSnackBar) = launch(IO) {
        val questionnaireId = event.completeQuestionnaire.questionnaire.id
        localRepository.delete(LocallyDownloadedQuestionnaire(questionnaireId))

        runCatching {
            backendRepository.deleteQuestionnaire(listOf(questionnaireId))
        }.onSuccess {
            if (it.isSuccessful) {
                localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(questionnaireId))
            }
        }
    }

    fun onUndoDeleteCachedQuestionnaireClicked(event: ShowUndoDeleteCachedQuestionnaireSnackBar) = launch(IO) {
        localRepository.insertCompleteQuestionnaire(event.completeQuestionnaire)
        localRepository.delete(LocallyDeletedQuestionnaire.notAsOwner(event.completeQuestionnaire.questionnaire.id))
    }







    // DELETE ANSWERS OF QUESTIONNAIRE
    //TODO -> ONLINE LÖSCHEN MACHEN!
    // WENN MAN OFFLINE IST UND ES ONLINE NICHT GELÖSCHT WERDEN KONNTE, WIRD ES in LocallyDeletedFilledQuestionnaire gespeichert
    // UND DANN GELÖSCHT WENN MAN SYNCT
    // WENN MAN WIEDER INET HAT UND VERUSUCHT DIE ANTWORTEN HOCHZULADEN, WIRD DAS LÖSCHEN IGNOERIERT und die LocallyDeletedFilledQuestionnaire GELÖSCHT
    // ES WIRD SOMIT IM BACKEND INSERTED ABER NICHT GELÖSCHT!
    fun deleteGivenAnswersOfQuestionnaire(questionnaireId: String) = launch(IO) {
        localRepository.findCompleteQuestionnaireWith(questionnaireId)?.let { completeQuestionnaire ->

            //TODO -> Inserted, dass man den hier reingeldaden hat -> Man muss noch schauen ob die Id in LocallyAnsweredQuestionnaireIds is und wenn ja rauslöschen!
            localRepository.insert(LocallyDeletedFilledQuestionnaire(completeQuestionnaire.questionnaire.id))

            fragmentHomeEventChannel.send(ShowUndoDeleteAnswersOfQuestionnaireSnackBar(completeQuestionnaire))
            completeQuestionnaire.allAnswers.map { it.copy(isAnswerSelected = false) }.also { localRepository.update(it) }
        }
    }
    //TODO -> Das ist der einzige kritische Punkt, da hier nur die antworten gelöscht werden, deswegen wird später der Fragebogen nicht automatisch rausgefiltert

    fun onDeleteGivenAnswersOfQuestionnaireConfirmed(event: ShowUndoDeleteAnswersOfQuestionnaireSnackBar) = launch(IO) {
        runCatching {
            backendRepository.insertFilledQuestionnaire(DataMapper.mapSqlEntitiesToEmptyFilledMongoEntity(event.completeQuestionnaire))
        }.onSuccess { response ->
            if(response.isSuccessful){

            }
        }
    }


    fun onUndoDeleteGivenAnswersClicked(event: ShowUndoDeleteAnswersOfQuestionnaireSnackBar) = launch(IO) {
        localRepository.update(event.completeQuestionnaire.allAnswers)
    }





    //TODO -> WANN SOLLEN ANTWORTEN HOCHGELADEN WERDEN?
    //TODO -> 1) WENN MAN IN DEM QUIZ SCREEN IST WIRD, sobald man eine Anwtort auswählt, die QuestionnaireId gespeichert.
    //TODO -> Wenn man dann wieder in den HomeScreen kommt, werden die Antworten für diese Id Hochgeladen
    //TODO -> Dafür wird eine eigene Entität genötigt, namens "LocallyGivenAnswersForQuestionnaire" oder so
    //TODO -> Wenn man den Fragebogen löscht, wird auch die LocallyGivenAnswersForQuestionnaire ID Gelöscht und die LocallayDeletedFilledQuestionnaire




    sealed class FragmentHomeEvent {
        class ShowSnackBarMessageBar(val messageRes: Int) : FragmentHomeEvent()
        class ShowUndoDeleteCreatedQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaireJunction) : FragmentHomeEvent()
        class ShowUndoDeleteCachedQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaireJunction) : FragmentHomeEvent()
        class ShowUndoDeleteAnswersOfQuestionnaireSnackBar(val completeQuestionnaire: CompleteQuestionnaireJunction) : FragmentHomeEvent()
        class ChangeProgressVisibility(val visible: Boolean) : FragmentHomeEvent()
    }

    sealed class FragmentHomeCreatedEvent {
        class ChangeCreatedSwipeRefreshLayoutVisibility(val visible: Boolean)  : FragmentHomeCreatedEvent()
    }

    sealed class FragmentHomeCachedEvent {
        class ChangeCachedSwipeRefreshLayoutVisibility(val visible: Boolean) : FragmentHomeCachedEvent()
    }
}