package com.example.quizapp.ui.fragments.homescreen

import android.os.Bundle
import android.view.View
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.extensions.attachToViewPager
import com.example.quizapp.extensions.getStringArray
import com.example.quizapp.ui.fragments.bindingfragmentsuperclasses.BindingFragment
import com.example.quizapp.viewpager.adapter.VpaHome
import com.example.quizapp.viewpager.pagetransformer.FadeOutPageTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : BindingFragment<FragmentHomeBinding>(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews(){
        binding.apply {
            viewPager.apply {
                offscreenPageLimit = 4
                adapter = VpaHome(this@FragmentHome)
                setPageTransformer(FadeOutPageTransformer())

                tabLayout.attachToViewPager(this) { tab, pos ->
                    tab.text = getStringArray(R.array.home_tab_names)[pos]
//                    tab.setIcon(when(pos) {
//                        0 -> R.drawable.ic_api
//                        1 -> R.drawable.ic_download
//                        2 -> R.drawable.ic_create
//                        else -> throw IllegalStateException()
//                    })
                }
            }
        }
    }
}