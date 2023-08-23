package com.noisefit.data.local.dataStored.implementation


import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.oreo.data.model.OHSModel
import javax.inject.Inject


private const val HELP_AND_SUPPORT_LIST = "HELP_AND_SUPPORT_LIST"


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class OfflineApiResponseStoreImpl
@Inject
constructor(
    private val gson: Gson,
    private val mPrefs: SharedPreferences
) : IOfflineApiResponseStore {


    override fun getHelpAndSupportList(): List<OHSModel>? {
        return mPrefs.getString(HELP_AND_SUPPORT_LIST, null)
            ?.let { Gson().fromJson<List<OHSModel>>(it) }
    }

    override fun setHelpAndSupportList(resultData: List<OHSModel>?) {
        mPrefs.edit()
            ?.putString(HELP_AND_SUPPORT_LIST, gson.toJson(resultData))
            ?.apply()
    }
}