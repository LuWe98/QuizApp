package com.example.quizapp.model.ktor.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.quizapp.model.databases.dto.MongoBrowsableQuestionnaire
import com.example.quizapp.model.datastore.datawrappers.BrowsableQuestionnaireOrderBy
import com.example.quizapp.model.ktor.BackendResponse

class BrowseQuestionnairePagingSource(
    private val orderBy: BrowsableQuestionnaireOrderBy,
    private val getQuestionnaireAction: suspend (BrowsableQuestionnairePageKeys) -> (BackendResponse.GetPagedQuestionnairesWithPageKeysResponse),
) : PagingSource<BrowsableQuestionnairePageKeys, MongoBrowsableQuestionnaire>() {

    override fun getRefreshKey(state: PagingState<BrowsableQuestionnairePageKeys, MongoBrowsableQuestionnaire>): BrowsableQuestionnairePageKeys? {
        return null
    }

    override suspend fun load(params: LoadParams<BrowsableQuestionnairePageKeys>): LoadResult<BrowsableQuestionnairePageKeys, MongoBrowsableQuestionnaire> = try {
        val pageKeys = params.key ?: EMPTY_KEY
        val response = getQuestionnaireAction(pageKeys)

        LoadResult.Page(
            data = response.questionnaires,
            prevKey = if (pageKeys == EMPTY_KEY) null else response.previousKeys,
            nextKey = if (response.questionnaires.isEmpty()) null else response.questionnaires.lastOrNull()?.let {
                when (orderBy) {
                    BrowsableQuestionnaireOrderBy.TITLE -> BrowsableQuestionnairePageKeys(
                        id = it.id,
                        title = it.title
                    )
                    BrowsableQuestionnaireOrderBy.LAST_UPDATED -> BrowsableQuestionnairePageKeys(
                        id = it.id,
                        timeStamp = it.lastModifiedTimestamp
                    )
                }
            }
        )
    } catch (e: Exception) {
        LoadResult.Error(e)
    }

    companion object {
        private val EMPTY_KEY = BrowsableQuestionnairePageKeys()
    }
}