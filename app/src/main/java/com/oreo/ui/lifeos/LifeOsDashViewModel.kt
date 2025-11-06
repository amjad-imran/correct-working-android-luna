package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LifeOsDashViewModel @Inject constructor() : ViewModel() {

    private val _questions = MutableLiveData<List<String>>()
    val questions: LiveData<List<String>> get() = _questions

    private val _whatsNew = MutableLiveData<List<String>>()
    val whatsNew: LiveData<List<String>> get() = _whatsNew

    init {
        loadSuggestedQuestions()
        loadWhatsNew()

    }

    fun loadSuggestedQuestions() {
        _questions.value = listOf(
            "Teach me about my sleep score",
            "Create a diet plan for me",
            "Create a workout plan for me"
        )
    }



    fun loadWhatsNew() {
        _whatsNew.value = listOf(
            "New Timeline: streamlined logging for Supplements and Recovery",
            "AI coaching improvements: better context understanding and tips",
            "Dashboard tweaks: faster loading and refreshed visuals"
        )
    }
}
