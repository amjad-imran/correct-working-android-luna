package com.noisefit.ui.reward.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.RewardAboutData
import com.noisefit_commans.data.model.RewardAboutSubCategoryList
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AboutRewardsViewModel
@Inject
constructor(val rewardsRepository: RewardsRepository) :
    BaseViewModel() {

    private val _rewardList = MutableLiveData<ArrayList<RewardAboutSubCategoryList>>()
    val rewardList: LiveData<ArrayList<RewardAboutSubCategoryList>>
        get() = _rewardList


    var subCategoryTitle: String? = null
    private val _earnRewardList = MutableLiveData<ArrayList<RewardAboutSubCategoryList>>()
    val earnRewardList: LiveData<ArrayList<RewardAboutSubCategoryList>>
        get() = _earnRewardList


    fun getRewardListData() {
        viewModelScope.launch {
            rewardsRepository.getRewardAboutList()
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getRewardListData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                handleData(it)
                            }

                        }
                    }
                }
        }
    }

    private fun handleData(rewardAboutData:  List<RewardAboutData>) {
        val rewardList = ArrayList<RewardAboutSubCategoryList>()
        val earnList = ArrayList<RewardAboutSubCategoryList>()
        rewardAboutData.forEach { rewardAboutList ->
            if (!rewardAboutList.rewardAboutSubCategoryList.isNullOrEmpty()) {
                subCategoryTitle = rewardAboutList.title
                earnList.addAll(rewardAboutList.rewardAboutSubCategoryList!!)
            }else{
                rewardList.add(RewardAboutSubCategoryList(rewardAboutList.title,rewardAboutList.subtitle,""))
            }
        }
        _rewardList.postValue(rewardList)
        _earnRewardList.postValue(earnList)

    }
}