package com.noisefit.data.local.dataStored.abstraction

import com.oreo.data.model.OHSModel


interface IOfflineApiResponseStore {


    fun getHelpAndSupportList(): List<OHSModel>?
    fun setHelpAndSupportList(resultData: List<OHSModel>?)
}