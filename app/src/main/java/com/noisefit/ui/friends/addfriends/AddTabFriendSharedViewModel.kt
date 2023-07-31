package com.noisefit.ui.friends.addfriends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val COMMON_INTERESTS = "Common Interests"
private const val CONTACTS = "Contacts"
private const val PAST_WINNERS = "Past Winners"
private const val NEAR_ME = "Near Me"

@HiltViewModel
class AddTabFriendSharedViewModel
@Inject
constructor(val localDataStore: DataStoredInterface) : BaseViewModel() {

    private val _tabListData = MutableLiveData<List<String>>()
    val tabListData: LiveData<List<String>> = _tabListData

    val contactCount = MutableLiveData<Int>(0)
    val commonInterestCount = MutableLiveData<Int>(0)
    val pastWinnerCount = MutableLiveData<Int>(0)
    val nearMeCount = MutableLiveData<Int>(0)


    fun cleanViewModel() {
        contactCount.value = 0
        commonInterestCount.value = 0
        pastWinnerCount.value = 0
        nearMeCount.value = 0
        currentItem = 0
        _tabListData.value = ArrayList()
    }

    fun setSelected() {
        val contactsCount = localDataStore.getNoiseFitContactCount()
        val tempList = ArrayList<String>()
        var contactText = CONTACTS
        if ((contactCount.value ?: 0) > 0) {
            contactText = "$CONTACTS (${contactCount.value})"
        }

        var nearMeText = NEAR_ME
        if ((nearMeCount.value ?: 0) > 0) {
            nearMeText = "$NEAR_ME (${nearMeCount.value})"
        }

        var commonInterestText = COMMON_INTERESTS
        if ((commonInterestCount.value ?: 0) > 0) {
            commonInterestText = "$COMMON_INTERESTS (${commonInterestCount.value})"
        }

        var pastWinnerText = PAST_WINNERS
        if ((pastWinnerCount.value ?: 0) > 0) {
            pastWinnerText = "$PAST_WINNERS (${pastWinnerCount.value})"
        }


        if (contactsCount > 0 || contactsCount == -1) {
            tempList.addAll(
                arrayListOf(
                    contactText,
                    commonInterestText,
                    pastWinnerText,
                    nearMeText
                )
            )
        } else {

            tempList.addAll(
                arrayListOf(
                    commonInterestText,
                    pastWinnerText,
                    nearMeText,
                    contactText
                )
            )
        }

        _tabListData.value = tempList
    }

    var currentItem: Int = 0
}