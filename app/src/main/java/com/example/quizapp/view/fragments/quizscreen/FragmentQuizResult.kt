package com.example.quizapp.view.fragments.quizscreen

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.navigation.navGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentQuizResultBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmQuiz
import com.example.quizapp.viewmodel.VmQuizResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentQuizResult: BindingFragment<FragmentQuizResultBinding>() {

    private val vmQuiz: VmQuiz by navGraphViewModels(R.id.quiz_nav_graph)

    private val vmQuizResult: VmQuizResult by hiltNavDestinationViewModels(R.id.fragmentQuizResult)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initViews()
        initClickListeners()
        registerObservers()
    }

    private fun initViews(){
        binding.apply {

        }
    }

    private fun initClickListeners(){
        binding.apply {
            btnExit.onClick(vmQuizResult::onCloseButtonClicked)
            cardRetry.onClick { vmQuizResult.onTryAgainClicked(vmQuiz.completeQuestionnaire) }
            cardShowSolutions.onClick(vmQuizResult::onShowSolutionsClicked)
        }
    }

    private fun registerObservers(){
        vmQuiz.questionStatisticsFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                progressCorrect.setProgressWithAnimation(it.correctQuestionsPercentage)
                progressIncorrect.setProgressWithAnimation(it.incorrectQuestionsPercentageDiff)

                val color: Int
                val resultIcon: Drawable?

                if(it.correctQuestionsPercentage > 80){
                    color = getColor(R.color.green)
                    resultIcon = getDrawable(R.drawable.ic_check)
                } else {
                    color = getColor(R.color.red)
                    resultIcon = getDrawable(R.drawable.ic_cross)
                }

                ivResultIcon.setImageDrawable(resultIcon)
                ivResultIcon.setDrawableTint(color)

                SpannableString(getString(R.string.outOf, it.correctQuestionsAmount.toString(), it.questionsAmount.toString())).let { spannableText ->
                    spannableText.setSpan(ForegroundColorSpan(color), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    tvScoreText.text = spannableText
                }
            }
        }

        vmQuizResult.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){

            }
        }
    }
}