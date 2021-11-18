package com.example.quizapp.extensions

inline fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    return indexOfFirst(predicate).let { index ->
        if(index == -1) null else index
    }
}