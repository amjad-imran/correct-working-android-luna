package com.noisefit.ui.challengeNew.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.base.ResourcesProvider
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.ChallengeHistory
import com.noisefit_commans.data.response.ChallengeModel
import com.noisefit.data.repository.abstraction.UserActivityRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.checkDayDifferenceMoreNMinutes
import com.noisefit_commans.utils.Event
import com.noisefit_commans.models.Units
import com.noisefit_commans.utils.DateFormats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChallengeDetailsViewModel
@Inject
constructor(
    private val userActivityRepository: UserActivityRepository,
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val keyValueDataSource: KeyValueDataSource,
    private val resourcesProvider: ResourcesProvider
) : BaseViewModel() {

    var selectedHistoryDate: DateTime? = null

    var unit = Units.METRIC
    var challengeName: String = ""

    var isInitAnimPlayed = false



    private var _challengeDetails = MutableLiveData<com.noisefit_commans.data.response.ChallengeModel>()
    var challengeDetails = _challengeDetails
    var challengeId: Int = 0
    var rankAnimationValue: Int = 0

    private val _leftChallenge = MutableLiveData<Event<Boolean>>()
    val leftChallenge: LiveData<Event<Boolean>>
        get() = _leftChallenge

    private val _joinedChallenge = MutableLiveData<Event<Boolean>>()
    val joinedChallenge: LiveData<Event<Boolean>>
        get() = _joinedChallenge

    var challengeDetailLastSyncTime: Long = 0L

    private val _serverReload = MutableLiveData<Boolean>(false)
    val serverReload: LiveData<Boolean>
        get() = _serverReload

    fun shouldReloadDetailData() {
        viewModelScope.launch(Dispatchers.IO) {
            val localData =
                keyValueDataSource.getData(challengeId.toString(), KeyValueDataType.CHALLENGE_2)
            if (localData != null) {
                challengeDetailLastSyncTime = localData.getSafeLastSyncValue()
                _serverReload.postValue(challengeDetailLastSyncTime.checkDayDifferenceMoreNMinutes(5))
            } else {
                challengeDetailLastSyncTime = 0L
                _serverReload.postValue(false)
            }
        }
    }

    fun handleChallengeModel(challengeModel: com.noisefit_commans.data.response.ChallengeModel) {
        val historyDays = challengeModel.getHistoryDays()

        val historyObjs = kotlin.collections.ArrayList<com.noisefit_commans.data.response.ChallengeHistory>()
        historyDays.forEach { his ->
            val searchedObj = challengeModel.history?.find { date ->
                date.date.equals(his)
            }
            historyObjs.add(
                com.noisefit_commans.data.response.ChallengeHistory(
                    date = his,
                    progress = if (searchedObj == null) {
                        0f
                    } else {
                        searchedObj.progress
                    }
                )
            )
        }


        challengeModel.history = historyObjs
        if (challengeModel.user_rank == null) {
            challengeModel.user_rank = 0
        }
        _challengeDetails.postValue(challengeModel)
    }

    fun getChallengeDetailsByID(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            userActivityRepository.getChallengeDetailByID(forceRefresh, challengeId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                handleChallengeModel(it)

                            } /*?: getChallengeDetailsByID()*/

                            shouldReloadDetailData()
                        }
                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getChallengeDetailsByID(forceRefresh)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                    }

                }
        }
    }


    fun joinChallenge(challengeId: Int) {
        viewModelScope.launch {
            userActivityRepository.joinChallengeById(challengeId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        joinChallenge(challengeId)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Success -> {
                            resource.data?.let {
                                _joinedChallenge.postValue(Event(true))
                            }
                        }
                    }

                }
        }
    }

    fun leaveChallenge(challengeId: Int, comment: String) {
        viewModelScope.launch {
            val requestObject = JsonObject().apply {
                //this.addProperty("comment", comment)
                this.addProperty("challenge_id", challengeId)
            }
            userActivityRepository.leaveChallengeById(requestObject)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        leaveChallenge(challengeId, comment)
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Success -> {
                            resource.data?.let {
                                withContext(Dispatchers.IO) {
                                    keyValueDataSource.removeDataByKey(
                                        challengeId.toString(),
                                        KeyValueDataType.CHALLENGE_2
                                    )
                                }

                                _leftChallenge.postValue(Event(true))
                            }
                        }
                    }

                }
        }
    }


    fun getChallengeTypeIcon(): Int {
        val challengeType = _challengeDetails.value?.type
        return if (challengeType.equals("step", true)) {
            R.drawable.ic_steps
        } else if (challengeType.equals("distance", true)) {
            R.drawable.ic_distance
        } else {
            R.drawable.ic_calories

        }

    }

    fun getTotalChallengeTitle(): String {
        val challengeType = _challengeDetails.value?.type
        return if (challengeType.equals("step", true)) {
            resourcesProvider.getString(R.string.text_total_steps)
        } else if (challengeType.equals("distance", true)) {
            resourcesProvider.getString(R.string.text_total_distance_covered)
        } else if (challengeType.equals("calories", true)) {
            resourcesProvider.getString(R.string.text_total_calories)
        } else ""
    }

    fun getUserId(): Int {
        return localDataStore.getUser()?.id ?: -1
    }

    fun findCurrentPosition(history: List<com.noisefit_commans.data.response.ChallengeHistory>, currentDate: DateTime?): Int {
        var ratValue: Int = (history.size - 1)
        history.forEachIndexed { index, challengeHistory ->
            val parseFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
            val date = parseFormat.parseDateTime(challengeHistory.date)
            if (date == currentDate) {
                ratValue = index
                return ratValue
            }
        }
        return ratValue
    }

    fun getSelectedDateFormat(dateValue: String): Pair<String, String> {
        val sdfSource = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sdfLocal = SimpleDateFormat("yyyy-MM-dd", DateFormats.defaultLocale).apply {
            timeZone = TimeZone.getDefault()
        }


        val date1: Date = sdfSource.parse(dateValue)
        val calenderStart = Calendar.getInstance()
        calenderStart.time = sdfLocal.parse(sdfLocal.format(date1)) as Date
        calenderStart.set(Calendar.HOUR_OF_DAY, 0)
        calenderStart.set(Calendar.MINUTE, 0)
        calenderStart.set(Calendar.SECOND, 0)
        calenderStart.set(Calendar.MILLISECOND, 0)

        return Pair(
            SimpleDateFormat("dd").format(calenderStart.time),
            SimpleDateFormat("MMM yy").format(calenderStart.time).replace(" ", "'")
        )
    }

    fun convertCurrentTimeIntoLocalDates(): DateTime {
        val sdfSource =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", DateFormats.defaultLocale).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
        val cDate1 =
            challengeDetails.value?.currentTime?.let { sdfSource.parse(it) }
        val cDate2 = cDate1?.let { SimpleDateFormat("yyyy-MM-dd").format(it) }

        val parseFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
        return parseFormat.parseDateTime(cDate2)
    }

    fun canInviteFriends(): Boolean {
        val retValue = challengeDetails.value?.let {
            val startsInData = it.getStartsInData()
            if (startsInData.first.equals("0", true)) {
                return@let true
            }
            return@let false
        }
        return retValue ?: false
    }

}