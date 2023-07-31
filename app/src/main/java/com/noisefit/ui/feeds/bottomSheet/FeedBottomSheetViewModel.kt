package com.noisefit.ui.feeds.bottomSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.data.model.ReportAbuseData
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.FeedRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FeedBottomSheetViewModel
@Inject
constructor(
    private val feedRepository: FeedRepository,
    val localDataStore: DataStoredInterface,
    val sessionManager: SessionManager
) : BaseViewModel() {
    var isOpenFromComment = false
    private val _serverStatus = MutableLiveData<Boolean>()
    val serverStatus: LiveData<Boolean>
        get() = _serverStatus

    var commentCount: Int = 0

    private val _reportAbuseList = MutableLiveData<List<ReportAbuseData>>()
    val reportAbuseList: LiveData<List<ReportAbuseData>>
        get() = _reportAbuseList


    var postId: Long? = null
    var commentId: Long? = null
    var comeFrom: String? = null

    fun deletePOrCFromServer() {

        viewModelScope.launch {
            feedRepository.deletePOrC(postId!!, commentId).collect { resource ->
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
                                    deletePOrCFromServer()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            if(commentId!=null){
                                commentCount = it.commentsCount
                            }
                            _serverStatus.postValue(true)

                        }
                    }
                }
            }
        }

    }


    fun reportAbusePOrC(reportComment: String) {

        val jsonObject = JsonObject().apply {

            addProperty("report_comment", reportComment)
            if (commentId != -1L) {
                addProperty("comment_id", commentId)
                addProperty("type", "comment")
            } else {
                addProperty("post_id", postId)
                addProperty("type", "post")
            }
        }
        viewModelScope.launch {
            feedRepository.reportFeed(jsonObject).collect { resource ->
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
                                    deletePOrCFromServer()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {

                            _serverStatus.postValue(true)

                        }
                    }
                }
            }
        }

    }

    fun getReportAbuseList() {

        viewModelScope.launch {
            feedRepository.getReportedList().collect { resource ->
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
                                    getReportAbuseList()
                                }

                                override fun no() {}
                            }
                        })
                    }
                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _reportAbuseList.postValue(it)

                        }
                    }
                }
            }
        }

    }
}