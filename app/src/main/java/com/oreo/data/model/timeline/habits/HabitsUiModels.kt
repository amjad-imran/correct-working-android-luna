package com.oreo.data.model.timeline.habits

data class CategoryUi(
    val id: String,          // section.type
    val title: String,       // section.typeLabel
)

data class HabitUi(
    val id: String,          // item.id as String
    val name: String,        // item.options
    val categoryId: String   // section.type
)

//---
sealed class HabitListItem {
    data class Header(val category: CategoryUi) : HabitListItem()
    data class Row(val habit: HabitUi) : HabitListItem()
    data object EmptyBottom : HabitListItem()
}

// HabitUiState.kt
data class HabitsUiState(
    val loading: Boolean = false,
    val categories: List<CategoryUi> = emptyList(),
    val items: List<HabitListItem> = emptyList(),
    val headerPositions: Map<String, Int> = emptyMap(),
    val selectedHabits: Set<String> = emptySet(),
    val error: String? = null,

    val selectionLimitReached: Boolean = false
)

data class SectionBuildResult(
    val rows: List<HabitListItem>,
    val headerPositions: Map<String, Int> // categoryId -> adapter position
)