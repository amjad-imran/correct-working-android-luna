package com.oreo.ui.circadianAlignment.quiz

import androidx.lifecycle.viewModelScope
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizCircadianViewModel @Inject constructor(
    private val userRepository: UserRepository
): BaseViewModel() {

    fun getQuizData(){
        viewModelScope.launch {

        }
    }

}