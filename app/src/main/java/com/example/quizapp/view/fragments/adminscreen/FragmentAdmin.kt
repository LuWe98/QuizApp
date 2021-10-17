package com.example.quizapp.view.fragments.adminscreen

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.databinding.FragmentAdminBinding
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.recyclerview.adapters.RvaAdminPageUsers
import com.example.quizapp.viewmodel.VmAdmin
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdmin: BindingFragment<FragmentAdminBinding>() {

    private val viewModel : VmAdmin by viewModels()

    lateinit var rvAdapter : RvaAdminPageUsers

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        rvAdapter = RvaAdminPageUsers().apply {

        }

        binding.rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            rvAdapter.refresh()
        }
    }

    private fun initObservers(){
        viewModel.filteredPagedData.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            rvAdapter.submitData(lifecycle, it)
        }
    }
}