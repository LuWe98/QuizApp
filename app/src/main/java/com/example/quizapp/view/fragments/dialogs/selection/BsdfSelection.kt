package com.example.quizapp.view.fragments.dialogs.selection

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfSelectionBinding
import com.example.quizapp.extensions.disableChangeAnimation
import com.example.quizapp.extensions.getThemeColor
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaMenuItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfSelection : BindingBottomSheetDialogFragment<BsdfSelectionBinding>() {

    private val args: BsdfSelectionArgs by navArgs()

    private val selectionType get() = args.selectionType

    private lateinit var rvAdapter: RvaMenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = run {
            if(selectionType.additionalTitleResources.isEmpty()) {
                getString(selectionType.titleRes)
            } else {
                getString(selectionType.titleRes, selectionType.additionalTitleResources.toList())
            }
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        rvAdapter = RvaMenuItem().apply {
            onItemClicked = {
                setFragmentResult(selectionType.resultKey, Bundle().apply {
                    putParcelable(selectionType.resultKey, it)
                    putParcelable(selectionType.resultKey.plus(SelectionType.INITIAL_VALUE_SUFFIX), selectionType)
                })
                navigator.popBackStack()
            }
            selectionPredicate = selectionType::isItemSelected
            selectionColor = getThemeColor(R.attr.colorOnBackground)
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }

        rvAdapter.submitList(selectionType.recyclerViewList)
    }
}