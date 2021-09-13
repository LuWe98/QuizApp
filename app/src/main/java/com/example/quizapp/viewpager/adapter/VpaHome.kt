package com.example.quizapp.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.ui.fragments.homescreen.FragmentHomeBrowseQuestionnaires
import com.example.quizapp.ui.fragments.homescreen.FragmentHomeCachedQuestionnaires
import com.example.quizapp.ui.fragments.homescreen.FragmentHomeCreatedQuestionnaires

class VpaHome(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = arrayOfNulls<Fragment>(3)

    init {
        fragments[0] = FragmentHomeBrowseQuestionnaires()
        fragments[1] = FragmentHomeCachedQuestionnaires()
        fragments[2] = FragmentHomeCreatedQuestionnaires()
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]!!
}