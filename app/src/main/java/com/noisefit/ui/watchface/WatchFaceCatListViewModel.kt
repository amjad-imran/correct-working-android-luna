package com.noisefit.ui.watchface

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit.data.base.ResourcesProvider
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.local.db.OfflineResult
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.CatWiseWatchFacesItem
import com.noisefit.data.remote.response.WatchFaceCustomListResponse
import com.noisefit.data.repository.abstraction.RewardsRepository
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.util.AnalyticEventUtils
import com.noisefit_commans.utils.Event
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.WatchFace
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WatchFaceCatListViewModel @Inject constructor(
    private val watchFaceRepository: WatchFaceRepository,
    val localDataStore: DataStoredInterface,
    val rewardsRepository: RewardsRepository,
    private val resourcesProvider: ResourcesProvider,
    var sessionManager: SessionManager,
    val watchesSDK: WatchesSDK,
    var watchDataStore: WatchDataStore,
    val analyticEventUtils: AnalyticEventUtils
) : BaseViewModel() {

    var masterPosition: Int = -1
    private val _categoryList = MutableLiveData<List<CatWiseWatchFacesItem>>()
    private val _recentList = MutableLiveData<List<CatWiseWatchFacesItem>>()
    private val _newlyAddedList = MutableLiveData<List<CatWiseWatchFacesItem>>()
    private val _topDownloadedList = MutableLiveData<List<CatWiseWatchFacesItem>>()
    private val _randomCategoryList = MutableLiveData<List<CatWiseWatchFacesItem>>()
    private val _favouriteWatchFaceList = MutableLiveData<List<WatchFace>>()
    private val _resetFavouriteState = MutableLiveData<Event<Boolean>>()
    private val _resetFavouriteStateFavourite = MutableLiveData<Event<Boolean>>()

    var categoryId: Int? = null

    val categoryList: LiveData<List<CatWiseWatchFacesItem>> = _categoryList
    val recentList: LiveData<List<CatWiseWatchFacesItem>> = _recentList
    val newlyAddedList: LiveData<List<CatWiseWatchFacesItem>> = _newlyAddedList
    val topDownloadedList: LiveData<List<CatWiseWatchFacesItem>> = _topDownloadedList
    val randomCategoryList: LiveData<List<CatWiseWatchFacesItem>> = _randomCategoryList
    val favouriteWatchFaceList: LiveData<List<WatchFace>> = _favouriteWatchFaceList
    val resetFavouriteState: LiveData<Event<Boolean>> = _resetFavouriteState
    val resetFavouriteStateFavourite: LiveData<Event<Boolean>> = _resetFavouriteStateFavourite

    private val _categoryWatchFacesList = MutableLiveData<List<WatchFace>>()
    val categoryWatchFacesList: LiveData<List<WatchFace>> = _categoryWatchFacesList

    private val _favouriteMarked = MutableLiveData<Event<Int>>()
    val favouriteMarked: LiveData<Event<Int>> = _favouriteMarked

    private val _watchFace = MutableLiveData<WatchFace>()
    val watchFace: LiveData<WatchFace> = _watchFace

    fun getWatchFaceCategories(forceRefresh: Boolean) {

        viewModelScope.launch {
            watchFaceRepository.getWatchFaceCategory(forceRefresh).collect { resource ->
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
                        }
                    }
                }
            }
        }
    }

    fun getWatchFaceCustomData(forceRefresh: Boolean) {

        viewModelScope.launch {
            watchFaceRepository.getWatchFaceCustomData(forceRefresh).collect { resource ->
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
                                    getWatchFaceCustomData(forceRefresh)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            postCustomWatchFaceData(response)
                        }
                    }
                }
            }
        }
    }

    private fun postCustomWatchFaceData(response: WatchFaceCustomListResponse) {

        _newlyAddedList.postValue(
            arrayListOf(
                CatWiseWatchFacesItem(
                    name = "Newly Added",
                    id = -2,
                    faces = response.newlyAdded ?: arrayListOf()
                )
            )
        )
        _topDownloadedList.postValue(
            arrayListOf(
                CatWiseWatchFacesItem(
                    name = "Top Downloaded",
                    id = -3,
                    faces = response.topDownloaded ?: arrayListOf()
                )
            )
        )
        _randomCategoryList.postValue(
            arrayListOf(
                CatWiseWatchFacesItem(
                    name = "Design Wise",
                    id = -4,
                    faces = response.designWise ?: arrayListOf()
                )
            )
        )
        val recentCategory = CatWiseWatchFacesItem(
            name = "Recently used",
            id = -1,
            faces = response.recentlyUsed ?: arrayListOf()
        )
        _recentList.postValue(arrayListOf(recentCategory))
    }

    fun getWatchFaceByCategory(categoryId: Int) {

        viewModelScope.launch {
            watchFaceRepository.getWatchFacesByCategoryId(categoryId).collect { resource ->
                when (resource) {
                    is OfflineResult.GenericError -> {
                        sendMessage(resource.errorMessage)
                    }
                    is OfflineResult.Loading -> {
                        setLoading(resource.loading)
                    }
                    is OfflineResult.NetworkError -> {
                        setApiErrors(resource.response.apply {
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    getWatchFaceByCategory(categoryId)
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is OfflineResult.Success -> {

                        resource.value?.let { response ->
                            _categoryWatchFacesList.postValue(response)
                        }
                    }
                }
            }
        }


        /* val result = _categoryList.value!!.find { it.id == categoryId } ?: return ArrayList()
         selectedCategoryName = result.name
         return result.faces*/
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


    fun clearWatchfaceListingDate() {
        LOGS.d("clearWatchfaceListingDate called")
        _categoryWatchFacesList.value = ArrayList()
    }

    fun clearCustomData() {
        LOGS.d("clearCustomData called")
        _newlyAddedList.value = ArrayList()
        _topDownloadedList.value = ArrayList()
        _randomCategoryList.value = ArrayList()
        _recentList.value = ArrayList()
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
            postCustomWatchFaceData(data)
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

    fun updateDownloadCountLocally(downloads: String?, watchFaceId: Int?) {

        if (watchFaceId == null) return
        if (downloads == null) return

        /**
         * Update Data in main Watchface listing, newly, top, design wise, recent
         */
        val customData = localDataStore.getCustomWatchFaceData()
        customData?.let { data ->
            data.newlyAdded?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.downloads = downloads
                        }
                    }
                }
            }

            data.topDownloaded?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.downloads = downloads
                        }
                    }
                }
            }

            data.recentlyUsed?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.downloads = downloads
                        }
                    }
                }
            }

            data.designWise?.let { faces ->
                if (faces.isNotEmpty()) {
                    faces.forEach { watchFace ->
                        if (watchFace.id == watchFaceId) {
                            watchFace.downloads = downloads
                        }
                    }
                }
            }
            localDataStore.setCustomWatchFaceData(data)
            postCustomWatchFaceData(data)
        }

        /**
         * Update Data in Wathcface Categories listing
         */
        localDataStore.getWatchFaceCategories()?.let { categories ->
            var isDataChanged = false
            categories.forEach {
                it.faces.forEach { watchFace ->
                    if (watchFace.id == watchFaceId) {
                        watchFace.downloads = downloads
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
    var favMarkedFrom: FavMarkFrom? = null

    fun markFavourite(
        isFavourite: Boolean,
        watchFace: WatchFace,
        position: Int,
        favMarkedFrom: FavMarkFrom
    ) {
        currentPosition = position
        this.favMarkedFrom = favMarkedFrom
        viewModelScope.launch(Dispatchers.IO) {

            val requestObject = JsonObject().apply {
                addProperty("watchface_id", watchFace.id)
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                override fun yes() {
                                    markFavourite(isFavourite, watchFace, position, favMarkedFrom)
                                }

                                override fun no() {
                                    if (favMarkedFrom == FavMarkFrom.MY_FAV) {
                                        _resetFavouriteStateFavourite.postValue(Event(!isFavourite))
                                    } else {
                                        _resetFavouriteState.postValue(Event(!isFavourite))
                                    }
                                }
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            if (favMarkedFrom == FavMarkFrom.MY_FAV) {
                                _favouriteMarked.postValue((Event(position)))
                            }
                            if (isFavourite) {
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_added))
                                watchFaceRepository.addToFavourites(watchFace)
                            } else {
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_removed))
                                watchFaceRepository.removeFromFavourites(watchFace.id ?: -1)
                            }

                            withContext(Dispatchers.Main) {
                                markFavouriteLocally(isFavourite, watchFace.id ?: -1)
                            }
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

    fun getNewlyAddedWatchFaces() {
        viewModelScope.launch {

            watchFaceRepository.getNewlyAddedWatchFaces().collect { resource ->
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
                                    getNewlyAddedWatchFaces()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _categoryWatchFacesList.postValue(response)
                        }
                    }
                }
            }
        }

    }

    fun getTopDownloadedWatchFaces() {
        viewModelScope.launch {

            watchFaceRepository.getTopDownloadedWatchFaces().collect { resource ->
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
                                    getTopDownloadedWatchFaces()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let { response ->
                            _categoryWatchFacesList.postValue(response)
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

enum class FavMarkFrom {
    MY_FAV, CATEGORY, RECENT, NEWLY_ADDED, TOP_DOWNLOADED, CATEGORY_RANDOM, CATEGORIES_LISTING
}