package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageCourseOfStudiesBinding
import com.example.quizapp.databinding.TabLayoutViewFacultyBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.dispatcher.fragmentresult.setFragmentResultEventListener
import com.example.quizapp.view.viewpager.adapter.VpaManageCourseOfStudies
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.ManageCourseOfStudiesEvent.ClearSearchQueryEvent
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.ManageCourseOfStudiesEvent.ShowMessageSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageCourseOfStudies : BindingFragment<FragmentAdminManageCourseOfStudiesBinding>() {

    private val vmAdmin: VmAdminManageCoursesOfStudies by hiltNavDestinationViewModels(R.id.fragmentAdminManageCourseOfStudies)

    private lateinit var vpAdapter: VpaManageCourseOfStudies

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMaterialZAxisAnimationForReceiver()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.etSearchQuery.setText(vmAdmin.searchQuery)

        val facultyList = vmAdmin.getFacultiesWithPlaceholder(requireContext())

        vpAdapter = VpaManageCourseOfStudies(this, facultyList)

        binding.viewPager.apply {
            adapter = vpAdapter
            setPageTransformer(FadeOutPageTransformer())
            onPageSelected { position ->
                updateTabs(position)
            }
        }

        binding.tabLayout.attachToViewPager(binding.viewPager) { tab, index ->
            TabLayoutViewFacultyBinding.inflate(layoutInflater).apply {
                tvText.text = facultyList[index].abbreviation
                tab.customView = root
                tab.view.onClick {
                    binding.viewPager.setCurrentItem(index, false)
                }
            }
        }
    }

    private fun updateTabs(newPosition: Int){
        binding.tabLayout.forEachTab { tab, i ->
            TabLayoutViewFacultyBinding.bind(tab.customView!!).apply {
                val textColor = if(i == newPosition) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                tvText.setTextColor(textColor)
                selectedView.animate()
                    .alpha(if (i == newPosition) 1f else 0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(if (i == newPosition) 650L else 0L)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(vmAdmin::onBackButtonClicked)
            btnAdd.onClick(vmAdmin::onAddCourseOfStudiesButtonClicked)
            etSearchQuery.onTextChanged(vmAdmin::onSearchQueryChanged)
            btnSearch.onClick(vmAdmin::onClearSearchQueryClicked)
        }
    }

    private fun initObservers(){

        setFragmentResultEventListener(vmAdmin::onCosMoreOptionsSelectionResultReceived)

        setFragmentResultEventListener(vmAdmin::onDeleteCourseOfStudiesConfirmationRequestReceived)

        vmAdmin.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isEmpty()
            }
        }

        vmAdmin.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}