package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LifeOsInsightsViewModel @Inject constructor() : ViewModel() {

    private val _insights = MutableLiveData<List<String>>()
    val insights: LiveData<List<String>> get() = _insights

    init {
        loadInsights()
    }

    fun loadInsights() {
        _insights.value = listOf(
            "Your circadian rhythm shifted by 45 minutes later this week.",
            "You slept 20m longer on average compared to last week.",
            "Your activity score is trending up for 3 days."
        )
    }
}
