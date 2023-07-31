package com.noisefit.data.local.dataStored.abstraction

import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.model.history.*
import com.noisefit_commans.response.SleepHistoryResponse


interface IOfflineApiResponseStore {

    fun getGraphLocalSleepData(historyType: String): SleepHistoryResponse?
    fun setGraphLocalSleepData(resultData: SleepHistoryResponse?, historyType: String)

    fun setGraphLocalSleepHighlightData(resultData: SleepHighlightResponse?)
    fun getGraphLocalSleepHighlightData(): SleepHighlightResponse?

    fun getGraphLocalBodyTempData(historyType: String): BodyTempHistoryResponse?
    fun setGraphLocalBodyTempData(resultData: BodyTempHistoryResponse?, historyType: String)

    fun getGraphLocalHRData(historyType: String): HrHistoryResponse?
    fun setGraphLocalHRData(resultData: HrHistoryResponse?, historyType: String)

    fun getGraphLocalBOData(historyType: String): BoHistoryResponse?
    fun setGraphLocalBOData(resultData: BoHistoryResponse?, historyType: String)

    fun getGraphLocalStressData(historyType: String): StressHistoryResponse?
    fun setGraphLocalStressData(resultData: StressHistoryResponse?, historyType: String)

    fun getGraphLocalStepsData(historyType: String): StepsHistoryResponse?
    fun setGraphLocalStepsData(resultData: StepsHistoryResponse?, historyType: String)

    fun getGraphLocalStepsHighlightData(): GraphHighlightResponse?
    fun setGraphLocalStepsHighlightData(resultData: GraphHighlightResponse?)

    fun getChallengeList(): com.noisefit_commans.data.response.ChallengeListResponse?
    fun setChallengeList(resultData: com.noisefit_commans.data.response.ChallengeListResponse?)

    fun getCurrentChallenges(): com.noisefit_commans.data.response.ChallengeListingResponse?
    fun setCurrentChallenges(resultData: com.noisefit_commans.data.response.ChallengeListingResponse?)

    fun getCompletedChallenges(): com.noisefit_commans.data.response.ChallengeListingResponse?
    fun setCompletedChallenges(resultData: com.noisefit_commans.data.response.ChallengeListingResponse?)

    fun getHelpAndSupportList(): List<HelpAndSupportResponse>?
    fun setHelpAndSupportList(resultData: List<HelpAndSupportResponse>?)
}