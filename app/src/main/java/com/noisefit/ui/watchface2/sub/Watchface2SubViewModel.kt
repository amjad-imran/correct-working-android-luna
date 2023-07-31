package com.noisefit.ui.watchface2.sub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit.data.base.ResourcesProvider
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.DiyMyCreation
import com.noisefit.data.remote.response.WatchFaceCategory2
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.util.AnalyticEventUtils
import com.noisefit_commans.utils.Event
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WF2SubListFrom() {
    Newly,
    Popular,
    Trending,
    MyCreation,
    CATEGORIES,
    Favourite,
    None
}

@HiltViewModel
class Watchface2SubViewModel
@Inject
constructor(
    private val watchFaceRepository: WatchFaceRepository,
    val localDataStore: DataStoredInterface,
    val rewardsRepository: RewardsRepository,
    private val resourcesProvider: ResourcesProvider,
    var sessionManager: SessionManager,
    val watchesSDK: WatchesSDK,
    var watchDataStore: WatchDataStore,
    val analyticEventUtils: AnalyticEventUtils
) : BaseViewModel() {

    var refreshPosition: Int = -1
    var temporaryDiyMyCreation: DiyMyCreation? = null
    var subListFrom: WF2SubListFrom = WF2SubListFrom.Popular
    var selectedCategoryId: Int? = null

    var minimumBatteryLevel = watchesSDK.getMinimumBatteryLevel()
    private val _editMode = MutableLiveData<Boolean>(false)
    val screenType = watchesSDK.getWatchForm()
    private val _categoryList = MutableLiveData<List<WatchFaceCategory2>>()
    private val _subWfList = MutableLiveData<List<Watchface2>>()
    private val _favouriteWatchFaceList = MutableLiveData<List<WatchFace>>()
    private val _resetFavouriteState = MutableLiveData<Event<Boolean>>()
    private val _resetFavouriteStateFavourite = MutableLiveData<Event<Boolean>>()
    private val _deleteDiyCreationSuccess = MutableLiveData<Event<Boolean>>()
    private val _diyMyCreationList = MutableLiveData<List<DiyMyCreation>>()
    private val _openEditDiyScreen = MutableLiveData<Event<Boolean>>()


    var categoryId: Int? = null


    val categoryList: LiveData<List<WatchFaceCategory2>> = _categoryList
    val subWfList: LiveData<List<Watchface2>> = _subWfList
    val favouriteWatchFaceList: LiveData<List<WatchFace>> = _favouriteWatchFaceList
    val deleteDiyCreationSuccess = _deleteDiyCreationSuccess
    val resetFavouriteStateFavourite: LiveData<Event<Boolean>> = _resetFavouriteStateFavourite
    val diyMyCreationList = _diyMyCreationList
    val openEditDiyScreen = _openEditDiyScreen
    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }


    private val _favouriteMarked = MutableLiveData<Event<Int>>()
    val favouriteMarked: LiveData<Event<Int>> = _favouriteMarked

    private val _watchFace = MutableLiveData<WatchFace>()
    val watchFace: LiveData<WatchFace> = _watchFace


    fun getWatchFaceCategories(forceRefresh: Boolean) {

        viewModelScope.launch {
            watchFaceRepository.getWatchfaceCategoriesOnlyV2().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getWatchFaceCategories(forceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            //response.lastSync = System.currentTimeMillis()
                            //pushCategoryIdData(response)
                            _categoryList.postValue(response)
                            selectedCategoryId?.let { getWatchFaceList(false, it) }
                        }
                    }
                }
            }
        }
    }

    fun getWatchFaceList(forceRefresh: Boolean, catId: Int? = null) {

        viewModelScope.launch {
            watchFaceRepository.getWatchFace2CustomData(forceRefresh, subListFrom, catId)
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
                                this.uiComponentType as UIComponentType.RetryApiDialog
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        getWatchFaceList(forceRefresh, catId)
                                    }

                                    override fun no() {}
                                }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let { response ->
                                _subWfList.postValue(response)
                            }
                        }
                    }
                }
        }
    }

    fun getBackgroundImageHeight(): Int {
        val device = localDataStore.getConnectedDevice() ?: return 240
        return if (device.deviceType.equals(DeviceType.COLORFIT_PRO_3.deviceType, true)) {
            360//PRO3
        } else {
            240
        }

    }

    fun getBackgroundImageWidth(): Int {
        val device = localDataStore.getConnectedDevice() ?: return 240
        return if (device.deviceType.equals(DeviceType.COLORFIT_PRO_3.deviceType, true)) {
            return 320//PRO3
        } else {
            240
        }
    }

    fun setWatchFace(watchFace: WatchFace) {
        _watchFace.value = watchFace
    }

    //TODO Not working, test
    fun deleteTempFile() {
        /* if (localFilePath == null) return

         try {
             val cacheFile = File(Uri.parse(localFilePath).toString())
             cacheFile.deleteRecursively()
         } catch (exp: Exception) {
             LOGS.d("Delete Failed")
         }*/
    }


    fun clearCustomData() {
        LOGS.d("clearCustomData called")
        _subWfList.value = ArrayList()

    }

    fun isUserLogined(): Boolean {
        return localDataStore.getUser() != null
    }

    fun markFavouriteLocally(isFavourite: Boolean, watchFaceId: Int) {

        /**
         * Update Data in main Watchface listing, newly, top, design wise, recent
         */
        val customData = localDataStore.getCustomWatchFaceData()
        customData?.let { data ->
            data.newlyAdded?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.is_favourite = if (isFavourite) "1" else "0"
                        }
                    }
                }
            }

            data.topDownloaded?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.is_favourite = if (isFavourite) "1" else "0"
                        }
                    }
                }
            }

            data.recentlyUsed?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.is_favourite = if (isFavourite) "1" else "0"
                        }
                    }
                }
            }

            data.designWise?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.is_favourite = if (isFavourite) "1" else "0"
                        }
                    }
                }
            }
            localDataStore.setCustomWatchFaceData(data)
            //  postCustomWatchFaceData(data)
        }

        /**
         * Update Data in Wathcface Categories listing
         */
        localDataStore.getWatchFaceCategories()?.let { categories ->
            var isDataChanged = false
            categories.forEach {
                it.faces.forEach { watchFace ->
                    if (watchFace.id == watchFaceId) {
                        watchFace.is_favourite = if (isFavourite) "1" else "0"
                        isDataChanged = true
                    }
                }
            }
            if (isDataChanged) {
                localDataStore.setWatchFaceCategories(categories)
            }
        }
    }


    var currentPosition = -1


    fun markFavourite(
        isFavourite: Boolean,
        watchFace: Watchface2,
        position: Int
    ) {
        currentPosition = position
        viewModelScope.launch(Dispatchers.IO) {

            val requestObject = JsonObject().apply {
                addProperty("watchface_id", watchFace.wId)
                addProperty("is_favourite", isFavourite)
            }
            watchFaceRepository.markAsFavourite(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    markFavourite(isFavourite, watchFace, position)
                                }

                                override fun no() {
                                    /* if (favMarkedFrom == WF2SubListFrom.Favourite) {
                                         _resetFavouriteStateFavourite.postValue(Event(!isFavourite))
                                     } else {*/
                                    _resetFavouriteState.postValue(Event(!isFavourite))
                                    //}
                                }
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { _ ->
                            //if (favMarkedFrom == WF2SubListFrom.Favourite) {
                            _favouriteMarked.postValue((Event(position)))
                            //}
                            if (isFavourite) {
                                sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_CATEGORY_MYFAVOURITES)
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_added))
                                //watchFaceRepository.addToFavourites(watchFace)
                            } else {
                                sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_CATEGORY_MYFAVOURITES)
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_removed))
                                //watchFaceRepository.removeFromFavourites(watchFace.id ?: -1)
                            }

                            /*withContext(Dispatchers.Main) {
                                markFavouriteLocally(isFavourite, watchFace.id ?: -1)
                            }*/
                        }
                    }
                }
            }
        }
    }

    fun getFavouriteWatchFaces(isForceRefresh: Boolean) {
        viewModelScope.launch {

            watchFaceRepository.getFavouriteWatchFaces(isForceRefresh).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getFavouriteWatchFaces(isForceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _favouriteWatchFaceList.postValue(response)
                        }
                    }
                }
            }
        }
    }


    fun getDiyMyCreationWatchFaces(isForceRefresh: Boolean) {
        viewModelScope.launch {

            watchFaceRepository.getDiyWatchFaceOnlineList().collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getFavouriteWatchFaces(isForceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _diyMyCreationList.postValue(response)
                        }
                    }
                }
            }
        }
    }

    fun deleteDiyMyCreationWatchFaces(watchFaceId: Int) {
        viewModelScope.launch {

            watchFaceRepository.deleteDiyWatchFaceOnline(watchFaceId).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    deleteDiyMyCreationWatchFaces(watchFaceId)
                                }

                                override fun no() {}
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _deleteDiyCreationSuccess.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

    fun removeFavouriteItem(position: Int) {
        if ((_favouriteWatchFaceList.value?.size ?: 0) > position) {
            val data = (_favouriteWatchFaceList.value as ArrayList)
            data.removeAt(position)
            _favouriteWatchFaceList.postValue(data)
        }
    }

    fun earnRewardsPoints() {
        if (localDataStore.getIsWatchFaceRewardEarned()) return

        viewModelScope.launch {
            val request = JsonObject().apply {
                this.addProperty("task_enum", "1st_custom_watch-face")
            }
            rewardsRepository.earnRewardsPoints(request).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        localDataStore.setIsWatchFaceRewardEarned(true)
                    }

                    else -> {}
                }
            }
        }
    }

}
