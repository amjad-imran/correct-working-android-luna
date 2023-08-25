package com.oreo.ui.readiness

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.noisefit.data.remote.base.Resource
import com.noisefit.luna.R
import com.noisefit_commans.common.fromJson
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.oreo.data.model.ChartModel
import com.oreo.data.model.Contributors
import com.oreo.data.model.OContributorResponseModal
import com.oreo.data.model.TestDataModel
import com.oreo.data.model.health.Nudges
import com.oreo.data.model.health.OreoReadinessModel
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OreoReadinessViewModel
@Inject
constructor(
    val userActivityRepository: OreoUserActivityRepository,
    val ringDataStore: RingDataStore,
) : BaseViewModel() {


    private val _readinessData = MutableLiveData<TestDataModel>()
    val readinessData: LiveData<TestDataModel>
        get() = _readinessData

    private val _readinessHistoryResponse = MutableLiveData<List<OreoReadinessModel>>()
    val readinessHistoryResponse: LiveData<List<OreoReadinessModel>> = _readinessHistoryResponse

    private val _dayReadinessData = MutableLiveData<OreoReadinessModel>()
    val dayReadinessData: LiveData<OreoReadinessModel> = _dayReadinessData

    private val _contributorInfo = MutableLiveData<OContributorResponseModal>()
    private val contributorInfo: LiveData<OContributorResponseModal> = _contributorInfo


    fun getContributorInfo() {
        viewModelScope.launch {
            userActivityRepository.getContributorDetailsInfo(
                "readiness"
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
                                        getContributorInfo()
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {
                            _contributorInfo.postValue(it)
                        }
                    }
                }
            }
        }


    }


    fun getReadinessDetailsData(date: String? = null) {
        viewModelScope.launch {
            userActivityRepository.getReadinessHistory(
                date ?: DateFormats.getCurrentDateOreoFormat()
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
                                        getReadinessDetailsData(date)
                                    }

                                    override fun no() {

                                    }
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.data?.let {

                            val response ="[{\n" +
                                    "\t\t\"date\": \"2023-08-25\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 69,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 19020,\n" +
                                    "\t\t\t\"valPrcnt\": 75,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 64,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 5040,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [0, 86, 0, 86, 85, 88, 66, 65, 61, 67, 60, 59, 64, 60, 64, 63, 66, 67, 99, 67, 74, 65, 64, 62, 61, 61, 64, 59, 62, 61, 56, 63, 65, 71, 65, 58, 61, 255, 63, 64, 255, 63, 61, 61, 62, 69, 56, 58, 57, 56, 58, 54, 60, 66, 60, 76, 0, 0, 0, 0, 0, 57, 54, 69, 62, 85, 60, 60, 66, 255, 57, 58, 61],\n" +
                                    "\t\t\t\"avg\": 65,\n" +
                                    "\t\t\t\"low\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [0, 96, 0, 39, 34, 98, 47, 55, 52, 2, 22, 56, 94, 193, 61, 73, 68, 74, 77, 61, 203, 198, 74, 150, 251, 47, 55, 56, 195, 59, 154, 173, 216, 234, 54, 248, 51, 178, 44, 44, 46, 32, 62, 42, 44, 90, 66, 49, 56, 124, 48, 65, 74, 13, 37, 40, 0, 0, 0, 0, 0, 27, 75, 28, 209, 16, 88, 219, 66, 54, 79, 76, 126],\n" +
                                    "\t\t\t\"avg\": 88,\n" +
                                    "\t\t\t\"max\": 251,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 88,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 11,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 96.8,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [92.1, 92.1, 92.1, 92.1, 92.1, 92.1, 92.1, 94.5, 94.5, 94.5, 94.5, 94.5, 96.8, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.6, 94.3, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.8, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.5, 94.1, 94.1, 93.9, 94.5, 94.5, 93.6, 94.5, 94.5, 94.5, 93.9, 94.5, 94.5, 94.5, 94.5, 94.5, 95, 94.5, 94.3, 93.9, 93.4, 93.2, 92.5, 93.6, 93.9, 94.5, 94.5],\n" +
                                    "\t\t\t\"avg\": 94.18,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-25 02:01:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-25 07:58:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 38,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"activityBalance\": {\n" +
                                    "\t\t\t\"value\": 2,\n" +
                                    "\t\t\t\"valPrcnt\": 67,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-24\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 51,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 13860,\n" +
                                    "\t\t\t\"valPrcnt\": 55,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 61,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 3720,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [85, 69, 71, 86, 70, 99, 63, 63, 59, 61, 61, 59, 62, 65, 62, 71, 69, 69, 101, 61, 64, 65, 62, 61, 59, 56, 61, 60, 59, 60, 61, 54, 61, 61, 64, 64, 60, 63, 63, 61, 60, 60, 62, 70, 56, 57, 58, 54, 55, 60, 63, 68],\n" +
                                    "\t\t\t\"avg\": 64,\n" +
                                    "\t\t\t\"low\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [238, 32, 79, 218, 88, 174, 73, 47, 54, 61, 221, 67, 97, 42, 204, 35, 36, 43, 55, 63, 37, 43, 45, 58, 47, 55, 51, 62, 56, 49, 48, 72, 232, 4, 251, 56, 170, 73, 62, 57, 61, 57, 67, 205, 62, 54, 80, 131, 75, 79, 117, 52],\n" +
                                    "\t\t\t\"avg\": 86,\n" +
                                    "\t\t\t\"max\": 251,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 86,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-24 02:48:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-24 06:57:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 6,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-23\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 47,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 55,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 8490,\n" +
                                    "\t\t\t\"valPrcnt\": 34,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 81,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 2910,\n" +
                                    "\t\t\t\"valPrcnt\": 81,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [56, 55, 57, 58, 58, 59, 75, 107, 58, 59, 65, 66, 62, 63, 68, 58, 65, 64, 66, 255, 61, 67, 65, 63, 64, 62, 61, 62, 62, 59, 65, 58, 85, 97, 255, 0, 0, 0],\n" +
                                    "\t\t\t\"avg\": 65,\n" +
                                    "\t\t\t\"low\": 55,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [128, 82, 86, 62, 56, 56, 61, 68, 107, 31, 72, 57, 45, 67, 75, 43, 54, 52, 39, 51, 52, 43, 74, 78, 240, 52, 79, 112, 79, 247, 223, 108, 69, 101, 12, 10, 0, 0],\n" +
                                    "\t\t\t\"avg\": 80,\n" +
                                    "\t\t\t\"max\": 247,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 80,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-23 03:49:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-23 06:49:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-22\"\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-21\"\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-20\"\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-19\"\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-18\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 76,\n" +
                                    "\t\t\t\"text\": \"Good\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 15450,\n" +
                                    "\t\t\t\"valPrcnt\": 61,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 97,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 5160,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [62, 58, 70, 66, 67, 64, 66, 68, 65, 58, 61, 66, 64, 61, 65, 61, 64, 62, 64, 64, 61, 71, 69, 66, 77, 61, 68, 69, 67, 67, 68, 65, 63, 77, 60, 97, 64, 77, 65, 69, 63, 61, 56, 61, 59, 61, 61, 54, 61, 60, 62, 65, 94, 70, 60, 59, 71, 61, 61, 65, 60, 66],\n" +
                                    "\t\t\t\"avg\": 65,\n" +
                                    "\t\t\t\"low\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [66, 76, 93, 92, 11, 68, 113, 46, 71, 47, 68, 61, 53, 76, 49, 53, 49, 54, 62, 177, 97, 230, 58, 42, 42, 40, 111, 36, 168, 36, 39, 63, 74, 67, 70, 25, 56, 73, 39, 213, 54, 0, 114, 51, 45, 47, 40, 49, 60, 58, 217, 33, 107, 71, 77, 74, 53, 42, 107, 48, 52, 46, 57],\n" +
                                    "\t\t\t\"avg\": 72,\n" +
                                    "\t\t\t\"max\": 230,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 72,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-18 02:19:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-18 07:22:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"activityBalance\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"valPrcnt\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 89,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-17\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 61,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 56,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 12720,\n" +
                                    "\t\t\t\"valPrcnt\": 50,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 90,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 3840,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [60, 63, 64, 59, 59, 61, 57, 75, 72, 68, 63, 60, 56, 64, 62, 60, 59, 60, 62, 63, 62, 64, 57, 59, 70, 63, 59, 71, 73, 73, 67, 66, 94, 64, 72, 70, 75, 68, 70, 68, 67, 66, 66, 63, 63, 92, 84, 71, 68, 74, 255],\n" +
                                    "\t\t\t\"avg\": 67,\n" +
                                    "\t\t\t\"low\": 56,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [45, 45, 57, 51, 46, 48, 48, 60, 100, 98, 121, 51, 56, 77, 255, 50, 63, 40, 107, 49, 45, 40, 49, 49, 59, 57, 164, 55, 61, 41, 183, 34, 43, 34, 158, 44, 47, 30, 11, 58, 28, 34, 76, 40, 44, 54, 35, 209, 89, 26, 48, 6],\n" +
                                    "\t\t\t\"avg\": 62,\n" +
                                    "\t\t\t\"max\": 209,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 62,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-17 02:20:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-17 06:31:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"activityBalance\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"valPrcnt\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-16\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 84,\n" +
                                    "\t\t\t\"text\": \"Good\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 20580,\n" +
                                    "\t\t\t\"valPrcnt\": 82,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 87,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 5970,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [64, 73, 67, 68, 74, 92, 61, 63, 62, 64, 63, 64, 64, 64, 59, 57, 61, 66, 67, 0, 56, 58, 63, 62, 60, 63, 61, 62, 65, 63, 62, 62, 59, 66, 61, 64, 61, 61, 57, 57, 60, 59, 60, 58, 59, 58, 73, 59, 60, 62, 61, 63, 0, 54, 84, 55, 59, 59, 62, 59, 61, 61, 63, 61, 62, 57, 55, 59, 61, 58, 65, 57, 60, 64, 62],\n" +
                                    "\t\t\t\"avg\": 62,\n" +
                                    "\t\t\t\"low\": 54,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [18, 9, 16, 13, 33, 19, 32, 30, 41, 42, 25, 33, 36, 29, 35, 14, 49, 29, 14, 28, 25, 51, 33, 35, 28, 28, 21, 30, 22, 27, 25, 33, 24, 24, 24, 27, 38, 8, 27, 32, 32, 36, 31, 29, 25, 32, 19, 17, 32, 23, 34, 34, 24, 29, 10, 36, 25, 13, 31, 20, 23, 30, 22, 35, 33, 27, 27, 27, 34, 35, 34, 37, 28, 21, 24],\n" +
                                    "\t\t\t\"avg\": 28,\n" +
                                    "\t\t\t\"max\": 51,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 28,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-16 00:49:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-16 07:46:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-15\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 82,\n" +
                                    "\t\t\t\"text\": \"Good\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 51,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 23160,\n" +
                                    "\t\t\t\"valPrcnt\": 92,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 81,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 5610,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [87, 91, 76, 73, 70, 70, 101, 72, 69, 70, 67, 68, 72, 64, 65, 66, 78, 72, 63, 68, 64, 66, 65, 64, 65, 65, 65, 64, 86, 57, 61, 59, 60, 66, 55, 66, 63, 66, 67, 65, 59, 61, 57, 65, 60, 57, 56, 58, 63, 59, 58, 63, 61, 68, 63, 58, 57, 61, 59, 64, 59, 51, 63, 68, 63, 63, 64, 59, 59, 61, 64, 255, 62, 63, 58, 61, 59, 55, 59, 60, 59, 61, 63, 69, 69],\n" +
                                    "\t\t\t\"avg\": 65,\n" +
                                    "\t\t\t\"low\": 51,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [19, 40, 17, 65, 55, 51, 209, 52, 74, 103, 48, 44, 37, 44, 46, 46, 29, 27, 92, 27, 76, 41, 46, 69, 51, 53, 53, 50, 114, 99, 70, 66, 98, 21, 46, 35, 49, 42, 42, 34, 52, 48, 63, 63, 87, 141, 127, 211, 191, 53, 64, 57, 46, 13, 62, 201, 60, 64, 73, 75, 83, 121, 215, 110, 82, 233, 72, 79, 83, 36, 55, 177, 186, 127, 67, 58, 46, 130, 69, 55, 59, 67, 79, 125, 188, 132],\n" +
                                    "\t\t\t\"avg\": 79,\n" +
                                    "\t\t\t\"max\": 233,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 79,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-15 00:49:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-15 07:46:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 13,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"activityBalance\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"valPrcnt\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-14\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 82,\n" +
                                    "\t\t\t\"text\": \"Good\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 51,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 23160,\n" +
                                    "\t\t\t\"valPrcnt\": 92,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 78,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 5610,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [101, 72, 69, 70, 67, 68, 72, 64, 65, 66, 78, 72, 63, 68, 64, 66, 65, 64, 65, 65, 65, 64, 86, 57, 61, 59, 60, 66, 55, 66, 63, 66, 67, 65, 59, 61, 57, 65, 60, 57, 56, 58, 63, 59, 58, 63, 61, 68, 63, 58, 57, 61, 59, 64, 59, 51, 63, 68, 63, 63, 64, 59, 59, 61, 64, 255, 62, 63, 58, 61, 59, 55, 59, 60, 59, 61, 63, 69, 69, 94, 0, 0, 0, 0, 0],\n" +
                                    "\t\t\t\"avg\": 64,\n" +
                                    "\t\t\t\"low\": 51,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [55, 51, 209, 52, 74, 103, 48, 44, 37, 44, 46, 46, 29, 27, 92, 27, 76, 41, 46, 69, 51, 53, 53, 50, 114, 99, 70, 66, 98, 21, 46, 35, 49, 42, 42, 34, 52, 48, 63, 63, 87, 141, 127, 211, 191, 53, 64, 57, 46, 13, 62, 201, 60, 64, 73, 75, 83, 121, 215, 110, 82, 233, 72, 79, 83, 36, 55, 177, 186, 127, 67, 58, 46, 130, 69, 55, 59, 67, 79, 125, 188, 132, 0, 0, 0, 0],\n" +
                                    "\t\t\t\"avg\": 81,\n" +
                                    "\t\t\t\"max\": 233,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 81,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-14 00:49:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-14 07:46:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 18,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"activityBalance\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"valPrcnt\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"valPrcnt\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-13\",\n" +
                                    "\t\t\"readiness_score\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHr\": {\n" +
                                    "\t\t\t\"value\": 52,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"totalSleep\": {\n" +
                                    "\t\t\t\"value\": 25710,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"sleepBalance\": {\n" +
                                    "\t\t\t\"valPrcnt\": 71,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"recoveryIndex\": {\n" +
                                    "\t\t\t\"value\": 6480,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrBreakUp\": {\n" +
                                    "\t\t\t\"value\": [65, 62, 61, 61, 61, 68, 64, 71, 67, 69, 64, 66, 64, 61, 62, 61, 60, 58, 58, 55, 56, 56, 60, 54, 55, 52, 58, 62, 58, 55, 93, 55, 58, 59, 55, 62, 59, 58, 59, 63, 65, 63, 85, 63, 64, 63, 63, 59, 61, 63, 59, 62, 57, 55, 65, 63, 64, 57, 64, 66, 62, 61, 62, 65, 60, 60, 58, 60, 57, 67, 59, 66, 57, 62, 66, 62, 88, 62, 65, 64, 55, 59, 63, 60, 60, 59, 63, 55, 61, 61, 66, 62, 60, 81, 70, 81, 255, 0, 0],\n" +
                                    "\t\t\t\"avg\": 62,\n" +
                                    "\t\t\t\"low\": 52,\n" +
                                    "\t\t\t\"unit\": \"bpm\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrvBreakUp\": {\n" +
                                    "\t\t\t\"value\": [77, 225, 71, 65, 57, 57, 69, 81, 120, 73, 123, 130, 63, 61, 39, 211, 73, 66, 60, 72, 67, 69, 74, 82, 81, 88, 105, 133, 220, 83, 223, 83, 106, 64, 67, 121, 189, 126, 179, 102, 133, 87, 69, 64, 78, 85, 49, 55, 92, 45, 55, 79, 59, 59, 74, 138, 173, 93, 47, 203, 58, 0, 60, 75, 57, 39, 56, 60, 65, 49, 59, 153, 56, 62, 85, 84, 13, 59, 38, 253, 49, 182, 121, 118, 52, 55, 187, 77, 237, 75, 68, 235, 157, 60, 105, 63, 30, 45, 0, 0],\n" +
                                    "\t\t\t\"avg\": 94,\n" +
                                    "\t\t\t\"max\": 253,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrv\": {\n" +
                                    "\t\t\t\"value\": 94,\n" +
                                    "\t\t\t\"unit\": \"ms\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"respiration\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperature\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"temperatureBreakUp\": {\n" +
                                    "\t\t\t\"value\": [],\n" +
                                    "\t\t\t\"avg\": 0,\n" +
                                    "\t\t\t\"unit\": \"°F\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"start_time\": \"2023-08-13 02:22:00\",\n" +
                                    "\t\t\"end_time\": \"2023-08-13 10:32:00\",\n" +
                                    "\t\t\"activity_score\": {\n" +
                                    "\t\t\t\"value\": 26,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"activityBalance\": {\n" +
                                    "\t\t\t\"value\": 0,\n" +
                                    "\t\t\t\"valPrcnt\": 0,\n" +
                                    "\t\t\t\"text\": \"Pay Attention\",\n" +
                                    "\t\t\t\"status\": \"warning\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"restingHrBalance\": {\n" +
                                    "\t\t\t\"value\": 1,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t},\n" +
                                    "\t\t\"hrReserve\": {\n" +
                                    "\t\t\t\"value\": 252,\n" +
                                    "\t\t\t\"valPrcnt\": 100,\n" +
                                    "\t\t\t\"text\": \"Optimal\",\n" +
                                    "\t\t\t\"status\": \"good\"\n" +
                                    "\t\t}\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-12\"\n" +
                                    "\t}, {\n" +
                                    "\t\t\"date\": \"2023-08-11\"\n" +
                                    "\t}]"

                            val parsed = Gson().fromJson<List<OreoReadinessModel>>(response)


                            _readinessHistoryResponse.postValue(parsed.reversed())

                            parsed.firstOrNull()?.let { data ->
                                _dayReadinessData.postValue(data)
                            }
                        }
                    }
                }
            }

        }


    }


    var dateList = ArrayList<String>()
    fun getPrefixAndSuffixList(dataList: List<OreoReadinessModel>):
            Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        dataList.reversed()
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            var currentDayText = ""
            if (it.date == DateFormats.getCurrentDate(DateFormats.dateFormat3)) {
                currentDayText = "Today, "
            }
            val formattedDate = DateFormats.formatDate(
                it.date,
                DateFormats.dateFormat3,
                DateFormats.dateFormat7
            )
            chartModel.formattedDate = "$currentDayText $formattedDate"
            chartModel.date = it.date
            chartModel.index = DateFormats.formatWeek(it.date)
            chartModel.value = it.readinessScore?.value ?: 0
            list.add(chartModel)
            dateList.add(it.date)
        }

        list.reverse()
        val lastDateFromList = dataList.last().date
        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )
        val currentDateFromList = dataList.first().date
        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
            currentDate,
            DateFormats.addDateFormat3(currentDate, 14)!!,
            DateFormats.dateFormat3, DateFormats.singleWeekDay
        )

        val suffix = java.util.ArrayList<ChartModel>()
        suffixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.value = 0
            chartModel.date = ""
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        prefixDatesList.forEach {
            val chartModel = ChartModel()
            chartModel.index = it
            chartModel.value = 0
            chartModel.date = ""
            prefix.add(chartModel)
        }
        prefix.reverse()


        return Triple(list, suffix, prefix)

    }

    fun getHeartPrefixAndSuffixList(dataList: List<Int>):
            Triple<ArrayList<ChartModel>, ArrayList<ChartModel>, ArrayList<ChartModel>> {
        dataList.reversed()
        val list = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.date = ""
            chartModel.index = ""
            chartModel.value = it
            list.add(chartModel)
        }

        list.reverse()
