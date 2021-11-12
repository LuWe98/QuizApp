package com.example.quizapp.view.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.model.databases.room.entities.questionnaire.Question
import com.example.quizapp.view.fragments.quizscreen.FragmentQuizQuestion

class VpaQuiz(fragment: Fragment, questions : List<Question>) : FragmentStateAdapter(fragment) {

    private val fragments = Array(questions.size) {
        FragmentQuizQuestion.newInstance(questions[it])
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}