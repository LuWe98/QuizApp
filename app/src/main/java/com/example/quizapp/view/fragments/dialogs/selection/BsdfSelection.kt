package com.example.quizapp.view.fragments.dialogs.selection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfSelectionBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaSelectionDialog
import com.example.quizapp.viewmodel.VmSelectionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfSelection : BindingBottomSheetDialogFragment<BsdfSelectionBinding>() {

    private val vmSelection: VmSelectionDialog by viewModels()

    private lateinit var rvAdapter: RvaSelectionDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iniViews()
    }

    private fun iniViews() {
        binding.tvTitle.text = vmSelection.selectionType.titleProvider(requireContext())

        vmSelection.selectionType.titleProvider(requireContext()).let { title ->
            if(title == null) {
                binding.tvTitle.isVisible = false
            } else {
                binding.tvTitle.text = title
            }
        }

        rvAdapter = RvaSelectionDialog().apply {
            onItemClicked = vmSelection::onItemSelected
            selectionPredicate = vmSelection::isItemSelected
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }

        rvAdapter.submitList(vmSelection.selectionType.recyclerViewList)
    }
}