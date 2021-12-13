package com.example.quizapp.view.fragments.dialogs.courseofstudiesselection

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfCourseOfStudiesSelectionPageBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.room.entities.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudiesSelection
import com.example.quizapp.viewmodel.VmCourseOfStudiesSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCourseOfStudiesSelectionPage: BindingFragment<BsdfCourseOfStudiesSelectionPageBinding>() {

    companion object {
        private const val FACULTY_ID_KEY = "facultyIdKey"

        fun newInstance(faculty : Faculty) = FragmentCourseOfStudiesSelectionPage().apply {
            arguments = Bundle().apply {
                putString(FACULTY_ID_KEY, faculty.id)
            }
        }
    }

    private val vmCos: VmCourseOfStudiesSelection by hiltNavDestinationViewModels(R.id.bsdfCourseOfStudiesSelection)

    private val facultyId: String by lazy { arguments!!.getString(FACULTY_ID_KEY)!! }

    private lateinit var rvAdapter : RvaCourseOfStudiesSelection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaCourseOfStudiesSelection().apply {
            onItemClicked = vmCos::onItemClicked
            selectionPredicate = { vmCos.isCourseOfStudySelected(it.id) }
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            setHasFixedSize(true)
            disableChangeAnimation()
        }
    }

    private fun initObservers(){
        vmCos.getCourseOfStudiesFlow(facultyId).collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }

        vmCos.selectedCoursesOfStudiesIdsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.rv.updateAllViewHolders()
        }
    }
}