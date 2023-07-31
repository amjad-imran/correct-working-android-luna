package com.noisefit.ui.workout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.RecentActivityResponse
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.SportsModeList
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityOptionsViewModel @Inject constructor(
    val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {

    private var recentActivities: RecentActivityResponse? = null
    private val _fetchRecentActivities = MutableLiveData<Boolean>()
    var fetchRecentActivities: LiveData<Boolean> = _fetchRecentActivities

    var masterDataSet = ArrayList<SportsModeList.SportsMode>()


    fun getActivities() {
        if (localDataStore.getUser() == null) {
            _fetchRecentActivities.postValue(true)
            return
        }
        setLoading(true)

        viewModelScope.launch {
            userRepository.getRecentActivitiesDates().collect { resource ->
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

                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            recentActivities = RecentActivityResponse(activities = it)
                            _fetchRecentActivities.postValue(true)
                        }
                    }
                }
            }
        }

    }

    fun getUserName(): String {
        var userText = "user"
        localDataStore.getUser()?.firstName?.let {
            userText = it
        }
        return userText
    }

    fun formatDataSet(dataSet: List<SportsModeList.SportsMode>): List<SportsModeList.SportsMode> {

        dataSet.forEach { sportsMode ->
            if (recentActivities?.activities?.has(sportsMode.name?.lowercase()) == true) {
                sportsMode.text =
                    getLastActivity(recentActivities?.activities?.get(sportsMode.name?.lowercase())?.asString)
            }

        }

        return dataSet

    }

    private fun getLastActivity(dateString: String?): String {
        val today = DateFormats.getTodaysDateString(7)
        val difference = DateFormats.getDateDiff(DateFormats.dateFormat, dateString, today).toInt()
        return when {
            difference == 0 -> {
                "Last activity performed today"
            }
            difference == 1 -> {
                "Last activity performed yesterday"
            }
            difference > 1 -> {
                "Last activity performed $difference days ago"
            }
            else -> {
                "Activity not performed"
            }
        }
    }

    fun getFilteredItems(searchString: String): List<SportsModeList.SportsMode> {
        val resultDataSet = ArrayList<SportsModeList.SportsMode>()
        if (searchString.isNullOrEmpty()) {
            return masterDataSet
        }
        masterDataSet.forEach { sportsMode ->
            if (sportsMode.name?.contains(searchString, true) == true) {
                resultDataSet.add(sportsMode)
            }
        }

        return resultDataSet


    }
}