package com.noisefit.ui.watchface2

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.R
import com.noisefit.data.base.ResourcesProvider
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.model.WatchFace2CategoryModal
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.WatchFaceCategory2
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.utils.InsiderAppEvents
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class Watchface2CategoryViewModel
@Inject
constructor(
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK,
    val sessionManager: SessionManager,
    private val resourcesProvider: ResourcesProvider,
    private val watchFaceRepository: WatchFaceRepository
) : BaseViewModel() {

    var currentPhotoPath: String? = null
    var photoURI: Uri? = null
    var outputUri: Uri? = null
    var isWatchSupportDiy = false
    var minimumBatteryLevel = watchesSDK.getMinimumBatteryLevel()
    private val _categoryList = MutableLiveData<ArrayList<WatchFace2CategoryModal>>()
    val categoryList: LiveData<ArrayList<WatchFace2CategoryModal>> = _categoryList
    val screenType = watchesSDK.getWatchForm()
    var refeshPosition = -1


    init {
        isWatchSupportDiy = getWatchSupportDiy()
    }

    private fun getWatchSupportDiy(): Boolean {
        val sdkWatchType = watchesSDK.getWatchType()
        if (sdkWatchType == SDKWatchType.SDK_ZH ||
            sdkWatchType == SDKWatchType.SDK_NAV_PLUS ||
            sdkWatchType == SDKWatchType.SDK_RYEEX ||
            sdkWatchType == SDKWatchType.SDK_EVOLVE
        ) {
            return true
        }

        return false
    }

    private fun generateCategoryData(response: List<WatchFaceCategory2>) {
        val dataList = ArrayList<WatchFace2CategoryModal>()
        dataList.add(
            WatchFace2CategoryModal.CreateYourOwn(
                screenType,
                watchesSDK.getWatchFaceGif(),
                isWatchSupportDiy
            )
        )
        response.forEach {
            val hasMoreData = if (it.faces.isNullOrEmpty()) {
                false
            } else it.faces!!.size >= it.limit
            dataList.add(
                WatchFace2CategoryModal.CategoryList(
                    screenType,
                    it,
                    1,
                    hasMoreData = hasMoreData
                )
            )
        }
        dataList.add(WatchFace2CategoryModal.HavingAnIssue())
        _categoryList.postValue(dataList)
    }

    fun getWatchfaceCategoriesV2(catId: Int? = null, page: Int? = null) {

        LOGS.d("appendWatchFacesappendWatchFaces $page")
        viewModelScope.launch {
            watchFaceRepository.getWatchfaceCategoriesV2(catId, page)
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
                                        getWatchfaceCategoriesV2(
                                            catId, page
                                        )
                                    }

                                    override fun no() {}
                                }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let { response ->
                                if (catId != null && page != null) {//Append Data
                                    appendWatchFaces(response, catId)
                                } else {
                                    generateCategoryData(response)
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun appendWatchFaces(response: List<WatchFaceCategory2>, catId: Int) {
        val data = _categoryList.value

        val item = data?.firstOrNull {
            (it as? WatchFace2CategoryModal.CategoryList)?.categoryData?.id == catId
        }
        val index = data?.indexOf(item)

        val oldFaces =
            (item as WatchFace2CategoryModal.CategoryList).categoryData.faces as ArrayList

        if (response.isEmpty()) return

        val newFaces = response.first().faces ?: ArrayList()
        oldFaces.addAll(newFaces)

        item.categoryData.faces = oldFaces
        item.currentPage = item.currentPage + 1
        item.hasMoreData = newFaces.size >= response.first().limit

        if (index != null) {
            data.removeAt(index)
            data.add(index, item)
        }

        data.let {
            _categoryList.postValue(it)
        }
    }

    fun loadMoreWatchFaces(data: WatchFace2CategoryModal.CategoryList) {
        getWatchfaceCategoriesV2(data.categoryData.id, data.currentPage + 1)
    }


    fun markFavourite(
        isFavourite: Boolean,
        watchFace: Watchface2,
    ) {
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
                                    markFavourite(isFavourite, watchFace)
                                }

                                override fun no() {
                                    /* if (favMarkedFrom == WF2SubListFrom.Favourite) {
                                         _resetFavouriteStateFavourite.postValue(Event(!isFavourite))
                                     } else {*/
                                    //_resetFavouriteState.postValue(Event(!isFavourite))
                                    //}
                                }
                            }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let { _ ->
                            //if (favMarkedFrom == WF2SubListFrom.Favourite) {
                            //_favouriteMarked.postValue((Event(position)))
                            //}
                            if (isFavourite) {
                                sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_FAVOURITES_ADD)
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_added))
                                //watchFaceRepository.addToFavourites(watchFace)
                            } else {

                                sessionManager.logInsiderAppEvent(InsiderAppEvents.WATCHFACES_FAVOURITES_REMOVE)
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

}