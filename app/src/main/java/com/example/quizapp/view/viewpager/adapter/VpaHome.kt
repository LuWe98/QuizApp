package com.example.quizapp.view.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.view.fragments.homescreen.FragmentHomeBrowseQuestionnaires
import com.example.quizapp.view.fragments.homescreen.FragmentHomeCachedQuestionnaires
import com.example.quizapp.view.fragments.homescreen.FragmentHomeCreatedQuestionnaires

class VpaHome(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = Array<Fragment>(3) {
        when(it){
            0 -> FragmentHomeBrowseQuestionnaires()
            1 -> FragmentHomeCachedQuestionnaires()
            2 -> FragmentHomeCreatedQuestionnaires()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}