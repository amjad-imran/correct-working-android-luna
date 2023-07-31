package com.noisefit.ui.friends.profile.friendsfriend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.ui.BaseViewModel

private const val ALL = "All"
private const val MUTUAL = "Mutual"

class FFriendSharedViewModel : BaseViewModel() {

    var name:String=""
    var friendId = -1
    private val _tabListData = MutableLiveData<List<String>>()
    val tabListData: LiveData<List<String>> = _tabListData

    val allCount = MutableLiveData(0)
    var mutualCount = MutableLiveData(0)

    fun cleanViewModelData() {
        allCount.value = 0
        mutualCount.value = 0
        _tabListData.value = ArrayList()
    }

    fun setSelected() {
        val tempList = ArrayList<String>()
        val allCount = allCount.value ?: 0
        val mutualCount = mutualCount.value ?: 0
        val allText = "$ALL ($allCount)"
        val mutualText = "$MUTUAL ($mutualCount)"
        tempList.addAll(
            arrayListOf(
                allText,
                mutualText
            )
        )
        _tabListData.value = tempList
    }
}