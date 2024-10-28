package com.noisefit.ui.onboarding.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class LanguageViewModel : BaseViewModel() {

    private val _languages = MutableLiveData<List<AppLanguage>>()
    val languages: LiveData<List<AppLanguage>> = _languages

    init {
        _languages.postValue(
            ApplicationUtils.getSupportedLanguages()
        )
    }

}