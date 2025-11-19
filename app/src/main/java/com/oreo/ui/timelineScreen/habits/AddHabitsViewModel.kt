package com.oreo.ui.timelineScreen.habits

import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.timeline.habits.Habit
import com.oreo.data.model.timeline.habits.HabitSection
import com.oreo.data.model.timeline.habits.HabitUiState
import com.oreo.data.model.timeline.habits.HabitsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddHabitsViewModel @Inject constructor(

): BaseViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState(isLoading = true))
    val uiState: StateFlow<HabitUiState> = _uiState

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

            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val response = Gson().fromJson(respJson, HabitsResponse::class.java)
                allHabits = response.habits

                val defaultCategoryId = response.categories.firstOrNull()?.id

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = response.categories,
                    selectedCategoryId = defaultCategoryId,
                    sections = buildSections(defaultCategoryId, "")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun buildSections(categoryId: String?, query: String): List<HabitSection> {
        if (categoryId == null) return emptyList()

        val filteredBySearch: (Habit) -> Boolean = { habit ->
            query.isBlank() || habit.name.contains(query, ignoreCase = true)
        }

        return when (categoryId) {
            "recent" -> {
                val recentHabits = allHabits.filter { it.isRecent }.filter(filteredBySearch)

                val lifestyleHabits = allHabits
                    .filter { it.categoryId == "lifestyle" }
                    .filter(filteredBySearch)

                val sections = mutableListOf<HabitSection>()
                if (recentHabits.isNotEmpty()) {
                    sections += HabitSection("Your Recent Entries", recentHabits)
                }
                if (lifestyleHabits.isNotEmpty()) {
                    sections += HabitSection("Lifestyle", lifestyleHabits)
                }
                sections
            }

            else -> {
                val categoryHabits = allHabits
                    .filter { it.categoryId == categoryId }
                    .filter(filteredBySearch)

                if (categoryHabits.isEmpty()) emptyList()
                else listOf(HabitSection(
                    title = _uiState.value.categories
                        .firstOrNull { it.id == categoryId }?.name ?: "",
                    habits = categoryHabits
                ))
            }
        }
    }

    fun toggleHabitSelection(habitId: String) {
        val current = _uiState.value.selectedHabits.toMutableSet()
        if (current.contains(habitId)) current.remove(habitId) else current.add(habitId)
        _uiState.value = _uiState.value.copy(selectedHabits = current)
    }

    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query
        val categoryId = _uiState.value.selectedCategoryId
        _uiState.value = _uiState.value.copy(
            sections = buildSections(categoryId, currentSearchQuery)
        )
    }

    fun onCategorySelected(categoryId: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = categoryId,
            sections = buildSections(categoryId, currentSearchQuery)
        )
    }
}