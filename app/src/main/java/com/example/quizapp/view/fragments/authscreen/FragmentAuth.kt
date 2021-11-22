package com.example.quizapp.view.fragments.authscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAuthBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.extensions.showSnackBar
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.viewpager.adapter.VpaAuth
import com.example.quizapp.view.viewpager.pagetransformer.VerticalFadePageTransformer
import com.example.quizapp.viewmodel.VmAuth
import com.example.quizapp.viewmodel.VmAuth.FragmentAuthEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAuth : BindingFragment<FragmentAuthBinding>() {

    private val viewModel : VmAuth by hiltNavDestinationViewModels(R.id.fragmentAuth)

    lateinit var vpaAdapter : VpaAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        initObservers()
        viewModel.checkIfLoggedIn()
    }

    private fun initViewPager(){
        vpaAdapter = VpaAuth(this)

        binding.viewPager.apply {
            adapter = vpaAdapter
            orientation = ViewPager2.ORIENTATION_VERTICAL
            offscreenPageLimit = 2
            isUserInputEnabled = false
            setPageTransformer(VerticalFadePageTransformer())
        }
    }

    private fun initObservers(){
        viewModel.fragmentEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                NavigateToHomeScreen -> navigator.navigateToHomeScreen()
                is SwitchPage -> binding.viewPager.setCurrentItem(event.pagePosition, true)
                is ShowMessageSnackBar -> showSnackBar(event.stringRes)
                is SetLoginCredentials -> {
                    vpaAdapter.loginFragment.binding.apply {
                        etUserName.setText(event.email)
                        etPassword.setText(event.password)
                    }
                }
                ShowLoginScreen -> binding.apply {
                    viewPager.isVisible = true
                    tvAppLogo.isVisible = true
                }
            }
        }
    }
}