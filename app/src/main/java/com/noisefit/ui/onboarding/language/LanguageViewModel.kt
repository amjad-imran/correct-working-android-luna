package com.noisefit.ui.onboarding.language

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.local.db.abstraction.KeyValueDataSource
import com.noisefit.data.local.db.abstraction.KeyValueDataType
import com.noisefit.data.model.language.AppLanguage
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.repository.LastSyncItems
import com.noisefit.data.repository.LastSyncProvider
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    val localDataStore: DataStoredInterface,
    private val userRepository: UserRepository,
    private val lastSyncProvider: LastSyncProvider,
    private val keyValueDataSource: KeyValueDataSource,
    private val userActivityRepository: OreoUserActivityRepository,
) : BaseViewModel() {

    var hideContinue: Boolean = false
    var selectedLanguage: String = ApplicationUtils.getDefaultLanguage().languageCode

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
        viewModelScope.launch(Dispatchers.IO) {
            userActivityRepository.clearAllHealthData()
            lastSyncProvider.removeSyncTimeStamp(LastSyncItems.HELP_AND_SUPPORT_LIST)
            keyValueDataSource.removeDataByKey("", KeyValueDataType.LEARN)
            keyValueDataSource.removeDataByType(KeyValueDataType.SLEEP_PLANNER)

            //Female health Data
            keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_CYCLE_HISTORY)
            keyValueDataSource.removeDataByType(KeyValueDataType.FEMALE_HEALTH_CURRENT_DAY_V2)

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
                            localDataStore.setHasUserSelectedLanguage()
                            _languageUpdated.postValue(Event(true))
                        }
                    }
                }
            }
        }
    }

    fun selectedListPosition(list: List<AppLanguage>, selectedLanguage: String): Int {
        return list.indexOfFirst {
            it.languageCode.equals(selectedLanguage, true)
        }
    }

}