package com.oreo.ui.timelineScreen.habits

import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.base.ResourcesProvider
import com.oreo.data.model.timeline.habits.HabitsByDateResponse
import com.oreo.data.model.timeline.habits.Options
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.timeline.habits.CategoryUi
import com.oreo.data.model.timeline.habits.HabitListItem
import com.oreo.data.model.timeline.habits.HabitUi
import com.oreo.data.model.timeline.habits.HabitsUiState
import com.oreo.data.model.timeline.habits.SectionBuildResult
import com.oreo.data.usecases.GetAllHabitsUseCase
import com.oreo.data.usecases.SubmitUserHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitsViewModel @Inject constructor(
    private val getAllHabitUC: GetAllHabitsUseCase,
    private val submitUserHabitsUC: dagger.Lazy<SubmitUserHabitsUseCase>,
    private val resourcesProvider: dagger.Lazy<ResourcesProvider>,
): BaseViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState(loading = true))
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    var selectedHabitsFromBundle: HabitsByDateResponse ?= null

    private var mainResponse = ArrayList<Options>()
    private var searchQuery: String = ""

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {

            _uiState.update { it.copy(loading = true, error = null) }

            getAllHabitUC.invoke().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        loadHabits()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.options?.let { sections ->
                            mainResponse = sections
                            processData(sections)
                            LOGS.d("asjkcas : $sections")
                        }
                    }
                }
            }

        }
    }

    private fun processData(sections: ArrayList<Options>, selectedHabits: Set<Int> ?= null) {

        val habitToBeMapped = selectedHabits ?: selectedHabitsFromBundle?.options?.let { list ->
            if (list.isEmpty()) {
                emptySet()
            } else {
                val set = mutableSetOf<Int>()
                list.forEach { item ->
                    item.timeTrackerOptionId?.let { optId -> set.add(optId) }
                }
                set
            }
        } ?: emptySet()

        val categories: List<CategoryUi> = sections.map { section ->
            CategoryUi(
                id = section.type ?: "",
                title = section.typeLabel ?: "",
            )
        }

        val finalCategories = ArrayList<CategoryUi>()
        if(habitToBeMapped.isNotEmpty()){
            val selectedCat = CategoryUi(
                id= "selected_category",
                title = resourcesProvider.get().getString(R.string.text_selected_habits)
            )
            finalCategories.add(selectedCat)
        }
        finalCategories.addAll(categories)

        val habits = sections.flatMap { section ->
            section.items.map { item ->

                HabitUi(
                    id = item.id,
                    name = item.options ?: "",
                    categoryId = section.type ?: ""
                )
            }
        }.toMutableList()

        selectedHabitsFromBundle?.options?.forEach { sOpt ->
            habits.find { it.id == sOpt.timeTrackerOptionId }?.let { item ->
                habits.add(item.copy(categoryId = "selected_category"))
            }
        }

        val filteredHabits = if (searchQuery.isNotEmpty()) {
            habits.filterNot {
                it.categoryId=="selected_category"
            }.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        } else {
            habits
        }

        val built = buildSectionedRows(finalCategories, filteredHabits)
        /*val finalList = built.rows.toMutableList().apply {
            if(searchQuery.isEmpty()) add(HabitListItem.EmptyBottom)
        }*/

        _uiState.update {
            it.copy(
                loading = false,
                categories = finalCategories,
                items = built.rows,
                headerPositions = built.headerPositions,
                isSearchActive = searchQuery.isNotEmpty(),
                selectedHabits = habitToBeMapped
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        processData(mainResponse, uiState.value.selectedHabits)
    }

    fun resetSearch() {
        searchQuery = ""
        processData(mainResponse, uiState.value.selectedHabits) // Revert to the full list of habits
    }

    fun toggleHabit(habitId: Int) {
        _uiState.update { state ->
            val newSet = state.selectedHabits.toMutableSet().apply {
                if (contains(habitId)) remove(habitId) else add(habitId)
            }
            state.copy(selectedHabits = newSet)
        }
    }

    fun saveHabitsToServer(selected: List<Int>, success: () -> Unit){
        viewModelScope.launch {
            val reqArray = JsonArray().apply {
                selected.map { id ->
                    add(
                        JsonObject().apply { this.addProperty("option_id" , id) }
                    )
                }
            }
            val reqObj = JsonObject().apply {
                add("habits", reqArray)
            }

            submitUserHabitsUC.get().invoke(reqObj).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        loadHabits()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            success()
                        }
                    }
                }
            }

        }
    }

    fun buildSectionedRows(
        categories: List<CategoryUi>,
        habits: List<HabitUi>
    ): SectionBuildResult {
        val rows = mutableListOf<HabitListItem>()
        val headerPositions = mutableMapOf<String, Int>()

        val habitsByCategory = habits.groupBy { it.categoryId }

        categories.forEach { cat ->
            if(searchQuery.isEmpty()){
                headerPositions[cat.id] = rows.size
                rows += HabitListItem.Header(cat)
            }

            habitsByCategory[cat.id].orEmpty().forEach { h ->
                rows += HabitListItem.Row(h)
            }
        }

        return SectionBuildResult(rows, headerPositions)
    }

}