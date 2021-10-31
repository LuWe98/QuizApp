package com.example.quizapp.view.recyclerview.adapters

import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.RviAnswerEditBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.room.entities.questionnaire.Answer
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import com.example.quizapp.viewmodel.VmAddEditQuestion

class RvaAnswerEditQuestion(
    private val vmEditQuestion: VmAddEditQuestion
) : BindingListAdapter<Answer, RviAnswerEditBinding>(Answer.DIFF_CALLBACK) {

    var onItemClick: ((List<Answer>) -> (Unit))? = null

    var onDeleteButtonClick: ((Answer) -> (Unit))? = null

    var onAnswerTextChanged: ((Int, String) -> (Unit))? = null

    override fun initListeners(binding: RviAnswerEditBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.setOnClickListener {
                getItem(vh.bindingAdapterPosition).let { answer ->
                    if (vmEditQuestion.isMultipleChoice) {
                        handleMultipleChoiceClick(vh.bindingAdapterPosition, answer)
                    } else if (!answer.isAnswerCorrect) {
                        handleSingleChoiceClick(vh.bindingAdapterPosition)
                    }
                }
            }

            btnDelete.onClick {
                getItem(vh.bindingAdapterPosition).let {
                    onDeleteButtonClick?.invoke(it)
                }
            }

            etAnswer.onTextChanged { text ->
                onAnswerTextChanged?.invoke(vh.bindingAdapterPosition, text)
            }
        }
    }

    private fun handleMultipleChoiceClick(position: Int, answer: Answer) {
        onItemClick?.invoke(mutableListOf<Answer>().apply {
            addAll(currentList)
            set(position, answer.copy(isAnswerCorrect = !answer.isAnswerCorrect))
        })
    }

    private fun handleSingleChoiceClick(position: Int) {
        onItemClick?.invoke(mutableListOf<Answer>().apply {
            currentList.forEachIndexed { index, answer ->
                add(answer.copy(isAnswerCorrect = position == index))
            }
        })
    }

    override fun bindViews(binding: RviAnswerEditBinding, item: Answer, position: Int) {
        binding.apply {
            val lastSelectionPos = etAnswer.selectionStart
            etAnswer.setText(item.answerText)
            etAnswer.setSelection(lastSelectionPos)
            etAnswer.setTextColorWithRes(if (item.isAnswerCorrect) R.color.green else R.color.black)
            ivRing.setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)

            ivSelectedIcon.apply {
                isVisible = item.isAnswerCorrect
                setImageDrawable(if (vmEditQuestion.isMultipleChoice) R.drawable.ic_check else R.drawable.ic_circle)
                setDrawableTintWithRes(if (item.isAnswerCorrect) R.color.green else R.color.unselectedColor)
            }
        }
    }
}