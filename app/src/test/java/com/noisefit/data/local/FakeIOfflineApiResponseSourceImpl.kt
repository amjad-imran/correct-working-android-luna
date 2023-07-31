package com.noisefit.data.local

import com.noisefit.data.local.dataStored.abstraction.IOfflineApiResponseStore
import com.noisefit_commans.data.response.*
import com.noisefit_commans.data.response.history.*
import com.noisefit_commans.data.model.history.*

class FakeIOfflineApiResponseSourceImpl
constructor() : IOfflineApiResponseStore {
    override fun getGraphLocalSleepData(historyType: String): SleepHistoryResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalSleepData(resultData: SleepHistoryResponse?, historyType: String) {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalSleepHighlightData(resultData: SleepHighlightResponse?) {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalSleepHighlightData(): SleepHighlightResponse? {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalBodyTempData(historyType: String): BodyTempHistoryResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalBodyTempData(
        resultData: BodyTempHistoryResponse?,
        historyType: String
    ) {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalHRData(historyType: String): HrHistoryResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalHRData(resultData: HrHistoryResponse?, historyType: String) {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalBOData(historyType: String): BoHistoryResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalBOData(resultData: BoHistoryResponse?, historyType: String) {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalStressData(historyType: String): StressHistoryResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalStressData(resultData: StressHistoryResponse?, historyType: String) {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalStepsData(historyType: String): StepsHistoryResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalStepsData(resultData: StepsHistoryResponse?, historyType: String) {
        TODO("Not yet implemented")
    }

    override fun getGraphLocalStepsHighlightData(): GraphHighlightResponse? {
        TODO("Not yet implemented")
    }

    override fun setGraphLocalStepsHighlightData(resultData: GraphHighlightResponse?) {
        TODO("Not yet implemented")
    }

    override fun getChallengeList(): com.noisefit_commans.data.response.ChallengeListResponse? {
        TODO("Not yet implemented")
    }

    override fun setChallengeList(resultData: com.noisefit_commans.data.response.ChallengeListResponse?) {
        TODO("Not yet implemented")
    }

    override fun getCurrentChallenges(): com.noisefit_commans.data.response.ChallengeListingResponse? {
        TODO("Not yet implemented")
    }

    override fun setCurrentChallenges(resultData: com.noisefit_commans.data.response.ChallengeListingResponse?) {
        TODO("Not yet implemented")
    }

    override fun getCompletedChallenges(): com.noisefit_commans.data.response.ChallengeListingResponse? {
        TODO("Not yet implemented")
    }

    override fun setCompletedChallenges(resultData: com.noisefit_commans.data.response.ChallengeListingResponse?) {
        TODO("Not yet implemented")
    }

    override fun getHelpAndSupportList(): List<HelpAndSupportResponse>? {
        TODO("Not yet implemented")
    }

    override fun setHelpAndSupportList(resultData: List<HelpAndSupportResponse>?) {
        TODO("Not yet implemented")
    }
}