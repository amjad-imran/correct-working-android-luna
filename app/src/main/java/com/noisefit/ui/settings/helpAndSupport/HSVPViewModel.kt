package com.noisefit.ui.settings.helpAndSupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit_commans.data.response.VideoData
import com.noisefit_commans.ui.BaseViewModel

class HSVPViewModel : BaseViewModel() {
    var isFirstTimeLoad: Boolean = true
    var helpAndSupportVideoData =ArrayList<VideoData>()
    var videoThumbnailUrl: String = ""

    var languageList = arrayOf<String>()
    var defaultVideoPos: Int = 0

    var language = MutableLiveData("English")

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

}