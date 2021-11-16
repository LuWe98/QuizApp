package com.example.quizapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quizapp.extensions.getMutableStateFlow
import com.example.quizapp.extensions.launch
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.model.datastore.PreferencesRepository
import com.example.quizapp.model.ktor.BackendRepository
import com.example.quizapp.viewmodel.VmAddEditNew.FragmentAddEditEvent.NavigateToCourseOfStudiesSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

    private val fragmentAddEditEventChannel = Channel<FragmentAddEditEvent>()

    val fragmentAddEditEventChannelFlow = fragmentAddEditEventChannel.receiveAsFlow()

    //TODO -> Faculty kommt im CompleteQuestionnaire wenn man den reinladed, nachem man speichern gedrückt hat!
    //TODO -> Um sie dann online zu speichern und in MongoQuestionnaire umzuwandeln
    private var coursesOfStudiesIdsMutableStateFlow = state.getMutableStateFlow(COURSES_OF_STUDIES_IDS_KEY, runBlocking(IO) {
        preferencesRepository.getPreferredCourseOfStudiesId().toList()
    })

    val coursesOfStudiesStateFlow = coursesOfStudiesIdsMutableStateFlow
        .flatMapLatest(localRepository::getCoursesOfStudiesFlowWithIds)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun onCourseOfStudiesButtonClicked() {
        launch(IO) {
            fragmentAddEditEventChannel.send(NavigateToCourseOfStudiesSelector(coursesOfStudiesIdsMutableStateFlow.value))
        }
    }

    fun setCoursesOfStudiesIds(courseOfStudiesIds: List<String>) {
        launch(IO) {
            val updatedList = mutableListOf<String>().apply {
                addAll(courseOfStudiesIds)
            }

            state.set(COURSES_OF_STUDIES_IDS_KEY, updatedList)
            coursesOfStudiesIdsMutableStateFlow.value = updatedList
        }
    }


    sealed class FragmentAddEditEvent {
        class NavigateToCourseOfStudiesSelector(val courseOfStudiesIds: List<String>) : FragmentAddEditEvent()
    }


    companion object {
        const val COURSES_OF_STUDIES_IDS_KEY = "coursesOfStudiesIdsKey"
    }
}