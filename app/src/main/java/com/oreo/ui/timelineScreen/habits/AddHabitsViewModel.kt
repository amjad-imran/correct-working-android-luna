package com.oreo.ui.timelineScreen.habits

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.model.Options
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.timeline.habits.CategoryUi
import com.oreo.data.model.timeline.habits.Habit
import com.oreo.data.model.timeline.habits.HabitListItem
import com.oreo.data.model.timeline.habits.HabitUi
import com.oreo.data.model.timeline.habits.HabitsUiState
import com.oreo.data.model.timeline.habits.SectionBuildResult
import com.oreo.data.usecases.GetAllHabitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitsViewModel @Inject constructor(
    private val getAllHabitUC: GetAllHabitsUseCase
): BaseViewModel() {

    companion object{
        private const val MAX_SELECTION = 5
    }

    private val _uiState = MutableStateFlow(HabitsUiState(loading = true))
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private var allHabits: List<Habit> = emptyList()

    private var currentSearchQuery: String = ""

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            val respJson = """
                {
                  "categories": [
                    { "id": "recent", "name": "Recent Entries" },
                    { "id": "lifestyle", "name": "Lifestyle" },
                    { "id": "workout", "name": "Workout" },
                    { "id": "supplements", "name": "Supplements" }
                  ],
                  "habits": [
                    { "id": "1", "name": "Caffeine", "categoryId": "lifestyle", "isRecent": true },
                    { "id": "2", "name": "Sauna", "categoryId": "lifestyle", "isRecent": true },
                    { "id": "3", "name": "Cold Exposure", "categoryId": "lifestyle", "isRecent": true },
                    { "id": "4", "name": "Vitamins", "categoryId": "supplements", "isRecent": true },
                    { "id": "5", "name": "Alcohol", "categoryId": "lifestyle", "isRecent": false }
                  ]
                }
            """.trimIndent()

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
                            processData(sections)
                            LOGS.d("asjkcas : $sections")
                        }
                    }
                }
            }

        }
    }

    private fun processData(sections: ArrayList<Options>) {
        val categories: List<CategoryUi> = sections.map { section ->
            CategoryUi(
                id = section.type ?: "",
                title = section.typeLabel ?: "",
            )
        }

        val habits: List<HabitUi> = sections.flatMap { section ->
            section.items.map { item ->

                HabitUi(
                    id = item.id.toString(),
                    name = item.options ?: "",
                    categoryId = section.type ?: ""
                )
            }
        }

        val built = buildSectionedRows(categories, habits)
        val finalList = built.rows.toMutableList().apply { add(HabitListItem.EmptyBottom) }

        _uiState.update {
            it.copy(
                loading = false,
                categories = categories,
                items = finalList,
                headerPositions = built.headerPositions
            )
        }
    }

    fun toggleHabit(habitId: String) {
        _uiState.update { state ->
            val newSet = state.selectedHabits.toMutableSet().apply {
                if (contains(habitId)) remove(habitId) else add(habitId)
            }
            state.copy(selectedHabits = newSet)
        }
    }

    fun saveHabitsToServer(selected: List<String>, success: () -> Unit){
        success()
    }

    fun buildSectionedRows(
        categories: List<CategoryUi>,
        habits: List<HabitUi>
    ): SectionBuildResult {
        val rows = mutableListOf<HabitListItem>()
        val headerPositions = mutableMapOf<String, Int>()

        val habitsByCategory = habits.groupBy { it.categoryId }

        categories.forEach { cat ->
            headerPositions[cat.id] = rows.size
            rows += HabitListItem.Header(cat)

            habitsByCategory[cat.id].orEmpty().forEach { h ->
                rows += HabitListItem.Row(h)
            }
        }

        return SectionBuildResult(rows, headerPositions)
    }

}