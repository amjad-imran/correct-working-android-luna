package com.oreo.ui.lifeos.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.lifeos.onboarding.LifeOSOnboardMCQquesStates
import com.noisefit_commans.data.model.lifeos.onboarding.OnBoardQuesGetResponse
import com.noisefit_commans.data.model.lifeos.onboarding.Question
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalizeLifeOsViewModel @Inject constructor(
    private val userRepository: UserRepository,
): BaseViewModel() {

    var onBoardResponseData: OnBoardQuesGetResponse ?= null

    val personalizeListData = MutableLiveData<List<Question>>()

    fun getOnboardData(){
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
                                        getOnboardData()
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            onBoardResponseData = it
                            processOnBoardData()
                        }
                    }
                }
            }
        }
    }

    private fun processOnBoardData(){
        val mainData = onBoardResponseData!!

        val mList = ArrayList<Question>()
        //
        mainData.questions?.forEachIndexed { idx, it ->
            val curQuesAns = mainData.answers?.find { it1-> it1.ques_id==it.id}
            if(
                curQuesAns != null &&
                curQuesAns.ans_id.isNotEmpty() == true ||
                curQuesAns?.addOntext?.isNotEmpty() == true
            ){
                val addOnText = curQuesAns.addOntext
                it.isSavedByUser = true

                //
                val desc = StringBuilder()
                var descItemCount = 0

                curQuesAns.ans_id.forEach { ans_id ->
                    if(descItemCount >= 2) return@forEach
                    it.answer.find { it.addOntext!="1" && it.id==ans_id }?.let { answer ->
                        desc.append(answer.text)
                        desc.append(", ")
                        descItemCount++
                    }
                }

                if(descItemCount < 2 && addOnText.isNotEmpty()){
                    desc.append(addOnText)
                    desc.append(", ")
                }

                it.personalizeDesc = desc.substring(0, desc.length-2).toString()
                mList.add(it)
                //
            }

            it.answer.forEach { ans ->

                ans.isSelected = curQuesAns?.ans_id?.contains(ans.id) == true

                ans.state = if(ans.addOntext.equals("1")){
                    ans.userInputText = curQuesAns?.addOntext
                    LifeOSOnboardMCQquesStates.OTHER
                }else if(ans.text.equals("none", ignoreCase = true)){
                    LifeOSOnboardMCQquesStates.NONE
                }else{
                    LifeOSOnboardMCQquesStates.NORMAL
                }
            }
        }
        //

        if(mList.isNotEmpty()){
            personalizeListData.postValue(mList)
        }

    }

}