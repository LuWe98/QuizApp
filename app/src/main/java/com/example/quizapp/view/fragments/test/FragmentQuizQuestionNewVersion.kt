package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentQuizQuestionNewBinding
import com.example.quizapp.databinding.RviAnswerQuizBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.log
import com.example.quizapp.utils.DiffCallbackUtil
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.impl.BindingListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class FragmentQuizQuestionNewVersion: BindingFragment<FragmentQuizQuestionNewBinding>() {

    companion object {
        fun newInstance() = FragmentQuizQuestionNewVersion().also { fragment ->
            Bundle().let { bundle ->
                bundle.putBoolean("test", Random.nextBoolean())
                fragment.arguments = bundle
            }
        }
    }

    val isAnswered by lazy { arguments!!["test"] as Boolean }

    private lateinit var rvAdapter: RvaQuizTest


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvQuestion.text = "Is Answered? : $isAnswered"

        rvAdapter = RvaQuizTest().apply {

        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
            adapter = rvAdapter
        }


        val items = Array(Random.nextInt(3) + 2) {
            "Answer $it"
        }

        rvAdapter.submitList(items.toList())
    }



    class RvaQuizTest : BindingListAdapter<String, RviAnswerQuizBinding>(DiffCallbackUtil.createDiffUtil { t, t2 ->  t == t2}){
        override fun initListeners(binding: RviAnswerQuizBinding, vh: BindingListAdapterViewHolder) {

        }

        override fun bindViews(binding: RviAnswerQuizBinding, item: String, position: Int) {
            binding.tvAnswerText.text = item
        }
    }
}