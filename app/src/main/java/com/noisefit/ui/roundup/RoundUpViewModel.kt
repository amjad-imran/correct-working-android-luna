package com.noisefit.ui.roundup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.RoundUpResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.RoundEndOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoundUpViewModel @Inject constructor(
    val friendsRepository: FriendsRepository,
    val localDataStore: DataStoredInterface,
) : BaseViewModel() {
    var entryType: EntryType = EntryType.INTRO


    var mLastClickTime: Long? = null


    private val _yearlySummaryData = MutableLiveData<RoundUpResponse>()
    val yearlySummaryData: LiveData<RoundUpResponse>
        get() = _yearlySummaryData

    var response: RoundUpResponse? = null
    fun setResponseData(dataSet: RoundUpResponse) {
        response = dataSet
    }

    private val _roundEndOptions = MutableLiveData<RoundEndOptions?>(null)
    var roundEndOptions = _roundEndOptions
    fun setRoundOptions(goal: RoundEndOptions) {
        _roundEndOptions.postValue(goal)
    }


    fun getYearlySummaryData() {
        viewModelScope.launch {
            friendsRepository.getYearlySummary(
            ).collect { resource ->
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
                                        getYearlySummaryData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data.let { it ->

                                _yearlySummaryData.postValue(it)
                                //_yearlySummaryData.postValue(Gson().fromJson<RoundUpResponse>("{ \"name\": \"DJGDHRJD YADAV\", \"steps\": { \"title\": \"Couch potato\", \"totalMsg\": \"You have only walked\\n90987 steps\", \"totalValue\": \"90987\", \"subtitle\": \"Time to start rolling out of the sheets\\nand onto the streets!\", \"status\": \"high\" }, \"workout\": { \"title\": \"Workin’ it right\", \"totalMsg\": \"We love how much you loved\\nindoor_cycling\", \"totalValue\": \"indoor_cycling\", \"subtitle\": \"Aren’t you a\\nwork(out) of art!\", \"status\": \"found\" }, \"calories\": { \"title\": \"The slow burner\", \"totalMsg\": \"Uh oh! You burnt just\\n5016 last year\", \"totalValue\": \"5016\", \"subtitle\": \"Looks like you need to\\nbring in the heat\", \"status\": \"low\" }, \"challenges\": { \"title\": \"The rookie\", \"totalMsg\": \"You participated in\\n4 challenges\", \"totalValue\": \"4\", \"subtitle\": \"That's a good start! How about\\n trying out some more?\", \"status\": \"medium\" }, \"sleep\": { \"title\": \"The night owl\", \"totalMsg\": \"Your daily average sleep was\\n12\", \"totalValue\": \"12\", \"subtitle\": \"Let this be a reminder for you to\\ncatch some Zzzzs\", \"status\": \"low\" }, \"overall\": { \"title\": \"The slacker\", \"totalMsg\": \"You were behind\\n75% of the NoiseFit family\", \"totalValue\": \"75\", \"subtitle\": \"Looks like you’ve got some\\ncatching up to do!\", \"status\": \"low\" }, \"roundUpEnd\": { \"endingTitle\": \"Looks like you couldn’t ace \\nyour fitness goals in 2022\", \"endingSubtitle\": \"Make the most of 2023\\n#ShorRukegaNahi\", \"status\": \"low\" } }"))
                            }
                        }
                    }
                }
        }
    }

    private inline fun <reified T> Gson.fromJson(json: String) =
        fromJson<T>(json, object : TypeToken<T>() {}.type)

    fun getFirstName(): String {
        val userName = localDataStore.getUser()?.firstName
        if (!userName.isNullOrEmpty()) {
            val names = userName.split(" ")
            return names.first()
        }
        return "Stranger"
    }

    /**
     * Return true if user has Activity data
     */
    fun hasUserData(data: RoundUpResponse?): Boolean {
        if (data == null) return false
        return data.steps != null
    }

    fun setEntryType(position: Int, response: RoundUpResponse?) {
        val hasData = hasUserData(response)
        entryType = if (hasData) {
            when (position) {
                0 -> EntryType.INTRO
                1 -> EntryType.STEPS
                2 -> EntryType.WORKOUT
                3 -> EntryType.CALORIES
                4 -> EntryType.CHALLENGE
                5 -> EntryType.SLEEP
                6 -> EntryType.OVERALL
                7 -> EntryType.END
                else -> EntryType.END
            }
        } else {
            if (position == 0) {
                EntryType.INTRO
            } else {
                EntryType.END
            }
        }

    }


}