package com.example.quizapp.view.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.view.fragments.homescreen.FragmentHomeBrowse
import com.example.quizapp.view.fragments.homescreen.FragmentHomeCached
import com.example.quizapp.view.fragments.homescreen.FragmentHomeCreated

class VpaHome(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = Array<Fragment>(3) {
        when(it){
            0 -> FragmentHomeBrowse()
            1 -> FragmentHomeCached()
            2 -> FragmentHomeCreated()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}