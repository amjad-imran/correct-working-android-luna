package com.noisefit.ui.settings.helpAndSupport.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit.data.remote.base.Resource
import com.noisefit_commans.data.response.HelpAndSupportDetailResponse
import com.noisefit_commans.data.response.HelpAndSupportQuestion
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.response.VideoData
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.BuildUtils
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HelpAndSupportDetailsViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    private val appRepository: AppRepository,
) : BaseViewModel() {

    var module: String? = null
    var phoneType = 4
    var isFirstTimeLoad: Boolean = true
    var thumbnailUrl: String = ""
    var languageList = arrayOf<String>()

    var language = MutableLiveData("English")
    var defaultVideoPos: Int = 0
    var videoThumbnailUrl: String = ""


    init {
        fetchPhoneType()
    }

    private fun fetchPhoneType() {
        val model = BuildUtils.getDeviceManufacturer().trim()
        // phoneType: 0 xiaomi   1 huawei   2 oppo   3 vivo   4 其它（other）
        LOGS.d("HELPANDSUPPORT ${model}")
        when (model.lowercase()) {
            "xiaomi" -> {
                phoneType = 0
            }
            "huawei" -> {
                phoneType = 1
            }
            "oppo" -> {
                phoneType = 2
            }
            "vivo" -> {
                phoneType = 3
            }
            else -> {
                phoneType = 4
            }
        }


    }

    /**
     * -1 - No action
     * 0 -> Thumbs down
     * 1 -> Thumbs up
     */
    var thumbLastState: Int? = -1

    private val _questionId = MutableLiveData<Int>()
    val questionId: LiveData<Int>
        get() = _questionId

    private val _helpAndSupportDetails = MutableLiveData<HelpAndSupportDetailResponse>()
    val helpAndSupportDetails: LiveData<HelpAndSupportDetailResponse>
        get() = _helpAndSupportDetails

    fun setHealthAndSupportQuestionId(id: Int) {
        _questionId.postValue(id)
    }

    fun fetchHelpAndSupportData(id: Int) {
        viewModelScope.launch {
            val manufacture = BuildUtils.getDeviceManufacturer().lowercase().replace(" ", "").trim()
            appRepository.getHelpAndSupportByQuestionId(id, manufacture)
                .collect { resource ->
                    when (resource) {
                        is Resource.GenericError -> {
                            sendMessage(resource.message)
                        }
                        is Resource.Loading -> {
                            setLoading(resource.loading)
                        }
                        is Resource.NetworkError -> {
                            setApiErrors(resource.response.apply {
                                (this.uiComponentType as UIComponentType.RetryApiDialog).callback = object : BinaryActionCallback {
                                    override fun yes() {
                                        fetchHelpAndSupportData(id)
                                    }

                                    override fun no() {}
                                }
                            })
                        }

                        is Resource.Success -> {
                            resource.data?.data?.let {
                                _helpAndSupportDetails.postValue(it)
                            }
                        }
                    }
                }
        }
    }

    fun setLanguage(selectedLanguage: String) {
        language.value = selectedLanguage
    }

    fun getSelectedLanguageValue(): String {
        return if (language.value?.lowercase() == "english")
            "English"
        else
            "Hindi"
    }

    fun getLanguageValues(): Array<String> {
        return languageList
    }

    fun setLanguageValues(languageData: Array<String>) {
        languageList = languageData
    }

    fun parseLanguageData(videoData: List<VideoData>): Array<String> {
        val langList = arrayOf("", "")
        for (i in videoData.indices) {
            val tempLng = if (videoData[i].language?.lowercase() == "en") {
                "English"
            } else
                "Hindi"
            langList[i] = tempLng
        }
        return langList
    }

}