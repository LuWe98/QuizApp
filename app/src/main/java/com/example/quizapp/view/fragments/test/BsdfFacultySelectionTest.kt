package com.example.quizapp.view.fragments.test

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.BsdfFacultySelectionTestBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.model.databases.room.LocalRepository
import com.example.quizapp.view.bindingsuperclasses.BindingFullScreenBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaFaculty
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BsdfFacultySelectionTest: BindingFullScreenBottomSheetDialogFragment<BsdfFacultySelectionTestBinding>() {

    private lateinit var rvAdapter: RvaFaculty

    @Inject
    lateinit var localRepository: LocalRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        registerObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaFaculty().apply {
            onItemClicked = {
                navigator.navigateToCourseOfStudiesTest(it.id)
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
        localRepository.allFacultiesFlow.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitList(it)
        }
    }
}