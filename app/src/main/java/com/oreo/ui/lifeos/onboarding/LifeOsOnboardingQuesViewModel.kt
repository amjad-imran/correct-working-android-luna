package com.oreo.ui.lifeos.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit_commans.ui.BaseViewModel
import com.oreo.data.model.lifeos.onboarding.OnBoardQuesGetResponse
import com.oreo.data.model.lifeos.onboarding.Question
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeOsOnboardingQuesViewModel @Inject constructor(

): BaseViewModel() {

    var onBoardResponseData: OnBoardQuesGetResponse ?= null
    val curQues = MutableLiveData<Question>()

    var curQuesIndex: Int ?= null

    fun getOnboardQues() {
        viewModelScope.launch {
            val jsonRes = """
                {
                  "success": true,
                  "data": {
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
                  },
                  "message": "Onboarding questions fetched successfully",
                  "time": "1762408592630"
                }
            """.trimIndent()
            curQuesIndex = 0
            onBoardResponseData = Gson().fromJson(jsonRes, OnBoardQuesGetResponse::class.java)

            onBoardResponseData?.questions?.first()?.let {
                curQues.postValue(it)
            }
        }
    }


}