//        val lastDateFromList = dataList.last().date
//        val lastDate = DateFormats.subtractDateFormat3(lastDateFromList, 1)!!
//        val suffixDatesList = DateFormats.getWeekDaysBetweenDates(
//            DateFormats.subtractDateFormat3(lastDate, 14)!!, lastDate,
//            DateFormats.dateFormat3, DateFormats.singleWeekDay
//        )
//        val currentDateFromList = dataList.first().date
//        val currentDate = DateFormats.addDateFormat3(currentDateFromList, 1)!!
//        val prefixDatesList = DateFormats.getWeekDaysBetweenDates(
//            currentDate,
//            DateFormats.addDateFormat3(currentDate, 14)!!,
//            DateFormats.dateFormat3, DateFormats.singleWeekDay
//        )

        val suffix = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            suffix.add(chartModel)
        }

        suffix.reverse()


        val prefix = java.util.ArrayList<ChartModel>()
        dataList.forEach {
            val chartModel = ChartModel()
            chartModel.index = ""
            chartModel.value = 0
            chartModel.date = ""
            prefix.add(chartModel)
        }
        prefix.reverse()


        return Triple(list, suffix, prefix)

    }


    fun updateSelectedDate(date: String) {
        val dayData = _readinessHistoryResponse.value?.firstOrNull() {
            it.date.equals(date, false)
        }
        if (dayData != null) {
            _dayReadinessData.postValue(dayData)
        }
    }

    fun getBannerDummyData(): ArrayList<Nudges> {
        val listData = ArrayList<Nudges>()
        listData.add(
            Nudges(
                "You’re on the mend",
                "A poor sleep score could be caused by insufficient or extraneous sleep, or due to frequent dreams."
            )
        )
        listData.add(
            Nudges(
                "You’re on the mend",
                "A poor sleep score could be caused by insufficient or extraneous sleep, or due to frequent dreams."
            )
        )
        listData.add(
            Nudges(
                "You’re on the mend",
                "A poor sleep score could be caused by insufficient or extraneous sleep, or due to frequent dreams."
            )
        )
        return listData
    }


    private fun getParsedDescriptionData(): ArrayList<String> {
        val descriptionList = ArrayList<String>()
        descriptionList.add(contributorInfo.value?.yesterdaySleepDuration ?: "")
        descriptionList.add(contributorInfo.value?.sleepBalance ?: "")
        descriptionList.add(contributorInfo.value?.yesterdayActivity ?: "")
        descriptionList.add(contributorInfo.value?.activityBalance ?: "")
        descriptionList.add(contributorInfo.value?.hrvBalance ?: "")
        descriptionList.add(contributorInfo.value?.restingHr ?: "")
        descriptionList.add(contributorInfo.value?.heartRate ?: "")
        descriptionList.add(contributorInfo.value?.recoveryIndex ?: "")
        return descriptionList
    }

    fun prepareDataForDescriptionArray(resultData: java.util.ArrayList<Contributors>): ArrayList<Contributors> {
        val contList = ArrayList<Contributors>()
        val desList = getParsedDescriptionData()
        for (i in resultData.indices) {
            val ctList = resultData[i]
            val child = Contributors(
                title = ctList.title,
                leftText = ctList.leftText,
                leftTextColor = ctList.leftTextColor,
                barColor = ctList.barColor,
                barPercent = ctList.barPercent,
                backgroundRes = ctList.backgroundRes,
                description = desList[i]
            )
            contList.add(child)
        }

        return contList
    }

    fun getContributorsData(dayData: OreoReadinessModel?): List<Contributors> {
        val result = ArrayList<Contributors>()

        if (dayData?.totalSleep != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.totalSleep.status)
            result.add(
                Contributors(
                    title = "Yesterday's sleep duration",
                    leftText = dayData.totalSleep.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.totalSleep.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Yesterday's sleep duration",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }
        if (dayData?.sleepBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.sleepBalance.status)
            result.add(
                Contributors(
                    title = "Sleep balance",
                    leftText = dayData.sleepBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.sleepBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Sleep balance",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.activityScore != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityScore.status)

            result.add(
                Contributors(
                    title = "Yesterday's activity",
                    leftText = dayData.activityScore.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityScore.value ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Yesterday's activity",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.activityBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.activityBalance.status)

            result.add(
                Contributors(
                    title = "Activity balance",
                    leftText = dayData.activityBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.activityBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Activity balance",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.hrvBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.hrvBalance.status)
            result.add(
                Contributors(
                    title = "HRV balance",
                    leftText = dayData.hrvBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.hrvBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "HRV balance",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.restingHrBalance != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.restingHrBalance.status)

            result.add(
                Contributors(
                    title = "Resting HR",
                    leftText = dayData.restingHrBalance.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.restingHrBalance.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Resting HR",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.hrReserve != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.hrReserve.status)

            result.add(
                Contributors(
                    title = "Heart rate reserve",
                    leftText = dayData.hrReserve.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.hrReserve.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Heart rate reserve",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }

        if (dayData?.recoveryIndex != null) {
            val (textColor, barColor, background) = getContributorsColors(dayData.recoveryIndex.status)

            result.add(
                Contributors(
                    title = "Recovery index",
                    leftText = dayData.recoveryIndex.text,
                    leftTextColor = textColor,
                    barColor = barColor,
                    barPercent = dayData.recoveryIndex.valPrcnt ?: 0,
                    backgroundRes = background
                )
            )
        } else {
            result.add(
                Contributors(
                    title = "Recovery index",
                    leftText = "",
                    leftTextColor = R.color.white,
                    barColor = R.color.readiness_progress_color,
                    barPercent = 1,
                    backgroundRes = com.noisefit_commans.R.drawable.back_modal_new
                )
            )
        }




        return result
    }

    private fun getContributorsColors(status: String): Triple<Int, Int, Int> {
        return if (status.equals("warning", true)) {
            Triple(
                R.color.oreo_contributor_warning,
                R.color.oreo_contributor_warning,
                com.noisefit_commans.R.drawable.back_modal_new_warning
            )
        } else {
            Triple(
                R.color.white,
                R.color.readiness_progress_color,
                com.noisefit_commans.R.drawable.back_modal_new
            )
        }
    }

    fun getStatusColors(status: String?): Int {
        return if (status.equals("warning", true)) {
            R.color.oreo_contributor_warning
        } else {
            R.color.steps_arc
        }
    }

    fun getDummyBreakUpDataForTimeDisplay(): ArrayList<Int> {
        val dummyList = ArrayList<Int>()
        for (i in 0..287) {
            dummyList.add(0)
        }
        return dummyList

    }
    fun getDummyBreakUpDataForTimeDisplayFloat(): ArrayList<Float> {
        val dummyList = ArrayList<Float>()
        for (i in 0..287) {
            dummyList.add(0f)
        }
        return dummyList

    }


}