package com.noisefit.data.local.dataStored.implementation


import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.response.SleepHistoryResponse
import javax.inject.Inject

private const val GRAPH_STEPS = "GRAPH_STEPS"
private const val GRAPH_STRESS = "GRAPH_STRESS"
private const val GRAPH_BO = "GRAPH_BO"
private const val GRAPH_HR = "GRAPH_HR"
private const val GRAPH_SLEEP = "GRAPH_SLEEP"
private const val GRAPH_SLEEP_HIGHLIGHTS = "GRAPH_SLEEP_HIGHLIGHTS"
private const val GRAPH_TEMP = "GRAPH_TEMP"
private const val GRAPH_STEPS_HIGHLIGHTS = "GRAPH_STEPS_HIGHLIGHTS"
private const val CHALLENGE_LIST = "CHALLENGE_LIST"
private const val CHALLENGE_LIST_CURRENT = "CHALLENGE_LIST_CURRENT"
private const val CHALLENGE_LIST_COMPLETED = "CHALLENGE_LIST_COMPLETED"

private const val HELP_AND_SUPPORT_LIST = "HELP_AND_SUPPORT_LIST"


private inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

class OfflineApiResponseStoreImpl
@Inject
constructor(
    private val gson: Gson,
    private val mPrefs: SharedPreferences
) : IOfflineApiResponseStore {

    override fun setGraphLocalSleepHighlightData(resultData: SleepHighlightResponse?) {
        mPrefs.edit()
            ?.putString(GRAPH_SLEEP_HIGHLIGHTS, gson.toJson(resultData))
            ?.apply()
    }

    override fun getGraphLocalSleepHighlightData(): SleepHighlightResponse? {
        return mPrefs.getString(GRAPH_SLEEP_HIGHLIGHTS, null)
            ?.let { Gson().fromJson<SleepHighlightResponse>(it) }
    }

    override fun getGraphLocalSleepData(historyType: String): SleepHistoryResponse? {
        val key = GRAPH_SLEEP + "_$historyType"
        return mPrefs.getString(key, null)
            ?.let { Gson().fromJson<SleepHistoryResponse>(it) }
    }

    override fun setGraphLocalSleepData(resultData: SleepHistoryResponse?, historyType: String) {
        val key = GRAPH_SLEEP + "_$historyType"
        mPrefs.edit()
            ?.putString(key, gson.toJson(resultData))
            ?.apply()
    }

    override fun getGraphLocalBodyTempData(historyType: String): BodyTempHistoryResponse? {
        val key = GRAPH_TEMP + "_$historyType"
        return mPrefs.getString(key, null)
            ?.let { Gson().fromJson<BodyTempHistoryResponse>(it) }
    }

    override fun setGraphLocalBodyTempData(
        resultData: BodyTempHistoryResponse?,
        historyType: String
    ) {
        val key = GRAPH_TEMP + "_$historyType"
        mPrefs.edit()
            ?.putString(key, gson.toJson(resultData))
            ?.apply()
    }

    override fun getGraphLocalHRData(historyType: String): HrHistoryResponse? {
        val key = GRAPH_HR + "_$historyType"
        return mPrefs.getString(key, null)
            ?.let { Gson().fromJson<HrHistoryResponse>(it) }
    }

    override fun setGraphLocalHRData(resultData: HrHistoryResponse?, historyType: String) {
        val key = GRAPH_HR + "_$historyType"
        mPrefs.edit()
            ?.putString(key, gson.toJson(resultData))
            ?.apply()
    }


    override fun getGraphLocalBOData(historyType: String): BoHistoryResponse? {
        val key = GRAPH_BO + "_$historyType"
        return mPrefs.getString(key, null)
            ?.let { Gson().fromJson<BoHistoryResponse>(it) }
    }

    override fun setGraphLocalBOData(resultData: BoHistoryResponse?, historyType: String) {
        val key = GRAPH_BO + "_$historyType"
        mPrefs.edit()
            ?.putString(key, gson.toJson(resultData))
            ?.apply()
    }

    override fun getGraphLocalStressData(historyType: String): StressHistoryResponse? {
        val key = GRAPH_STRESS + "_$historyType"
        return mPrefs.getString(key, null)
            ?.let { Gson().fromJson<StressHistoryResponse>(it) }
    }

    override fun setGraphLocalStressData(resultData: StressHistoryResponse?, historyType: String) {
        val key = GRAPH_STRESS + "_$historyType"
        mPrefs.edit()
            ?.putString(key, gson.toJson(resultData))
            ?.apply()
    }

    override fun getGraphLocalStepsData(
        historyType: String
    ): StepsHistoryResponse? {
        val key = GRAPH_STEPS + "_$historyType"
        return mPrefs.getString(key, null)
            ?.let { Gson().fromJson<StepsHistoryResponse>(it) }
    }

    override fun setGraphLocalStepsData(resultData: StepsHistoryResponse?, historyType: String) {
        val key = GRAPH_STEPS + "_$historyType"
        mPrefs.edit()
            ?.putString(key, gson.toJson(resultData))
            ?.apply()
    }

    override fun getGraphLocalStepsHighlightData(): GraphHighlightResponse? {
        return mPrefs.getString(GRAPH_STEPS_HIGHLIGHTS, null)
            ?.let { Gson().fromJson<GraphHighlightResponse>(it) }
    }

    override fun setGraphLocalStepsHighlightData(resultData: GraphHighlightResponse?) {
        mPrefs.edit()
            ?.putString(GRAPH_STEPS_HIGHLIGHTS, gson.toJson(resultData))
            ?.apply()
    }

    override fun getChallengeList(): com.noisefit_commans.data.response.ChallengeListResponse? {
        return mPrefs.getString(CHALLENGE_LIST, null)
            ?.let { Gson().fromJson<com.noisefit_commans.data.response.ChallengeListResponse>(it) }
    }

    override fun setChallengeList(resultData: com.noisefit_commans.data.response.ChallengeListResponse?) {
        mPrefs.edit()
            ?.putString(CHALLENGE_LIST, gson.toJson(resultData))
            ?.apply()
    }

    override fun getCurrentChallenges(): com.noisefit_commans.data.response.ChallengeListingResponse? {
        return mPrefs.getString(CHALLENGE_LIST_CURRENT, null)
            ?.let { Gson().fromJson<com.noisefit_commans.data.response.ChallengeListingResponse>(it) }
    }

    override fun setCurrentChallenges(resultData: com.noisefit_commans.data.response.ChallengeListingResponse?) {
        mPrefs.edit()
            ?.putString(CHALLENGE_LIST_CURRENT, gson.toJson(resultData))
            ?.apply()
    }

    override fun getCompletedChallenges(): com.noisefit_commans.data.response.ChallengeListingResponse? {
        return mPrefs.getString(CHALLENGE_LIST_COMPLETED, null)
            ?.let { Gson().fromJson<com.noisefit_commans.data.response.ChallengeListingResponse>(it) }
    }

    override fun setCompletedChallenges(resultData: com.noisefit_commans.data.response.ChallengeListingResponse?) {
        mPrefs.edit()
            ?.putString(CHALLENGE_LIST_COMPLETED, gson.toJson(resultData))
            ?.apply()
    }

    override fun getHelpAndSupportList(): List<HelpAndSupportResponse>? {
        return mPrefs.getString(HELP_AND_SUPPORT_LIST, null)
            ?.let { Gson().fromJson<List<HelpAndSupportResponse>>(it) }
    }

    override fun setHelpAndSupportList(resultData: List<HelpAndSupportResponse>?) {
        mPrefs.edit()
            ?.putString(HELP_AND_SUPPORT_LIST, gson.toJson(resultData))
            ?.apply()
    }
}