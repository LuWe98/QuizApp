package com.example.quizapp.view.fragments.adminscreens.managecourseofstudies

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.FragmentAdminManageCourseOfStudiesPageBinding
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.hiltNavDestinationViewModels
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudies
import com.example.quizapp.viewmodel.VmAdminManageCoursesOfStudies
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdminManageCourseOfStudiesPage: BindingFragment<FragmentAdminManageCourseOfStudiesPageBinding>() {

    companion object {
        private const val FACULTY_ID_KEY = "facultyIdKey"

        fun newInstance(faculty : Faculty?) = FragmentAdminManageCourseOfStudiesPage().apply {
            arguments = Bundle().apply {
                putString(FACULTY_ID_KEY, faculty?.id)
            }
        }
    }

    private val facultyId: String by lazy { arguments!!.getString(FACULTY_ID_KEY)!! }

    private val vmAdmin: VmAdminManageCoursesOfStudies by hiltNavDestinationViewModels(R.id.fragmentAdminManageCourseOfStudies)

    private lateinit var rvAdapter : RvaCourseOfStudies


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaCourseOfStudies().apply {
            onItemClicked = vmAdmin::onItemClicked
        }

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            setHasFixedSize(true)
            disableChangeAnimation()
        }
    }

    private fun initObservers(){
        vmAdmin.getCourseOfStudiesFlowWith(facultyId).collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }
    }
}