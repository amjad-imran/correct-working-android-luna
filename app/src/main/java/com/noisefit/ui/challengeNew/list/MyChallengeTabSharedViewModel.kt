package com.noisefit.ui.challengeNew.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MyChallengeTabSharedViewModel @Inject
constructor(
    val sessionManager: SessionManager
) : BaseViewModel() {
    var isTabClicked: Boolean = false
    private val _tabListData = MutableLiveData<List<String>>()
    val tabListData: LiveData<List<String>> = _tabListData
    var newCount = MutableLiveData<Int>(0)
    var joinedCount = MutableLiveData<Int>(0)
    var completedCount = MutableLiveData<Int>(0)
    var moveToNewChallenge = MutableLiveData<Event<Boolean>>()

    var updatedAtText = MutableLiveData<String>()
    var moveToJoinChallenge = MutableLiveData<Event<Boolean>>()


    fun setSelected() {
        val tempList = ArrayList<String>()
        if ((newCount.value ?: 0) > 0) {
            tempList.add("New (${newCount.value})")
        } else
            tempList.add("New")

        if ((joinedCount.value ?: 0) > 0)
            tempList.add("Joined (${joinedCount.value})")
        else
            tempList.add("Joined")

        if ((completedCount.value ?: 0) > 0)
            tempList.add("Completed (${completedCount.value})")
        else
            tempList.add("Completed")

        _tabListData.value = tempList
    }

    fun setUpdatedText(text: String) {
        updatedAtText.postValue(text.replace("minutes", "mins").replace("minute", "min"))
    }

    fun moveToNewChallenge() {
        moveToNewChallenge.postValue(Event(true))
    }

    fun moveToJoinChallenge() {
        if (!isTabClicked) {
            moveToJoinChallenge.postValue(Event(true))
        }
    }
}