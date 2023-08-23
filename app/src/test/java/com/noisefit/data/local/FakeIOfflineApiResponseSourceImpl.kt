package com.noisefit.data.local

import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.oreo.data.model.OHSModel

class FakeIOfflineApiResponseSourceImpl
constructor() : IOfflineApiResponseStore {

    override fun getHelpAndSupportList(): List<OHSModel>? {
        TODO("Not yet implemented")
    }

    override fun setHelpAndSupportList(resultData: List<OHSModel>?) {
        TODO("Not yet implemented")
    }
}