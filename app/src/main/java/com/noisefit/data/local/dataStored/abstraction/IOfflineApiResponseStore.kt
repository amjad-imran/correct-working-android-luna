package com.noisefit.data.local.dataStored.abstraction

import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.response.SleepHistoryResponse
import com.oreo.data.model.OHSModel


interface IOfflineApiResponseStore {



    fun getHelpAndSupportList(): List<OHSModel>?
    fun setHelpAndSupportList(resultData: List<OHSModel>?)
}