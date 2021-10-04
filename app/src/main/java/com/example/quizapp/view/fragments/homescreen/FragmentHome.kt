package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.extensions.attachToViewPager
import com.example.quizapp.extensions.getStringArray
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.view.viewpager.adapter.VpaHome
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : BindingFragment<FragmentHomeBinding>(){

    private val vmHome: VmHome by hiltNavGraphViewModels(R.id.main_nav_graph)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews(){
        binding.apply {
            viewPager.apply {
                offscreenPageLimit = 2
                adapter = VpaHome(this@FragmentHome)
                setPageTransformer(FadeOutPageTransformer())

                tabLayout.attachToViewPager(this) { tab, pos ->
                    tab.text = getStringArray(R.array.home_tab_names)[pos]
                    tab.view.setOnClickListener {
                        viewPager.setCurrentItem(pos, false)
                    }
//                    tab.setIcon(when(pos) {
//                        0 -> R.drawable.ic_api
//                        1 -> R.drawable.ic_download
//                        2 -> R.drawable.ic_create
//                        else -> throw IllegalStateException()
//                    })
                }
            }

            clSearch.setOnClickListener {
                navigator.navigateToSearchScreen()
            }
        }
    }
}