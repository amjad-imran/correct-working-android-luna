package com.oreo.ui.circadianAlignment.quiz

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.oreo.data.model.circadian.CircadianQuizResponseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizCircadianViewModel @Inject constructor(
    private val userRepository: UserRepository
): BaseViewModel() {

    val quizData = MutableLiveData<ArrayList<CircadianQuizResponseModel>>()

    val quesOptionMap = HashMap<Int, Int>()

    val quizDataSubmitted = MutableLiveData<Event<Boolean>>()

    fun getQuizData(){
        viewModelScope.launch {
            userRepository.getCircadianQuizData().collect{ resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {

                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            quizData.postValue(it as ArrayList)
                        }
                    }
                }
            }
        }
    }

    fun handleQuizOptionClick(pair: Pair<Int, Int>) {
        quesOptionMap[pair.first] = pair.second
        if(quizData.value?.isNotEmpty() == true && quizData.value?.size == quesOptionMap.size){
            submitQuizQuesAndAnswers(true)
        }
    }

    fun submitQuizQuesAndAnswers(isAllDone: Boolean){
        viewModelScope.launch {

            val reqObj = JsonObject()
            val jsonArrayRes = JsonArray()
            if(isAllDone){
                quesOptionMap.forEach { (quesId, ansId) ->
                    jsonArrayRes.add(
                        JsonObject().apply {
                            this.addProperty("ques_id", quesId)
                            this.addProperty("ans_id", ansId)
                        }
                    )
                }
            }
            reqObj.add("data", jsonArrayRes)

            userRepository.submitCircadianQuizData(reqObj).collect{ resource ->
                when(resource){
                    is Resource.GenericError -> {}
                    is Resource.Loading -> {}
                    is Resource.NetworkError -> {}
                    is Resource.Success<*> -> {
                        quizDataSubmitted.postValue(Event(true))
                    }
                }
            }
        }
    }

}