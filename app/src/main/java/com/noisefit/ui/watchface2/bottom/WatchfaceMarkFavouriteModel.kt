package com.noisefit.ui.watchface2.bottom

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.luna.R
import com.noisefit.data.base.ResourcesProvider
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.data.repository.abstraction.WatchFaceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WatchfaceMarkFavouriteModel
@Inject constructor(
    private val watchFaceRepository: WatchFaceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var rating: Int = -1
//    val watchFaceRated = MutableLiveData<Event<Boolean>>()

    var fromFav = false
    var watchface: Watchface2? = null
    val favMarked = MutableLiveData<Event<Boolean>>()

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

                                override fun no() {/* if (favMarkedFrom == WF2SubListFrom.Favourite) {
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
                            favMarked.postValue(Event(true))
                            if (isFavourite) {
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_added))
                            } else {
                                sendMessage(resourcesProvider.getString(R.string.text_watchface_removed))
                            }
                        }
                    }
                }
            }
        }
    }

    fun rateWatchFace(
        rating: Int,
        watchFace: Watchface2
    ) {
        GlobalScope.launch(Dispatchers.IO) {

            val requestObject = JsonObject().apply {
                addProperty("watchface_id", watchFace.wId)
                addProperty("rating", rating)
            }
            watchFaceRepository.rateWatchFace(requestObject).collect { resource ->
                when (resource) {
                    is Resource.GenericError -> {
                        sendMessage(resource.message)
                    }

                    is Resource.Loading -> {
                        setLoading(resource.loading)
                    }

                    is Resource.NetworkError -> {
//                        setApiErrors(resource.response.apply {
//                            this.uiComponentType as UIComponentType.RetryApiDialog
//                            this.uiComponentType.callback = object : BinaryActionCallback {
//                                override fun yes() {
//                                    rateWatchFace(rating, watchFace)
//                                }
//
//                                override fun no() {/* if (favMarkedFrom == WF2SubListFrom.Favourite) {
//                                         _resetFavouriteStateFavourite.postValue(Event(!isFavourite))
//                                     } else {*/
//                                    //_resetFavouriteState.postValue(Event(!isFavourite))
//                                    //}
//                                }
//                            }
//                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            localDataStore.setWatchFaceRatedId(watchFace.wId)
//                            watchFaceRated.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }



}