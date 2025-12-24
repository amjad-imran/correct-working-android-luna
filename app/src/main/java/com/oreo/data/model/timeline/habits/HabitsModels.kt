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