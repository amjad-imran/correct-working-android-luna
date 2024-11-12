package com.noisefit.ui.onboarding.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.abstraction.AppRepository
import com.noisefit.data.repository.abstraction.UserRepository
import com.noisefit.ui.onboarding.auth.AuthMode
import com.noisefit.util.ApplicationUtils
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.Units
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.Event
import com.noisefit_commans.utils.LOGS
import com.oreo.data.repository.abstraction.OreoUserActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    private val userRepository: UserRepository,
    private val userActivityRepository: OreoUserActivityRepository,
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
        viewModelScope.launch {
            userActivityRepository.clearAllHealthData()

            userRepository.saveAppLanguage().collect { resource ->
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
                                        updateSelectedLanguage(language)
                                    }

                                    override fun no() {
                                        _languageUpdated.postValue(Event(true))
                                    }
                                }
                        })

                    }

                    is Resource.Success -> {

                        resource.data?.data?.let {
                            _languageUpdated.postValue(Event(true))

                        }
                    }
                }
            }
        }
    }

}