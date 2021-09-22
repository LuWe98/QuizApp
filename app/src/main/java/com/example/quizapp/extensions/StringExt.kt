package com.example.quizapp.extensions

val String.containsWhiteSpaces : Boolean get() = matches(".*\\s.*".toRegex())

val String.isValidEmail : Boolean get() = matches(".+@.+[.].+".toRegex())