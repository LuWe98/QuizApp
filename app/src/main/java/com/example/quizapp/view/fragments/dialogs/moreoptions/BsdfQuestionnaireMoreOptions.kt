package com.example.quizapp.view.fragments.dialogs.moreoptions

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfQuestionnaireMoreOptionsBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.extensions.collectWhenStarted
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.recyclerview.adapters.RvaIntIdMenu
import com.example.quizapp.viewmodel.VmHome
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions
import com.example.quizapp.viewmodel.VmQuestionnaireMoreOptions.QuestionnaireMoreOptionsEvent.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfQuestionnaireMoreOptions : BindingBottomSheetDialogFragment<BsdfQuestionnaireMoreOptionsBinding>() {

    private val vm: VmQuestionnaireMoreOptions by viewModels()

    private val vmHome: VmHome by hiltNavDestinationViewModels(R.id.fragmentHome)

    private val args: BsdfQuestionnaireMoreOptionsArgs by navArgs()

    lateinit var rvAdapter: RvaIntIdMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObservers()
    }

    private fun initRecyclerView(){
        binding.tvTitle.text = args.questionnaire.title

        rvAdapter = RvaIntIdMenu().apply {
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
        vm.questionnaireMoreOptionsEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event){
                is NavigateToEditQuestionnaireScreen -> navigator.navigateToAddEditQuestionnaireScreen(event.completeQuestionnaire)
                is NavigateToCopyQuestionnaireScreen -> navigator.navigateToAddEditQuestionnaireScreen(event.completeQuestionnaire, true)
                is DeleteCreatedQuestionnaireEvent -> vmHome.deleteCreatedQuestionnaire(event.questionnaireId)
                is DeleteCachedQuestionnaireEvent -> vmHome.deleteCachedQuestionnaire(event.questionnaireId)
                is DeleteGivenAnswersOfQuestionnaire -> vmHome.deleteFilledQuestionnaire(event.questionnaireId)
                is PublishQuestionnaireEvent -> vmHome.onChangeQuestionnaireVisibilitySelected(event.questionnaireId, event.newVisibility)
                is NavigateToShareQuestionnaireDialogEvent -> navigator.navigateToShareQuestionnaireDialog(event.questionnaireId)
                NavigateBack -> navigator.popBackStack()
            }
        }
    }
}