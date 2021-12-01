package com.example.quizapp.extensions

import com.example.quizapp.model.databases.room.LocalDatabase

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    return indexOfFirst(predicate).let { index ->
        if(index == -1) null else index
    }
}


inline fun <reified T> Collection<T>.asRawQueryPlaceHolderString() : String = joinToString(LocalDatabase.PLACEHOLDER_SEPARATOR) { LocalDatabase.PLACEHOLDER }