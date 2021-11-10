package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.BsdfCourseOfStudiesSelectionTestBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.launch
import com.example.quizapp.extensions.log
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.bindingsuperclasses.BindingFullScreenBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaCourseOfStudies
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BsdfCourseOfStudiesSelectionTest: BindingFullScreenBottomSheetDialogFragment<BsdfCourseOfStudiesSelectionTestBinding>() {

    private lateinit var rvAdapter: RvaCourseOfStudies

    private val args: BsdfCourseOfStudiesSelectionTestArgs by navArgs()

    @Inject
    lateinit var localRepository: LocalRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        registerObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaCourseOfStudies().apply {
            onItemClicked = {
                log("COS: ${it.name} | ID: ${it.id} | FACULTY ID: ${args.facultyId}")
            }
        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }
    }

    private fun registerObservers(){
        launch(IO) {
            val items = localRepository.getCoursesOfStudiesForFacultyAlt(args.facultyId)
            withContext(Main) {
                rvAdapter.submitList(items.coursesOfStudies)
            }
        }
    }
}