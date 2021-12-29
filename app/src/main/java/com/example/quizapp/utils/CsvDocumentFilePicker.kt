package com.example.quizapp.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.example.quizapp.R
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.properties.AuthorInfo
import com.example.quizapp.model.databases.room.entities.Answer
import com.example.quizapp.model.databases.room.entities.Question
import com.example.quizapp.model.databases.room.entities.Questionnaire
import com.example.quizapp.model.databases.room.junctions.QuestionWithAnswers
import com.example.quizapp.utils.CsvDocumentFilePicker.CsvDocumentFilePickerErrorType.*
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject

@FragmentScoped
class CsvDocumentFilePicker @Inject constructor(
    private val fragment: Fragment
) {

    companion object {

        private const val COLUMN_SEPARATOR = ";"

        private const val INTENT_TYPE = "*/*"

        private const val CSV_SUFFIX_DELIMITER = "."
        private const val CSV_SUFFIX_MISSING_DELIMITER_DEFAULT_VALUE = ""
        private const val CSV_SUFFIX = "csv"

        private const val QUESTIONNAIRE_FULL = "QUESTIONNAIRE"
        private const val QUESTION_FULL = "QUESTION"
        private const val QUESTION_SHORT = "Q"
        private const val ANSWER_FULL = "ANSWER"
        private const val ANSWER_SHORT = "A"

        private const val TRUE_FULL = "TRUE"
        private const val TRUE_SHORT = "T"
        private const val TRUE_BINARY = "1"
        private val TRUE_CHARACTERS = listOf(TRUE_FULL, TRUE_SHORT, TRUE_BINARY)

        private const val FALSE_FULL = "FALSE"
        private const val FALSE_SHORT = "F"
        private const val FALSE_BINARY = "0"
        private val FALSE_CHARACTERS = listOf(FALSE_FULL, FALSE_SHORT, FALSE_BINARY)
    }

    sealed class CsvDocumentFilePickerResult {
        class Success(
            val questionnaire: Questionnaire,
            val qwa: List<QuestionWithAnswers>
        ) : CsvDocumentFilePickerResult()
        class Error(
            val type: CsvDocumentFilePickerErrorType
        ) : CsvDocumentFilePickerResult()
    }

    sealed class CsvDocumentFilePickerErrorType(@StringRes val messageRes: Int) {
        class WrongRowAnnotation(
            val annotation: String,
            val lineIndex: Int
        ) : CsvDocumentFilePickerErrorType(R.string.errorUnknownRowAnnotation)
        class RowMappingError(
            val row: String,
            val lineIndex: Int
        ) : CsvDocumentFilePickerErrorType(R.string.errorRowCouldNotBeMapped)
        object CouldNotOpenFile : CsvDocumentFilePickerErrorType(R.string.errorCouldNotOpenFileException)
        object WrongFileTypeSelected : CsvDocumentFilePickerErrorType(R.string.errorWrongFileTypeSelected)
        object UnknownError : CsvDocumentFilePickerErrorType(R.string.errorCouldNotCreateQuestionnaireFromCsv)
        object MissingQuestionnaireHeader : CsvDocumentFilePickerErrorType(R.string.errorMissingQuestionnaireHeader)
        class MissingQuestionHeader(val lineIndex: Int) : CsvDocumentFilePickerErrorType(R.string.errorMissingQuestionHeaderForRow)
        class EmptyColumnInRow(val lineIndex: Int) : CsvDocumentFilePickerErrorType(R.string.errorEmptyColumnInRow)
        class NoCorrectAnswersSelectedForQuestion(
            val questionWithAnswers: QuestionWithAnswers,
            val lineIndex: Int
        ) : CsvDocumentFilePickerErrorType(R.string.errorNoCorrectAnswerSelectedForQuestion)
        class QuestionIsSingleChoiceAndHasMultipleCorrectAnswers(
            val questionWithAnswers: QuestionWithAnswers,
            val lineIndex: Int
        ): CsvDocumentFilePickerErrorType(R.string.errorQuestionIsSingleChoiceAndHasMultipleCorrectAnswers)
        object EmptyDocumentError: CsvDocumentFilePickerErrorType(R.string.errorDocumentIsEmpty)

        fun getErrorMessage(context: Context) = context.run {
            when (this@CsvDocumentFilePickerErrorType) {
                is WrongRowAnnotation -> getString(messageRes, annotation, lineIndex.toString())
                is RowMappingError -> getString(messageRes, lineIndex.toString())
                is MissingQuestionHeader -> getString(messageRes, lineIndex.toString())
                is EmptyColumnInRow -> getString(messageRes, lineIndex.toString())
                is NoCorrectAnswersSelectedForQuestion -> getString(messageRes, lineIndex.toString())
                is QuestionIsSingleChoiceAndHasMultipleCorrectAnswers -> getString(messageRes, lineIndex.toString())
                else -> getString(messageRes)
            }
        }
    }

    class CsvDocumentFilePickerException(val errorType: CsvDocumentFilePickerErrorType) : Exception()

    private var _selectedAction: (() -> (Unit))? = null

    private val selectedAction get() = _selectedAction!!

    private var _resultAction: ((CsvDocumentFilePickerResult) -> (Unit))? = null

    private val resultAction get() = _resultAction!!

    fun startFilePicker(selectedAction: () -> (Unit), resultAction: ((CsvDocumentFilePickerResult) -> (Unit))) {
        _selectedAction = selectedAction
        _resultAction = resultAction

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
        selectedAction.invoke()

        if (documentFile == null) {
            resultAction.invoke(CsvDocumentFilePickerResult.Error(CouldNotOpenFile))
            return@startDocumentFilePickerResult
        }
        if (!isFileSuffixCorrect(documentFile)) {
            resultAction.invoke(CsvDocumentFilePickerResult.Error(WrongFileTypeSelected))
            return@startDocumentFilePickerResult
        }

        runCatching {
            getEntitiesFromCsvFile(documentFile)
        }.onSuccess {
            CsvDocumentFilePickerResult.Success(it.first, it.second).let(resultAction)
        }.onFailure {
            CsvDocumentFilePickerResult.Error(if(it is CsvDocumentFilePickerException) it.errorType else UnknownError).let(resultAction)
        }
    }

    private fun isFileSuffixCorrect(documentFile: DocumentFile) = documentFile.name?.substringAfterLast(
        CSV_SUFFIX_DELIMITER,
        CSV_SUFFIX_MISSING_DELIMITER_DEFAULT_VALUE
    ).equals(CSV_SUFFIX, true)


    private fun getEntitiesFromCsvFile(documentFile: DocumentFile): Pair<Questionnaire, List<QuestionWithAnswers>> {
        val lines = documentFile.readLines(fragment.requireContext())

        if(lines.isEmpty()) {
            throw CsvDocumentFilePickerException(EmptyDocumentError)
        }

        var questionnaire: Questionnaire? = null
        val questionWithAnswers: MutableList<QuestionWithAnswers> = mutableListOf()
        var lastQuestion: Question? = null
        val tempAnswerList: MutableList<Answer> = mutableListOf()

        lines.forEachIndexed { index, row ->
            val columns = row.split(COLUMN_SEPARATOR).map(String::trim)

            if (columns.any(String::isEmpty)) {
                throw CsvDocumentFilePickerException(EmptyColumnInRow(index))
            }

            try {
                when (columns[0].uppercase()) {
                    QUESTIONNAIRE_FULL -> {
                        questionnaire = Questionnaire(
                            title = columns[1],
                            authorInfo = AuthorInfo("", ""),
                            subject = columns[2]
                        )
                    }
                    QUESTION_FULL, QUESTION_SHORT -> {
                        Question(
                            questionnaireId = questionnaire!!.id,
                            questionText = columns[1],
                            isMultipleChoice = columns[2].uppercase() in TRUE_CHARACTERS,
                            questionPosition = questionWithAnswers.size
                        ).let {
                            if (tempAnswerList.isNotEmpty()) {
                                validateQuestionWithAnswers(
                                    index - tempAnswerList.size - 1,
                                    QuestionWithAnswers(lastQuestion!!, tempAnswerList.toList())
                                ).let { qwa ->
                                    questionWithAnswers.add(qwa)
                                    tempAnswerList.clear()
                                }
                            }
                            lastQuestion = it
                        }
                    }
                    ANSWER_FULL, ANSWER_SHORT -> {
                        tempAnswerList.add(
                            Answer(
                                questionId = lastQuestion!!.id,
                                answerText = columns[1],
                                isAnswerCorrect = columns[2].uppercase() in TRUE_CHARACTERS,
                                answerPosition = tempAnswerList.size
                            )
                        )
                    }
                    else -> throw CsvDocumentFilePickerException(WrongRowAnnotation(columns[0], index))
                }
            } catch (exception: Exception) {
                when {
                    exception is CsvDocumentFilePickerException -> throw exception
                    questionnaire == null -> throw CsvDocumentFilePickerException(MissingQuestionnaireHeader)
                    lastQuestion == null -> throw CsvDocumentFilePickerException(MissingQuestionHeader(index))
                    else -> throw CsvDocumentFilePickerException(RowMappingError(row, index))
                }
            }
        }

        if(lastQuestion != null && tempAnswerList.isNotEmpty()) {
            validateQuestionWithAnswers(
                lines.size - tempAnswerList.size - 1,
                QuestionWithAnswers(lastQuestion!!, tempAnswerList.toList())
            ).let { qwa ->
                questionWithAnswers.add(qwa)
            }
        }

        return Pair(questionnaire!!, questionWithAnswers)
    }

    private fun validateQuestionWithAnswers(lineIndex: Int, questionWithAnswers: QuestionWithAnswers): QuestionWithAnswers {
        questionWithAnswers.answers.count(Answer::isAnswerCorrect).let { correctAnswersCount ->
            if (correctAnswersCount == 0) {
                throw CsvDocumentFilePickerException(NoCorrectAnswersSelectedForQuestion(questionWithAnswers, lineIndex))
            }
            if (!questionWithAnswers.question.isMultipleChoice && correctAnswersCount != 1) {
                throw CsvDocumentFilePickerException(QuestionIsSingleChoiceAndHasMultipleCorrectAnswers(questionWithAnswers, lineIndex))
            }
            return questionWithAnswers
        }
    }
}