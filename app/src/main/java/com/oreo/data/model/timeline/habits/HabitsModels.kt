package com.oreo.data.model.timeline.habits

// HabitCategory.kt
data class HabitCategory(
    val id: String,
    val name: String,
)

// Habit.kt
data class Habit(
    val id: String,
    val name: String,
    val categoryId: String,
    val isRecent: Boolean,
)

// HabitSection.kt
// For UI – a section like "Your Recent Entries" or "Lifestyle"
data class HabitSection(
    val title: String,
    val habits: List<Habit>
)

// API response that powers the whole screen
data class HabitsResponse(
    val categories: List<HabitCategory>,
    val habits: List<Habit>
)

// ------------------------

sealed class HabitListItem {
    data class Header(val title: String) : HabitListItem()
    data class Row(val habit: Habit) : HabitListItem()
}

fun HabitUiState.toListItems(): List<HabitListItem> {
    val items = mutableListOf<HabitListItem>()
    sections.forEach { section ->
        items += HabitListItem.Header(section.title)
        items += section.habits.map { HabitListItem.Row(it) }
    }
    return items
}

// HabitUiState.kt
data class HabitUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<HabitCategory> = emptyList(),
    val sections: List<HabitSection> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedHabits: Set<String> = emptySet(), // habit IDs
)