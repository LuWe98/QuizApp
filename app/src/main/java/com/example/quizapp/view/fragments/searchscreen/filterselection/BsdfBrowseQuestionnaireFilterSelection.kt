package com.example.quizapp.view.fragments.searchscreen.filterselection

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.quizapp.R
import com.example.quizapp.databinding.BsdfBrowseQuestionnaireFilterSelectionBinding
import com.example.quizapp.databinding.ChipEntryBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.mongodb.documents.user.AuthorInfo
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingBottomSheetDialogFragment
import com.example.quizapp.view.fragments.dialogs.authorselection.remote.BsdfRemoteAuthorSelection
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelection
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection
import com.example.quizapp.viewmodel.VmBrowseQuestionnaireFilterSelection.FilterEvent.*
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfBrowseQuestionnaireFilterSelection : BindingBottomSheetDialogFragment<BsdfBrowseQuestionnaireFilterSelectionBinding>() {

    companion object {
        const val QUESTIONNAIRE_FILTER_RESULT_KEY = "questionnaireFilterResultKey"
    }

    private val vmFilter: VmBrowseQuestionnaireFilterSelection by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding.apply {
            tvOrderBy.onClick(vmFilter::onOrderByCardClicked)
            tvOrderAscending.onClick(vmFilter::onOrderAscendingCardClicked)
            addChipAuthor.onClick(vmFilter::onAuthorAddButtonClicked)
            addChipCos.onClick(vmFilter::onCourseOfStudiesAddButtonClicked)
            addChipFaculty.onClick(vmFilter::onFacultyCardAddButtonClicked)
            btnApply.onClick(vmFilter::onApplyButtonClicked)
        }
    }

    private fun initObservers() {
        setFragmentResultListener(BsdfRemoteAuthorSelection.AUTHOR_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.apply {
                classLoader = AuthorInfo::class.java.classLoader
                getParcelableArray(key)?.let { vmFilter.onSelectedAuthorsUpdateReceived(it as Array<AuthorInfo>) }
            }
        }

        setFragmentResultListener(BsdfFacultySelection.FACULTY_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmFilter::onSelectedFacultyUpdateReceived)
        }

        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmFilter::onSelectedCourseOfStudiesUpdateReceived)
        }

        setSelectionTypeListener(vmFilter::onSortByUpdateReceived)

        vmFilter.orderByStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.apply {
                ivSortBy.setImageDrawable(it.iconRes)
                tvOrderBy.setText(it.textRes)
            }
        }

        vmFilter.orderAscendingStateFlow.collectWhenStarted(viewLifecycleOwner) { ascending ->
            binding.apply {
                tvOrderAscending.setText(if(ascending) R.string.ascending else R.string.descending)
//                ivOrderAscending.setImageDrawable(if(ascending) R.drawable.ic_arrow_drop_down else R.drawable.ic_arrow_drop_up)
                tvOrderAscending.clearAnimation()
                ivOrderAscending.animate()
                    .rotation(if(ascending) 0f else 180f)
                    .setDuration(300)
                    .start()
            }
        }

        vmFilter.selectedCourseOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) { coursesOfStudies ->
            setUpChipsForChipGroup(
                binding.chipGroupCos,
                coursesOfStudies,
                CourseOfStudies::abbreviation,
                vmFilter::removeFilteredCourseOfStudies
            )
        }

        vmFilter.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            setUpChipsForChipGroup(
                binding.chipGroupFaculty,
                faculties,
                Faculty::abbreviation,
                vmFilter::removeFilteredFaculty
            )
        }

        vmFilter.selectedAuthorsStateFlow.collectWhenStarted(viewLifecycleOwner) { authors ->
            setUpChipsForChipGroup(
                binding.chipGroupAuthor,
                authors.toList(),
                AuthorInfo::userName,
                vmFilter::removeFilteredAuthor
            )
        }

        vmFilter.searchFilterEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToCourseOfStudiesSelectionScreen -> navigator.navigateToCourseOfStudiesSelection(event.selectedCourseOfStudiesIds)
                is NavigateToFacultySelectionScreen -> navigator.navigateToFacultySelection(event.selectedFacultyIds)
                is NavigateToUserSelectionScreen -> navigator.navigateToRemoteAuthorSelection(event.selectedUsers)
                is NavigateToSortBySelection -> navigator.navigateToSelectionDialog(SelectionType.BrowsableOrderBySelection(event.browsableOrderBy))
                is ApplyFilterPreferencesEvent -> {
                    setFragmentResult(QUESTIONNAIRE_FILTER_RESULT_KEY, Bundle().apply {
                        putParcelable(QUESTIONNAIRE_FILTER_RESULT_KEY, event.resultBrowse)
                    })
                    navigator.popBackStack()
                }
            }
        }
    }

    //TODO -> Problem mit letztem Chip anschauen -> Der bleibt irgendwie übrig
    private inline fun <reified T> setUpChipsForChipGroup(
        chipGroup: ChipGroup,
        list: List<T>,
        crossinline textProvider: (T) -> (String),
        crossinline onClickCallback: (T) -> (Unit)
    ) {
        chipGroup.apply {
            val mapped = children.filterIndexed { index, _ -> index != childCount - 1 }.map { it.tag as T }
            val itemsToInsert = list - mapped.toSet()
            val itemsToRemove = mapped - list.toSet() - itemsToInsert.toSet()

            itemsToRemove.forEach { tagToFind ->
                children.firstOrNull { it.tag == tagToFind }?.let(::removeView)
            }

            itemsToInsert.forEach { entry ->
                ChipEntryBinding.inflate(layoutInflater).root.apply {
                    tag = entry
                    text = textProvider.invoke(entry)
                    onClick { onClickCallback.invoke(this.tag as T) }

                    //TODO -> Long Click anschauen!
                    onLongClick { showToast("${this.tag}") }
                }.let {
                    addView(it, 0)
                }
            }
        }
    }
}