package com.example.quizapp.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.example.quizapp.R
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.utils.CsvDocumentFilePicker.CsvDocumentFilePickerErrorType.*
import dagger.hilt.android.scopes.FragmentScoped
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@FragmentScoped
class CsvDocumentFilePicker @Inject constructor(
    private val fragment: Fragment
) {

    companion object {
        private const val INTENT_TYPE = "*/*"

        private const val CSV_SUFFIX_DELIMITER = "."
        private const val CSV_SUFFIX_MISSING_DELIMITER_DEFAULT_VALUE = ""
        private const val CSV_SUFFIX = "csv"


        private const val ROWS_TO_SKIP = 10
        private const val QUESTION_TEXT_INDEX = 0
        private const val QUESTION_IS_MULTIPLE_CHOICE_INDEX = 1
        private const val CORRECT_ANSWERS_INDEX = 2
        private const val ANSWERS_COLUMN_OFFSET = 3
        private const val CORRECT_ANSWERS_SEPARATOR = ","
        private const val COLUMN_SEPARATOR = ";"

        private const val IS_MULTIPLE_CHOICE = "YES"
        private const val IS_MULTIPLE_CHOICE_SHORT = "Y"
        private const val IS_MULTIPLE_CHOICE_GERMAN = "JA"
        private const val IS_MULTIPLE_CHOICE_GERMAN_SHORT = "J"
        private const val IS_MULTIPLE_CHOICE_BOOLEAN = "TRUE"
        private const val IS_MULTIPLE_CHOICE_BOOLEAN_SHORT = "T"
        private const val IS_MULTIPLE_CHOICE_BINARY = "1"

        private val IS_MULTIPLE_CHOICE_CHARACTERS = listOf(
            IS_MULTIPLE_CHOICE,
            IS_MULTIPLE_CHOICE_SHORT,
            IS_MULTIPLE_CHOICE_GERMAN,
            IS_MULTIPLE_CHOICE_GERMAN_SHORT,
            IS_MULTIPLE_CHOICE_BOOLEAN,
            IS_MULTIPLE_CHOICE_BOOLEAN_SHORT,
            IS_MULTIPLE_CHOICE_BINARY
        )
    }

    class CsvDocumentFilePickerException(val errorType: CsvDocumentFilePickerErrorType) : Exception()

    sealed class CsvDocumentFilePickerErrorType {
        object CouldNotOpenFile : CsvDocumentFilePickerErrorType()
        object WrongFileTypeSelected : CsvDocumentFilePickerErrorType()
        object UnknownError : CsvDocumentFilePickerErrorType()
        object EmptyDocumentError : CsvDocumentFilePickerErrorType()
        class MalformedRow(val row: Int) : CsvDocumentFilePickerErrorType()
        class NoCorrectAnswersSelectedForQuestion(val questionWithAnswers: QuestionWithAnswers, val row: Int) : CsvDocumentFilePickerErrorType()
        class QuestionIsSingleChoiceAndHasMultipleCorrectAnswers(val questionWithAnswers: QuestionWithAnswers, val row: Int) : CsvDocumentFilePickerErrorType()

        fun getErrorMessage(context: Context) = context.run {
            when (this@CsvDocumentFilePickerErrorType) {
                CouldNotOpenFile -> getString(R.string.errorCouldNotOpenFileException)
                EmptyDocumentError -> getString(R.string.errorDocumentIsEmpty)
                UnknownError -> getString(R.string.errorCouldNotCreateQuestionnaireFromCsv)
                WrongFileTypeSelected -> getString(R.string.errorWrongFileTypeSelected)
                is MalformedRow -> getString(R.string.errorMalformedRow, row.toString())
                is NoCorrectAnswersSelectedForQuestion -> getString(R.string.errorNoCorrectAnswerSelectedForQuestion, row.toString())
                is QuestionIsSingleChoiceAndHasMultipleCorrectAnswers -> getString(R.string.errorQuestionIsSingleChoiceAndHasMultipleCorrectAnswers, row.toString())
            }
        }
    }

    sealed class CsvDocumentFilePickerResult {
        class Success(
            val questionsWithAnswers: List<QuestionWithAnswers>
        ) : CsvDocumentFilePickerResult()

        class Error(
            val type: CsvDocumentFilePickerErrorType
        ) : CsvDocumentFilePickerResult()
    }

    private var _onValidCsvFileSelected: (() -> (Unit))? = null

    private val onValidCsvFileSelected get() = _onValidCsvFileSelected!!

    private var _onCsvLinesReadResult: ((CsvDocumentFilePickerResult) -> (Unit))? = null

    private val onCsvLinesReadResult get() = _onCsvLinesReadResult!!

    fun startFilePicker(selectedAction: () -> (Unit), resultAction: ((CsvDocumentFilePickerResult) -> (Unit))) {
        _onValidCsvFileSelected = selectedAction
        _onCsvLinesReadResult = resultAction
        if (fragment.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startDocumentPicker()
        } else {
            askForPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val askForPermission = fragment.askForPermission { granted ->
        if (granted) startDocumentPicker()
    }

    private fun startDocumentPicker() {
        startDocumentPicker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = INTENT_TYPE
        })
    }

    private val startDocumentPicker = fragment.startDocumentFilePickerResult { documentFile ->
        if (documentFile == null) {
            onCsvLinesReadResult.invoke(CsvDocumentFilePickerResult.Error(CouldNotOpenFile))
            return@startDocumentFilePickerResult
        }
        if (!isFileSuffixCorrect(documentFile)) {
            onCsvLinesReadResult.invoke(CsvDocumentFilePickerResult.Error(WrongFileTypeSelected))
            return@startDocumentFilePickerResult
        }

        onValidCsvFileSelected.invoke()

        runCatching {
            getEntitiesFromCsvFile(documentFile).also {
                it.forEach {
                    log("LOADED: $it")
                }
            }
        }.onSuccess {
            CsvDocumentFilePickerResult
                .Success(it)
                .let(onCsvLinesReadResult)
        }.onFailure {
            CsvDocumentFilePickerResult
                .Error(if (it is CsvDocumentFilePickerException) it.errorType else UnknownError)
                .let(onCsvLinesReadResult)
        }
    }

    private fun isFileSuffixCorrect(documentFile: DocumentFile) = documentFile.name?.substringAfterLast(
        CSV_SUFFIX_DELIMITER,
        CSV_SUFFIX_MISSING_DELIMITER_DEFAULT_VALUE
    ).equals(CSV_SUFFIX, true)

    private fun readLines(file: DocumentFile) = BufferedReader(
        InputStreamReader(fragment.requireContext().contentResolver.openInputStream(file.uri), StandardCharsets.UTF_8)
    ).readLines()


    private fun getEntitiesFromCsvFile(documentFile: DocumentFile): List<QuestionWithAnswers> {
        val filteredRows = readLines(documentFile).ifEmpty {
            throw CsvDocumentFilePickerException(EmptyDocumentError)
        }.mapIndexedNotNull { index, row ->
            if (index <= ROWS_TO_SKIP) null else row.split(COLUMN_SEPARATOR).filter(String::isNotBlank)
        }.filter(List<String>::isNotEmpty)

        return filteredRows
            .map(::mapColumnsToQuestionWithAnswers)
            .mapIndexed(::validateQuestionWithAnswers)
    }

    private fun mapColumnsToQuestionWithAnswers(columns: List<String>) : QuestionWithAnswers {
        val question = Question(
            questionText = columns[QUESTION_TEXT_INDEX],
            isMultipleChoice = IS_MULTIPLE_CHOICE_CHARACTERS.contains(columns[QUESTION_IS_MULTIPLE_CHOICE_INDEX].uppercase())
        )

        val correctAnswersIndex = columns[CORRECT_ANSWERS_INDEX]
            .split(CORRECT_ANSWERS_SEPARATOR)
            .map(String::trim)
            .map(String::toInt)

        val answers = columns.subList(ANSWERS_COLUMN_OFFSET, columns.size).mapIndexed { answerIndex, answerText ->
            Answer(
                answerText = answerText,
                answerPosition = answerIndex,
                isAnswerCorrect = correctAnswersIndex.contains(answerIndex)
            )
        }

        return QuestionWithAnswers(question, answers)
    }

    private fun validateQuestionWithAnswers(rowIndex: Int, questionWithAnswers: QuestionWithAnswers): QuestionWithAnswers {
        questionWithAnswers.answers.count(Answer::isAnswerCorrect).let { correctAnswersCount ->
            if (correctAnswersCount == 0) {
                throw CsvDocumentFilePickerException(NoCorrectAnswersSelectedForQuestion(questionWithAnswers, rowIndex))
            }
            if (!questionWithAnswers.question.isMultipleChoice && correctAnswersCount != 1) {
                throw CsvDocumentFilePickerException(QuestionIsSingleChoiceAndHasMultipleCorrectAnswers(questionWithAnswers, rowIndex))
            }
            return questionWithAnswers
        }
    }
}