package com.noisefit

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.model.DashboardBanner
import com.noisefit_commans.data.model.User
import com.noisefit.data.remote.UserLocationUpdatedResponse
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.request.UpdateAdditionalDetailRequest
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.data.repository.abstraction.AuthenticationRepository
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.repository.abstraction.FriendsRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.session.SessionManager
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.autostart.AutoStartUtil
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.Content
import com.noisefit_commans.models.ColorFitDevice
import com.noisefit_commans.models.UserLocation
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.DateFormats.checkTimeDifferenceMoreThanNDays
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager,
    val watchDataStore: WatchDataStore,
    val watchesSDK: WatchesSDK,
    val autoStartUtil: AutoStartUtil,
    private val deviceRepository: DeviceRepository,
    private val geoCoder: Geocoder,
    private val authenticationRepository: AuthenticationRepository,
    private val friendsRepository: FriendsRepository,
) : BaseViewModel() {

    var playNplAnim: Boolean = false
    var lat: Double? = null
    var long: Double? = null


    var logFileName: String? = null
    var lastConnectedDevice: ColorFitDevice? = null

    private var _logFileUploadStatus = MutableLiveData<Event<Boolean>>()
    var logFileUploadStatus: LiveData<Event<Boolean>> = _logFileUploadStatus

    var bottomNavigation = MutableLiveData<Event<BottomNavOption>>()

    private val _randomWatchFaces = MutableLiveData<List<List<Watchface2>>>()
    val randomWatchFaces: LiveData<List<List<Watchface2>>> = _randomWatchFaces

    val checkBluetooth = MutableLiveData<Event<Boolean>>()
    val checkLocation = MutableLiveData<Event<Boolean>>()

    val fetchLocation = MutableLiveData<Event<Boolean>>()

    val displayBanners: MutableLiveData<List<DashboardBanner>> =
        MutableLiveData<List<DashboardBanner>>()

    private val _contentData = MutableLiveData<Content>()
    val contentData: LiveData<Content> = _contentData


    private val _showInterestSelector = MutableLiveData<Event<Boolean>>()
    val showInterestSelector: LiveData<Event<Boolean>>
        get() = _showInterestSelector

    var isOtaUpdateShownToUser = false

    var isLocationApiCalled = false

    init {
        //getShopBanners()
    }


    fun checkUserInterestStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            if (localDataStore.getUser()?.interests.isNullOrEmpty() && !localDataStore.isInterestCancelled()) {
                _showInterestSelector.postValue(Event(true))
            }
        }
    }

    fun checkConfigData() {
        if (localDataStore.getEndGameList().isNullOrEmpty()) {
            getConfig()
        }
    }

    fun getConfig() {
        viewModelScope.launch {
            deviceRepository.getConfig().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data?.let {

                            it.endGames?.let { endGames ->
                                localDataStore.setEndGameList(endGames)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }

    }


    private fun validateUserData(user: User) {
        var updateUserData = false
        user.userGoals?.let {
            if (it.stepGoal == 0) {
                updateUserData = true
                it.stepGoal = 10000
            }
            if (it.caloriesGoal == 0) {
                updateUserData = true
                it.caloriesGoal = 400
            }
            if (it.distanceGoal == 0) {
                updateUserData = true
                it.distanceGoal = 8000
            }
        }

        if (updateUserData) {
            val request = UpdateAdditionalDetailRequest(
                userInfo = user.userInfo,
                stepGoal = user.userGoals
            )
            viewModelScope.launch {
                userRepository.saveAdditionalDetails(request).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                getUserProfile()
                            }
                        }
                        else -> {}
                    }
                }
            }

        }
    }

    fun getUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.user?.let { user ->
                            authenticationRepository.saveUserInfo(user, true)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun hasCrashLog(): Boolean {
        return !localDataStore.getCrashLog().isNullOrEmpty()
    }

    fun uploadLogFile(file: File) {
        viewModelScope.launch {
            userRepository.uploadCrashLogFile(file).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.let {
                            _logFileUploadStatus.value = Event(true)
                        }
                    }
                    else -> {}
                }
            }
        }

    }

    fun deleteLogFile(context: Context) {
        logFileName?.let {
            val path: File? = context.externalCacheDir
            val file = File(path, it)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    fun getShouldShowReview(): Boolean {
        val installTimeStamp = localDataStore.getFirstOpenTimeStamp()
        val lastReviewShownTimeStamp = localDataStore.getLastReviewShownTimeStamp()

        val laterNowTimeStamp=localDataStore.getLaterNowLastReviewShownTimeStamp()
        val tenDayLaterNowTimeStamp=localDataStore.getTenDayLaterNowLastTimeStamp()

        /*LOGS.d(
            "MainActivity",
            "Difference : ${installTimeStamp.checkTimeDifferenceMoreThanNDays(25)} |" +
                    "Award Count: ${localDataStore.getAwardCount()} |" +
                    "Goal Count: ${localDataStore.getGoalCompletionCount()} |" +
                    "getCustomWatchFaceTransferCount Count: ${localDataStore.getCustomWatchFaceTransferCount()} |" +
                    "getWatchFaceTransferCount Count: ${localDataStore.getWatchFaceTransferCount()} |" +
                    "Workout Share Count : ${localDataStore.getWorkoutShareCount()}"
        )*/


        val installDays =
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installTimeStamp)

        LOGS.d("MainActivity ", "installDays $installDays")
        var shouldShowReview = false
        try {
            if (lastReviewShownTimeStamp == 0L && installDays.toInt() < 15) {
                return false
            } else if (lastReviewShownTimeStamp != 0L && installDays.toInt() >= 15) {
                val lastReviewDaysDifference =
                    TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastReviewShownTimeStamp)
                if (lastReviewDaysDifference < 15) return false
            }
        } catch (exp: Exception) {
            exp.printStackTrace()
            return false
        }

        if (installTimeStamp.checkTimeDifferenceMoreThanNDays(25)) {
            if (!localDataStore.is25DaysReviewShown()) {
                localDataStore.set25DaysReviewShown(true)
                shouldShowReview = true
            }
        }

        if (laterNowTimeStamp.checkTimeDifferenceMoreThanNDays(5)){
            if (localDataStore.isShowReviewPopUp()){
                localDataStore.setShowReviewPopUp(false)
                shouldShowReview=true
            }
        }
        if (tenDayLaterNowTimeStamp.checkTimeDifferenceMoreThanNDays(10)){
            if (localDataStore.isShowReviewPopUp()){
                localDataStore.setShowReviewPopUp(false)
                shouldShowReview=true
            }
        }


        if (localDataStore.getAwardCount() >= 1) {
            shouldShowReview = true
        }
        if (localDataStore.getGoalCompletionCount() >= 6) {
            shouldShowReview = true
        }
        /*if (localDataStore.getWorkoutShareCount() >= 4) {
            shouldShowReview = true
        }*/

        if (localDataStore.getCustomWatchFaceTransferCount() >= 3) {
            shouldShowReview = true
        }
        if (localDataStore.getWatchFaceTransferCount() >= 3) {
            shouldShowReview = true
        }
        if (localDataStore.getActivitySyncCount() >= 1) {
            shouldShowReview = true
        }
        if (localDataStore.getJoinedChallengeCount() >= 1) {
            shouldShowReview = true
        }
        if (localDataStore.getFriendCount() >= 1) {
            shouldShowReview = true
        }
        if (localDataStore.getFeedPostCreateCount()>=1){
            shouldShowReview=true
        }

        if (shouldShowReview) {
            localDataStore.setLastReviewShownTimeStamp(System.currentTimeMillis())
            localDataStore.setGoalCompletionCount(0)
            localDataStore.setWorkoutShareCount(0)
            localDataStore.setAwardCount(0)
            localDataStore.setCustomWatchFaceTransferCount(0)
            localDataStore.setWatchFaceTransferCount(0)
            localDataStore.setActivitySyncCount(0)
            localDataStore.setJoinedChallengeCount(0)
            localDataStore.setFriendCount(0)
            localDataStore.setFeedPostCreateCount(0)
        }

        return shouldShowReview
    }

    fun navigateTo(option: BottomNavOption) {
        bottomNavigation.postValue(Event(option))
    }

    fun getRecentWatchFaces() {

        viewModelScope.launch {

            /*localDataStore.getRandomWatchFaceListResponse()?.let { response ->
                if (!localDataStore.getRandomWatchFaceListSyncTime()
                        .checkTimeDifferenceMoreThanNDays(1)
                ) {
                    _randomWatchFaces.postValue(response)
                    return@launch
                }
            }*/


            deviceRepository.getRecentWatchFace().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        resource.data?.data.let { response ->
                           /* response?.let { watchFaces ->
                                if (watchFaces.isNotEmpty()) {
                                    localDataStore.setRandomWatchFaceListResponse(
                                        watchFaces
                                    )
                                    localDataStore.setRandomWatchFaceListSyncTime(DateFormats.getTimeStamp())
                                }
                            }*/


                            _randomWatchFaces.postValue(response?.let { ArrayList(it).chunked(3) }
                                ?: ArrayList())
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun getShopBanners() {

        viewModelScope.launch {
            userRepository.getDashboardBanner().collect { resource ->
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
                                        getShopBanners()
                                    }

                                    override fun no() {}
                                }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.banners.isNotEmpty()) {
                                displayBanners.postValue(it.banners)
                            }
                            //_contentData.postValue(it.content) //TODO uncomment after feeds release
                        }
                    }
                }
            }
        }

    }

    fun checkUserLocationStatus() {

        viewModelScope.launch(Dispatchers.IO) {
            val userLocation = localDataStore.getUser()?.location
            if (!localDataStore.isUserLocationMapped()) {
                if (userLocation?.stateId == null || userLocation.cityId == null) {
                    fetchLocation.postValue(Event(true))
                } else {
                    localDataStore.setUserLocationMapped(true)
                }
            }


        }

    }

    fun getAddress() {

        try {
            if (lat == null || long == null) {
                return
            }
//28.5921° N, 76.2653
            val addresses = geoCoder.getFromLocation(lat!!, long!!, 10)
            val data = ApplicationUtils.getLocationStringCityState(addresses)
            if (data != null && !isLocationApiCalled) {
                isLocationApiCalled = true
                updateUserLocation(data.first, data.second)
            } else {
                //might me not in the region,, abort
                localDataStore.setUserLocationMapped(true)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun updateUserLocation(city: String, state: String) {
        LOGS.d("Localtionasaaa $state $city")
//        "state_id":1,
//        "city_id":12
        val jsonObject = JsonObject()
        try {
            jsonObject.addProperty("state", state)
            jsonObject.addProperty("city", city)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        viewModelScope.launch {
            friendsRepository.saveUserLocation(jsonObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {

                    }
                    is Resource.Loading -> {

                    }
                    is Resource.NetworkError -> {

                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if (it.isMapped) {
                                updateUserLocationState(it)
                            }
                            localDataStore.setUserLocationMapped(true)
                        }
                    }
                }
            }
        }

    }

    private fun updateUserLocationState(data: UserLocationUpdatedResponse) {
        val user = localDataStore.getUser()
        var userLocation = user?.location
        if (userLocation == null) {
            userLocation = UserLocation()
        }

        userLocation.stateId = data.stateId
        userLocation.state = data.state
        userLocation.city = data.city
        userLocation.cityId = data.cityId
        user?.location = userLocation

        user?.let { localDataStore.saveUserInfo(it) }
    }


}

enum class BottomNavOption {
    HOME, EXPLORE, SHOP, MY_DEVICE, COMMUNITY
}