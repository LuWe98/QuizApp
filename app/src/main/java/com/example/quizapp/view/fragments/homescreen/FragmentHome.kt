package com.example.quizapp.view.fragments.homescreen

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentHomeBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.viewpager.adapter.VpaHome
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmHome.*
import com.example.quizapp.viewmodel.VmHome.FragmentHomeEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : BindingFragment<FragmentHomeBinding>() {

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private lateinit var vpAdapter: VpaHome

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            vpAdapter = VpaHome(this@FragmentHome)

            viewPager.apply {
                offscreenPageLimit = 2
                adapter = vpAdapter
                setPageTransformer(FadeOutPageTransformer())

                tabLayout.attachToViewPager(this) { tab, pos ->
                    tab.text = getStringArray(R.array.home_tab_names)[pos]
                }
            }
        }
    }

    private fun initObservers() {
        vmHome.fragmentHomeEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackBarMessageBar -> {
                    showSnackBar(event.messageRes)
                }
                is ShowUndoDeleteCreatedQuestionnaireSnackBar -> {
                    showSnackBar(
                        R.string.questionnaireDeleted,
                        anchorView = bindingActivity.findViewById(R.id.bottomAppBar),
                        onDismissedAction = { vmHome.onDeleteCreatedQuestionnaireConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmHome.onUndoDeleteCreatedQuestionnaireClicked(event) }
                    )
                }
                is ShowUndoDeleteCachedQuestionnaireSnackBar -> {
                    showSnackBar(
                        R.string.questionnaireDeleted,
                        anchorView = bindingActivity.findViewById(R.id.bottomAppBar),
                        onDismissedAction = { vmHome.onDeleteCachedQuestionnaireConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent = { vmHome.onUndoDeleteCachedQuestionnaireClicked(event) }
                    )
                }
                is ShowUndoDeleteAnswersOfQuestionnaireSnackBar -> {
                    showSnackBar(
                        R.string.answersDeleted,
                        anchorView = bindingActivity.findViewById(R.id.bottomAppBar),
                        onDismissedAction = { vmHome.onDeleteFilledQuestionnaireConfirmed(event) },
                        actionTextRes = R.string.undo,
                        actionClickEvent =  { vmHome.onUndoDeleteFilledQuestionnaireClicked(event) }
                    )
                }
                is ChangeProgressVisibility -> binding.syncProgress.isVisible = event.visible
            }
        }
    }
}