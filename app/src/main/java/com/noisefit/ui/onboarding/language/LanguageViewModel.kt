package com.noisefit.ui.onboarding.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    val localDataStore: DataStoredInterface
) : BaseViewModel() {

    var hideContinue: Boolean = false
    var selectedLanguage: String = ApplicationUtils.getDefaultLanguage().languageCode
    var selectedAppLanguage: AppLanguage? = null

    private val _languages = MutableLiveData<List<AppLanguage>>()
    val languages: LiveData<List<AppLanguage>> = _languages


    private val _languageUpdated = MutableLiveData<Event<Boolean>>()
    val languageUpdated: LiveData<Event<Boolean>> = _languageUpdated

    init {
        selectedLanguage = localDataStore.getSelectedAppLanguage()
            ?: ApplicationUtils.getDefaultLanguage().languageCode
        _languages.postValue(
            ApplicationUtils.getSupportedLanguages()
        )
    }

    fun updateSelectedLanguage(language: AppLanguage) {
        localDataStore.saveSelectedAppLanguage(language.languageCode)
        NoiseFitApplicationMain.updateUserLanguage(language)

        _languageUpdated.postValue(Event(true))

    }

}