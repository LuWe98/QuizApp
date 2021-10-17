package com.example.quizapp.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfQuestionnaireMoreOptionsBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaBsdfMenu
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions.QuestionnaireMoreOptionsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfQuestionnaireMoreOptions : BindingBottomSheetDialogFragment<BsdfQuestionnaireMoreOptionsBinding>() {

    private val vm: VmQuestionnaireMoreOptions by viewModels()

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    lateinit var rvAdapter: RvaBsdfMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){

        rvAdapter = RvaBsdfMenu().apply {
            onItemClicked = vm::onMenuItemClicked
        }

        binding.rv.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
            disableChangeAnimation()
        }

        rvAdapter.submitList(vm.getQuestionnaireMoreOptionsMenu())
    }

    private fun initObservers(){
        vm.questionnaireMoreOptionsEventChannelFlow.collect(lifecycleScope) { event ->
            when(event){
                is NavigateToEditQuestionnaireScreen -> {
                    navigator.navigateToAddQuestionnaireScreen(event.questionnaireId)
                }
                is DeleteCreatedQuestionnaireEvent -> {
                    vmHome.deleteCreatedQuestionnaire(event.questionnaireId)
                }
                is DeleteCachedQuestionnaireEvent -> {
                    vmHome.deleteCachedQuestionnaire(event.questionnaireId)
                }
                is DeleteGivenAnswersOfQuestionnaire -> {
                    vmHome.deleteGivenAnswersOfQuestionnaire(event.questionnaireId)
                }
                NavigateBack -> {
                    navigator.popBackStack()
                }
            }
        }
    }
}