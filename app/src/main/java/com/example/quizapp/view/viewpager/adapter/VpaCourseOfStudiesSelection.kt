package com.example.quizapp.view.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.FragmentCourseOfStudiesSelectionPage

class VpaCourseOfStudiesSelection(fragment: Fragment, faculties: List<Faculty>) : FragmentStateAdapter(fragment) {

    private val fragments = Array<Fragment>(faculties.size) {
        FragmentCourseOfStudiesSelectionPage.newInstance(faculties[it])
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

}