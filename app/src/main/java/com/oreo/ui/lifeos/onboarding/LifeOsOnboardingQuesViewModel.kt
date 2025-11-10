package com.oreo.ui.lifeos.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import com.oreo.data.model.lifeos.onboarding.AnswerX
import com.oreo.data.model.lifeos.onboarding.OnBoardQuesGetResponse
import com.oreo.data.model.lifeos.onboarding.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsOnboardingQuesViewModel @Inject constructor(
    private val userRepository: UserRepository,
): BaseViewModel() {

    var onBoardResponseData: OnBoardQuesGetResponse ?= null
    val curQues = MutableLiveData<Question>()

    var curQuesIndex: Int ?= null

    val quesAnsMap = HashMap<Int, HashSet<Int>>()
    val otherTextMap = HashMap<Int, String>() // {quesId, textField txt}

    fun getOnboardQues() {
        viewModelScope.launch {
            /*val jsonRes = """
                {
                    "questions": [
                      {
                        "id": 1,
                        "text": "Do you follow any specific diet or eating style?(e.g., keto, vegetarian, intermittent fasting) ",
                        "type": "mcq-multi-other/mcq-multi-none/text-none",
                        "answer": [
                          {
                            "id": 1,
                            "text": "Keto",
                            "addOntext": "0"
                          },
                          {
                            "id": 2,
                            "text": "Intermittent Fasting",
                            "addOntext": "0"
                          },
                          {
                            "id": 3,
                            "text": "Mediterranean",
                            "addOntext": "0"
                          },
                          {
                            "id": 4,
                            "text": "None",
                            "addOntext": "0"
                          },
                          {
                            "id": 5,
                            "text": "Other",
                            "addOntext": "1"
                          }
                        ]
                      },
                      {
                        "id": 2,
                        "text": "Do you follow any specific diet or eating style?(e.g., keto, vegetarian, intermittent fasting) ",
                        "type": "mcq-multi-other/mcq-multi-none/text-none",
                        "answer": [
                          {
                            "id": 1,
                            "text": "Keto",
                            "addOntext": "0"
                          },
                          {
                            "id": 2,
                            "text": "Intermittent Fasting",
                            "addOntext": "0"
                          },
                          {
                            "id": 3,
                            "text": "Mediterranean",
                            "addOntext": "0"
                          },
                          {
                            "id": 4,
                            "text": "None",
                            "addOntext": "0"
                          },
                          {
                            "id": 5,
                            "text": "Other",
                            "addOntext": "1"
                          }
                        ]
                      }
                    ],
                    "answers": [
                      {
                        "ques_id": 1,
                        "answer_id": [],
                        "other_text": "other data"
                      }
                    ]
                  }
            """.trimIndent()
            curQuesIndex = 0
            onBoardResponseData = Gson().fromJson(jsonRes, OnBoardQuesGetResponse::class.java)

            onBoardResponseData?.let { processData(it) }
            */
            //--
            viewModelScope.launch {
                userRepository.getLifeOsOnboardQuesAnsList().collect{ resource ->
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
                                            getOnboardQues()
                                        }

                                        override fun no() {}
                                    }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {
                                curQuesIndex = 0
                                onBoardResponseData = it
                                processData(it)
                            }
                        }
                    }
                }
            }
        }
    }

    fun processData(mainData: OnBoardQuesGetResponse){
        mainData.questions.forEach {
            it.answer.forEach { ans ->
                ans.state = if(ans.addOntext.equals("1")){
                    States.OTHER
                }else if(ans.text.equals("none", ignoreCase = true)){
                    States.NONE
                }else{
                    States.NORMAL
                }
            }
        }
        mainData.questions.first().let {
            curQues.postValue(it)
        }
    }

    fun switchToNextQuestion(){
        if(curQuesIndex == null){
            return
        }

        val nextQuesIdx = curQuesIndex!! + 1
        val nextQuesData = onBoardResponseData?.questions?.getOrNull(nextQuesIdx)
        if(nextQuesData == null){
            return
        }
        curQuesIndex = nextQuesIdx
        curQues.postValue(nextQuesData)
    }

    fun switchToPrevQuestion(){
        if(curQuesIndex == null){
            return
        }

        val prevQuesIndex = curQuesIndex!! - 1
        val prevQuesData = onBoardResponseData?.questions?.getOrNull(prevQuesIndex)
        if(prevQuesData == null){
            return
        }
        curQuesIndex = prevQuesIndex
        curQues.postValue(prevQuesData)
    }

    fun saveSelectedItems(updatedList: List<AnswerX>) {
        curQues.value?.let { it ->
            val updatedAns = updatedList.filter { it.isSelected }.map { it.id }.toSet()
            if(updatedAns.isEmpty()){
                return@let
            }
            quesAnsMap[it.id] = HashSet(updatedAns)

            // save other text
            updatedList.find { it.state==States.OTHER && it.isSelected }?.let { it1 ->
                otherTextMap[it.id] = it1.userInputText ?: ""
            }

            LOGS.d("ajsbkcac : $")
        }
    }

    fun submitQuesAnsToServer(){
        viewModelScope.launch {
            val reqArray = JsonArray()

            onBoardResponseData?.questions?.filter { it.isSavedByUser==true }?.forEach { ques ->
                val quesId = ques.id
                val ansId = JsonArray().apply {
                    ques.answer.filter {
                        it.isSelected
                    }.forEach {
                        this.add(it.id)
                    }
                }
                val addOnText = ques.answer.find { it.state== States.OTHER }?.addOntext ?: ""

                val jsonObject = JsonObject().apply {
                    this.add("ans_id", ansId)
                    this.addProperty("ques_id", quesId)
                    this.addProperty("addOntext", addOnText)
                }

                reqArray.add(jsonObject)
            }

            userRepository.submitLifeOsOnboardQuesAnsList(reqArray).collect{ resource ->
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
                                        getOnboardQues()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                        }
                    }
                }
            }
        }
    }

    enum class States {
        OTHER, NONE, NORMAL
    }


}