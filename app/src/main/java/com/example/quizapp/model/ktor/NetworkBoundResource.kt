package com.example.quizapp.model.ktor

import kotlinx.coroutines.flow.*

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline onFetchFailed: (Throwable) -> Unit = { },
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    emit(ResourceStatus.Loading())

    val data = query().first()
    val flow = if(shouldFetch(data)){
        emit(ResourceStatus.Loading(data))

        try {
            val fetchedResult = fetch()
            saveFetchResult(fetchedResult)
            query().map { ResourceStatus.Success(it) }
        } catch (t: Throwable){
            onFetchFailed(t)
            query().map { ResourceStatus.Error(t, it) }
        }
    } else {
        query().map { ResourceStatus.Success(it) }
    }

    emitAll(flow)
}