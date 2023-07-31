package com.noisefit.ui.settings.helpAndSupport

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.HelpAndSupportResponse
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.BuildUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HelpAndSupportViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    private val appRepository: AppRepository
) : BaseViewModel() {

    var highlightTopic: HelpAndSupportType = HelpAndSupportType.NONE

    var deviceN: String? = null
    var osVersion: String? = null
    var helpAndSupportType: HelpAndSupportType? = null

    private val _helpAndSupportResponseList = MutableLiveData<List<HelpAndSupportResponse>>()
    val helpAndSupportResponseList: LiveData<List<HelpAndSupportResponse>>
        get() = _helpAndSupportResponseList


    private val _helpAndSupportResponse = MutableLiveData<HelpAndSupportResponse>()
    val helpAndSupportResponse: LiveData<HelpAndSupportResponse>
        get() = _helpAndSupportResponse


    init {
        deviceN = getDeviceName()
        osVersion = "(Android ${BuildUtils.getDeviceOSVersion()})"
    }


    fun setHelpAndSupportType(type: String) {
        when (type) {
            HelpAndSupportType.PAIRING_AND_CONNECTIVITY.name -> {
                helpAndSupportType = HelpAndSupportType.PAIRING_AND_CONNECTIVITY
            }
            HelpAndSupportType.BATTERY_AND_CHARGING.name -> {
                helpAndSupportType = HelpAndSupportType.BATTERY_AND_CHARGING
            }
            HelpAndSupportType.WATCHFACE_TRANSFER.name -> {
                helpAndSupportType = HelpAndSupportType.WATCHFACE_TRANSFER
            }
        }
    }

    fun setHelpAndSupport(helpAndSupportResponse: HelpAndSupportResponse) {
        _helpAndSupportResponse.postValue(helpAndSupportResponse)
    }

    fun getHelpAndSupport(): HelpAndSupportResponse? {
        return _helpAndSupportResponse.value
    }

    fun getHelpAndSupportResponse(): List<HelpAndSupportResponse> {
        return _helpAndSupportResponseList.value ?: ArrayList()
    }

    fun fetchHelpAndSupportData() {
        viewModelScope.launch {
            appRepository.getHelpAndSupportList()
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
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        fetchHelpAndSupportData()
                                    }

                                    override fun no() {}
                                }
                            })
                        }
                        is Resource.Success -> {
                            resource.data?.data?.let {
                                if (helpAndSupportType != null) {
                                    //filter result and post on selected type
                                    it.forEach { helpAndSupportResponse ->
                                        if (helpAndSupportResponse.type?.equals(
                                                helpAndSupportType?.name,
                                                true
                                            ) == true
                                        ) {
                                            setHelpAndSupport(helpAndSupportResponse)
                                            return@forEach
                                        }
                                    }
                                }
                                _helpAndSupportResponseList.postValue(it)

                            }
                        }
                    }
                }
        }
    }

    private fun getDeviceName(): String {

        val manufacturer: String = BuildUtils.getDeviceManufacturer()
        val model: String = BuildUtils.getDeviceModel()
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else capitalize(manufacturer) + " " + model
    }

    private fun capitalize(str: String): String {
        if (TextUtils.isEmpty(str)) {
            return str
        }
        val arr = str.toCharArray()
        var capitalizeNext = true
        val phrase = StringBuilder()
        for (c in arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(c.uppercaseChar())
                capitalizeNext = false
                continue
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true
            }
            phrase.append(c)
        }
        return phrase.toString()
    }
}