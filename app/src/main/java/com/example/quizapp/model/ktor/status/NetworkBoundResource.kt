package com.example.quizapp.model.ktor.status

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    emit(Resource.Loading())

    val data = query().first()
    val flow = if(shouldFetch(data)){
        emit(Resource.Loading(data))

        try {
            val fetchedResult = fetch()
            saveFetchResult(fetchedResult)
            query().map { Resource.Success(it) }
        } catch (t: Throwable){
            onFetchFailed(t)
            query().map { Resource.Error(t, it) }
        }
    } else {
        query().map { Resource.Success(it) }
    }

    emitAll(flow)
}