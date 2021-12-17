package com.example.quizapp.view.viewpager.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.quizapp.view.fragments.authscreen.FragmentAuthLogin
import com.example.quizapp.view.fragments.authscreen.FragmentAuthRegister

class VpaAuth(fragment : Fragment) : FragmentStateAdapter(fragment) {

    private val fragments = Array<Fragment>(2) {
        when(it){
            0 -> FragmentAuthLogin()
            1 -> FragmentAuthRegister()
            else -> throw IllegalStateException()
        }
    }

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

}