package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageCourseOfStudiesBinding
import com.example.quizapp.databinding.TabLayoutViewFacultyBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.fragments.dialogs.confirmation.ConfirmationType
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.viewpager.adapter.VpaManageCourseOfStudies
import com.example.quizapp.view.viewpager.pagetransformer.FadeOutPageTransformer
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies.FragmentAdminManageCourseOfStudiesEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageCourseOfStudies : BindingFragment<FragmentAdminManageCourseOfStudiesBinding>() {

    private val vmAdmin: VmAdminManageCoursesOfStudies by hiltNavDestinationViewModels(R.id.fragmentAdminManageCourseOfStudies)

    private lateinit var vpAdapter: VpaManageCourseOfStudies

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.etSearchQuery.setText(vmAdmin.searchQuery)

        val facultyList = vmAdmin.getFacultiesWithPlaceholder
        vpAdapter = VpaManageCourseOfStudies(this, facultyList)

        binding.viewPager.apply {
            adapter = vpAdapter
            setPageTransformer(FadeOutPageTransformer())
            onPageSelected { position ->
                updateTabs(position)
                changeTitleWithAnimation(facultyList[position])
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

    private fun changeTitleWithAnimation(faculty: Faculty){
        binding.tvFacultyName.apply {
            val duration = if(text.isEmpty()) 0L else 150L
            animate().setDuration(duration)
                .alpha(0f)
                .withEndAction {
                    text = faculty.name
                    animate().setDuration(duration)
                        .alpha(1f)
                        .start()
                }.start()
        }
    }

    private fun updateTabs(newPosition: Int){
        binding.tabLayout.forEachTab { tab, i ->
            val factors = if (i == newPosition) 1f else 0f
            val duration = if (i == newPosition) 300L else 150L

            TabLayoutViewFacultyBinding.bind(tab.customView!!).apply {
                val textColor = if(i == newPosition) getColor(R.color.white) else getThemeColor(R.attr.colorControlNormal)
                tvText.setTextColor(textColor)
                selectedView.animate()
                    .scaleX(factors)
                    .scaleY(factors)
                    .alpha(factors)
                    .setDuration(duration)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun initListeners(){
        binding.apply {
            btnBack.onClick(navigator::popBackStack)
            fabAdd.onClick(navigator::navigateToAdminAddEditCourseOfStudies)
            etSearchQuery.onTextChanged(vmAdmin::onSearchQueryChanged)
            btnSearch.onClick(vmAdmin::onClearSearchQueryClicked)
        }
    }

    private fun initObservers(){
        setSelectionTypeWithParsedValueListener(vmAdmin::onMoreOptionsItemSelected)

        setConfirmationTypeListener(vmAdmin::onDeleteCourseOfStudiesConfirmed)

        vmAdmin.searchQueryStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.btnSearch.changeIconOnCondition {
                it.isBlank()
            }
        }

        vmAdmin.fragmentAdminManageCourseOfStudiesEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is NavigateToManageCourseOfStudiesMoreOptionsEvent ->
                    navigator.navigateToSelectionDialog(SelectionType.CourseOfStudiesMoreOptionsSelection(event.courseOfStudies))
                is NavigateToAddEditCourseOfStudiesEvent ->
                    navigator.navigateToAdminAddEditCourseOfStudies(event.courseOfStudies)
                is NavigateToConfirmDeletionEvent ->
                    navigator.navigateToConfirmationDialog(ConfirmationType.DeleteCourseOfStudiesConfirmation(event.courseOfStudies))
                is ShowMessageSnackBar -> showSnackBar(event.messageRes)
                ClearSearchQueryEvent -> binding.etSearchQuery.setText("")
            }
        }
    }
}