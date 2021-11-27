package com.example.quizapp.view.fragments.searchscreen

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quizapp.R
import com.example.quizapp.databinding.ChipEntryBinding
import com.example.quizapp.databinding.SearchNewBinding
import com.example.quizapp.extensions.*
import com.example.quizapp.model.databases.mongodb.documents.user.User
import com.example.quizapp.model.databases.room.entities.faculty.CourseOfStudies
import com.example.quizapp.model.databases.room.entities.faculty.Faculty
import com.example.quizapp.view.bindingsuperclasses.BindingFragment
import com.example.quizapp.view.customimplementations.backdrop.BackDropAnimListener
import com.example.quizapp.view.fragments.dialogs.courseofstudiesselection.BsdfCourseOfStudiesSelection
import com.example.quizapp.view.fragments.dialogs.facultyselection.BsdfFacultySelection
import com.example.quizapp.view.fragments.dialogs.selection.SelectionType
import com.example.quizapp.view.fragments.dialogs.usercreatorselection.BsdfUserCreatorSelection
import com.example.quizapp.view.recyclerview.adapters.RvaBrowsableQuestionnaires
import com.example.quizapp.viewmodel.VmSearch
import com.example.quizapp.viewmodel.VmSearch.SearchEvent.*
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearch : BindingFragment<SearchNewBinding>() {

    private val vmSearch: VmSearch by hiltNavDestinationViewModels(R.id.fragmentSearch)

    private lateinit var rvAdapter: RvaBrowsableQuestionnaires

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            backLayer.etSearchQuery.setText(vmSearch.searchQuery)

            backdrop.apply {
                frontLayerScrimView = frontLayer.scrimView
                setFrontLayerTopAnchor(backLayer.clSearchBar)
                setFrontLayerBotAnchor(backLayer.root)

                addAnimProgressListener(object : BackDropAnimListener {
                    override fun onProgressChanged(progress: Float) {
                        frontLayer.root.elevation = progress * 10
                    }
                })
            }

            rvAdapter = RvaBrowsableQuestionnaires(vmSearch).apply {

            }

            frontLayer.rv.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = rvAdapter
            }
        }
    }


    private fun initListeners(){
        binding.apply {
            backLayer.apply {
                btnFilter.onClick {
                    navigator.navigateToSearchFilterDialog()
//                    backdrop.toggle()
                }

                tvSortBy.onClick(vmSearch::onSortByCardClicked)

                addChipAuthor.onClick(vmSearch::onAuthorAddButtonClicked)

                addChipCos.onClick(vmSearch::onCourseOfStudiesAddButtonClicked)

                addChipFaculty.onClick(vmSearch::onFacultyCardAddButtonClicked)

                etSearchQuery.onTextChanged(vmSearch::onSearchQueryChanged)
            }

            frontLayer.apply {
                swipeRefreshLayout.setOnRefreshListener {
                    rvAdapter.refresh()
                }
            }
        }
    }

    private fun initObservers() {
        setFragmentResultListener(BsdfUserCreatorSelection.USER_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.apply {
                classLoader = User::class.java.classLoader
                getParcelableArray(key)?.let { vmSearch.onSelectedUserUpdateReceived(it as Array<User>) }
            }
        }

        setFragmentResultListener(BsdfFacultySelection.FACULTY_SELECTION_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmSearch::onSelectedFacultyUpdateReceived)
        }

        setFragmentResultListener(BsdfCourseOfStudiesSelection.COURSE_OF_STUDIES_RESULT_KEY) { key, bundle ->
            bundle.getStringArray(key)?.let(vmSearch::onSelectedCourseOfStudiesUpdateReceived)
        }

        setSelectionTypeListener(vmSearch::onSortByUpdateReceived)

        vmSearch.sortByFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.backLayer.apply {
                ivSortBy.setImageDrawable(it.iconRes)
                tvSortBy.setText(it.textRes)
            }
        }

        vmSearch.selectedCourseOfStudiesStateFlow.collectWhenStarted(viewLifecycleOwner) { coursesOfStudies ->
            setUpChipsForChipGroup(
                binding.backLayer.chipGroupCos,
                coursesOfStudies,
                CourseOfStudies::abbreviation,
                vmSearch::removeFilteredCourseOfStudies
            )
        }

        vmSearch.selectedFacultyStateFlow.collectWhenStarted(viewLifecycleOwner) { faculties ->
            setUpChipsForChipGroup(
                binding.backLayer.chipGroupFaculty,
                faculties,
                Faculty::abbreviation,
                vmSearch::removeFilteredFaculty
            )
        }

        vmSearch.selectedUserCreatorsStateFlow.collectWhenStarted(viewLifecycleOwner) { users ->
            setUpChipsForChipGroup(
                binding.backLayer.chipGroupAuthor,
                users.toList(),
                User::userName,
                vmSearch::removeFilteredUser
            )
        }

        vmSearch.filteredPagedData.collectWhenStarted(viewLifecycleOwner) {
            rvAdapter.submitData(it)
            binding.frontLayer.swipeRefreshLayout.isRefreshing = false
        }

        vmSearch.searchEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToCourseOfStudiesSelectionScreen -> navigator.navigateToCourseOfStudiesSelection(event.selectedCourseOfStudiesIds)
                is NavigateToFacultySelectionScreen -> navigator.navigateToFacultySelection(event.selectedFacultyIds)
                is NavigateToUserSelectionScreen -> navigator.navigateToUserCreatorSelectionScreen(event.selectedUsers)
                is NavigateToSortBySelection -> navigator.navigateToSelectionDialog(SelectionType.SortingTypeSelection(event.sortBy))
            }
        }
    }


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