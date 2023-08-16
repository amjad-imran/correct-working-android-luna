package com.noisefit.ui.settings.feedbacknew

import android.net.Uri
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.noisefit.NoiseFitApplicationMain.Companion.context
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.NoisefitApplication
import com.noisefit_commans.constants.WatchInfoGlobals
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.data.local.abstraction.RingDataStore
import com.noisefit_commans.data.local.abstraction.WatchDataStore
import com.noisefit_commans.data.model.FeedbackNew
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.DateFormats
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class FeedbackNewViewModel @Inject constructor(
    private val localDataStore: DataStoredInterface,
    private val watchDataStore: WatchDataStore,
    private val ringDataStore: RingDataStore,
    private val deviceRepository: DeviceRepository,
    val sessionManager: SessionManager
) : BaseViewModel() {

    var submittedSuccessfully = MutableLiveData<Boolean>()
    var problemType: String = ""
    var rating: Int = -1

    var problemTypeList = HashSet<String>()
    val feedbackUriList = MutableLiveData<ArrayList<Uri>>()

    var showAttachLogButton = MutableLiveData<Boolean>(false)
    private val _feedbackQuestion = MutableLiveData<ArrayList<FeedbackQuestionaries>>()
    val feedbackQuestion: LiveData<ArrayList<FeedbackQuestionaries>> = _feedbackQuestion
    var questionList = ArrayList<FeedbackQuestionaries>()


    fun getLocalDataStore(): DataStoredInterface {
        return localDataStore
    }

    fun getWatchDataStore(): WatchDataStore {
        return watchDataStore
    }

    fun submitFeedbackNew(
        feedback: FeedbackNew
    ) {

        feedback.user_id = localDataStore.getUser()?.id

        val request = JsonObject().apply {
            addProperty("platform", feedback.platform)
            addProperty("mobile_device", feedback.mobileDevice)
            addProperty("os_version", feedback.osVersion)
            addProperty("app_version", feedback.appVersion)
            addProperty("watch_name", feedback.watchName)
            addProperty("watch_firmware_version", feedback.watchFirmwareVersion)
            addProperty("rating", feedback.rating)
            addProperty("problem_type", feedback.problemType)
            addProperty("suggestion", feedback.suggestions)
            addProperty("date", feedback.date)
            addProperty("user_id", feedback.user_id)
        }


        viewModelScope.launch {
            deviceRepository.submitFeedbackNew(
                request
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
                            (this.uiComponentType as UIComponentType.RetryApiDialog).callback =
                                object : BinaryActionCallback {
                                    override fun yes() {
                                        submitFeedbackNew(
                                            feedback
                                        )
                                    }

                                    override fun no() {}
                                }
                        })
                    }

                    is Resource.Success -> {
                        resource.data?.let {
                            submittedSuccessfully.postValue(true)
                        } ?: sendMessage("Something went wrong")
                    }
                }

            }
        }

    }

    fun provideFeedbackNewData(
        rating: Int,
        problemType: String,
        comment: String,
    ): FeedbackNew {
        val packageInfo = NoisefitApplication.context!!.packageManager.getPackageInfo(
            NoisefitApplication.context!!.packageName,
            0
        )
        val versionName = packageInfo.versionName
        val versionCode = packageInfo.versionCode


        val connectedDevice = ringDataStore.getRingDevice()


        val platform = "android"
        val mobileDevice = "${Build.BRAND} ${Build.MODEL}"
        val osVersion = Build.VERSION.RELEASE
        val appVersion = "$versionName($versionCode)"
        val watchName = connectedDevice?.bluetoothName.toString()
        val watchFirmwareVersion = WatchInfoGlobals.firmwareVersionRing
            ?: WatchInfoGlobals.firmwareVersionNumberRing.toString()

        LOGS.d("connectedDevice $watchName")
        return FeedbackNew(
            platform,
            mobileDevice,
            osVersion,
            appVersion,
            watchName,
            watchFirmwareVersion,
            rating,
            problemType,
            comment,
            DateFormats.getTodaysDateString(9)
        )
    }

    fun getQuestionData() {
        lateinit var jsonString: String
        try {
//            val fileName: String = if (localDataStore.getPairDeviceType()== Device.RING) {
//                "feedback_question_oreo.json"
//            } else
            val fileName = "feedback_question.json"
            jsonString = context?.assets?.open(fileName)
                ?.bufferedReader()
                .use { it?.readText() ?: "" }
        } catch (ioException: IOException) {
            LOGS.d(ioException)
        }
        val listCountryType = object : TypeToken<List<FeedbackQuestionaries>>() {}.type
        _feedbackQuestion.postValue(Gson().fromJson(jsonString, listCountryType))
    }


}