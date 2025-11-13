package com.oreo.ui.lifeos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsDashViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
) : ViewModel() {

    private val _questions = MutableLiveData<List<String>>()
    val questions: LiveData<List<String>> get() = _questions

    private val _whatsNew = MutableLiveData<List<String>>()
    val whatsNew: LiveData<List<String>> get() = _whatsNew

    val destinationData = MutableLiveData<LifeOsDestinations>()


    init {
        loadSuggestedQuestions()
        loadWhatsNew()
    }

    fun getLifeOsData(){
        viewModelScope.launch {
            val onBoardData = localDataStore.getLifeOsOnboardData()

            if(onBoardData==null){
                destinationData.postValue(LifeOsDestinations.BEGIN_FRAG)
                return@launch
            }

            val isAllDone = onBoardData.questions?.size == onBoardData.answers?.size
            if(!isAllDone){
                destinationData.postValue(LifeOsDestinations.QUES_FRAG)
            }

            destinationData.postValue(LifeOsDestinations.LIFE_OS_MAIN)
            /*val curProgress = 10
            val totalQues = 10
            var destination = LifeOsDestinations.LIFE_OS_MAIN
            if(curProgress==0){
                destination = LifeOsDestinations.BEGIN_FRAG
            }else if(curProgress in 1..totalQues-1){
                destination = LifeOsDestinations.QUES_FRAG
            }

            destinationData.postValue(destination)*/
        }
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

    enum class LifeOsDestinations{
        BEGIN_FRAG, QUES_FRAG, LIFE_OS_MAIN
    }
}
