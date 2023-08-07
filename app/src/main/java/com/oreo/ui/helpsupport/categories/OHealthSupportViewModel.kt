package com.oreo.ui.helpsupport.categories

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.BuildUtils
import com.oreo.data.model.OHSModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OHealthSupportViewModel @Inject constructor(
    val userActivityRepository: OreoUserActivityRepository
) : BaseViewModel() {
    var deviceN: String? = null
    var osVersion: String? = null
    private val _hsCategoriesData = MutableLiveData<List<OHSModel>>()
    val hsCategoriesData: LiveData<List<OHSModel>> = _hsCategoriesData


    init {
        deviceN = getDeviceName()
        osVersion = "(Android ${BuildUtils.getDeviceOSVersion()})"
    }

    fun getHSCategories() {
        viewModelScope.launch {
            userActivityRepository.getHSCategories(
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
                            this.uiComponentType as UIComponentType.RetryApiDialog
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        getHSCategories()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _hsCategoriesData.postValue(it)
                        }
                    }
                }
            }
        }
    }

    fun getDummyHelpData(): ArrayList<OHSModel> {
        val testData = ArrayList<OHSModel>()
        testData.add(OHSModel(id = "1", imgUrl = "", title = "Getting started"))
        testData.add(OHSModel(id = "1", imgUrl = "", title = "Care for your Luna Ring"))
        testData.add(OHSModel(id = "1", imgUrl = "", title = "Product safety and use"))
        return testData

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