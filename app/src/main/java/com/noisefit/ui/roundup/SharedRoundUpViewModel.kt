package com.noisefit.ui.roundup

import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.model.RoundUpResponse
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.RoundEndOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharedRoundUpViewModel @Inject constructor(
    val friendsRepository: FriendsRepository
) : BaseViewModel() {

    private val _roundEndOptions = MutableLiveData<RoundEndOptions?>(null)
    var roundEndOptions = _roundEndOptions

    var pageIndex: Int = 0
    var response: RoundUpResponse? = null
    fun setResponseData(dataSet: RoundUpResponse) {
        response = dataSet
    }



    fun setRoundOptions(gender: RoundEndOptions) {
        _roundEndOptions.postValue(gender)
    }
}