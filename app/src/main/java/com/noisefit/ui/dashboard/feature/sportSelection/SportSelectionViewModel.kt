package com.noisefit.ui.dashboard.feature.sportSelection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit.util.RyeexConst
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.AppConstants
import com.noisefit_commans.models.DeviceType
import com.noisefit_commans.models.SportsModeList
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SportSelectionViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watches: WatchesSDK,
) : BaseViewModel() {
    var maxCount = 20
    private var allSportModeList = ArrayList<SportsModeList.SportsMode>()

    init {
        val watchType = watches.getWatchType(sessionManager.connectedDevice.value)
        allSportModeList =
            when (sessionManager.connectedDevice.value?.deviceType) {
                DeviceType.COLORFIT_PRO_2.deviceType -> {
                    AppConstants.sportsModeListPro2
                }
                DeviceType.COLORFIT_PRO_2_OXY.deviceType -> {
                    AppConstants.sportsModeListOxy
                }
                else -> {
                    if (watchType == SDKWatchType.SDK_RYEEX) {
                        RyeexConst.sportsModeListColorFit
                    } else {
                        AppConstants.sportsModeList
                    }

                }
            }

        maxCount = if (watchType == SDKWatchType.SDK_RYEEX) {
               10
            } else {
                allSportModeList.size
            }


    }


    private val _noiseFitSearchQuery = MutableLiveData<String>()
    val noiseFitSearchQuery: LiveData<String>
        get() = _noiseFitSearchQuery

    var sportSelectedCount = MutableLiveData(0)

    private val selectedSports = ArrayList<SportsModeList.SportsMode>()


    private val _editMode = MutableLiveData<Boolean>()

    val editMode: LiveData<Boolean>
        get() = _editMode

    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }

    private val _sportsLiveData = MutableLiveData<ArrayList<SportsModeList.SportsMode>>()

    val sportsLiveData: LiveData<ArrayList<SportsModeList.SportsMode>>
        get() = _sportsLiveData

    fun setSportList(contactList: ArrayList<SportsModeList.SportsMode>) {
        _sportsLiveData.value = ArrayList()
        _sportsLiveData.value?.clear()
        _sportsLiveData.value = contactList
    }

    fun setAllSports(selectSportsMode: ArrayList<SportsModeList.SportsMode>) {
        selectedSports.clear()
        selectedSports.addAll(selectSportsMode)
        sportSelectedCount.value = selectedSports.size

        val remainingActivities =
            getRemainingActivities(selectSportsMode, allSportModeList)
        selectSportsMode.addAll(remainingActivities.toMutableList())

        _sportsLiveData.value = selectSportsMode
    }

    private fun getRemainingActivities(
        selectedSportsMode: List<SportsModeList.SportsMode>,
        totalSportsList: ArrayList<SportsModeList.SportsMode>
    ): List<SportsModeList.SportsMode> {

        val remainingList = ArrayList<SportsModeList.SportsMode>()
        totalSportsList.toMutableList().forEach { sportsMode ->
            val sportFound = selectedSportsMode.filter {
                it.name?.lowercase() == sportsMode.name?.lowercase()
            }
            if (sportFound.isEmpty()) {
                sportsMode.value = false
                remainingList.add(sportsMode)
            }
        }
        return remainingList
    }

    fun getSportList(): List<SportsModeList.SportsMode> {
        return _sportsLiveData.value ?: ArrayList()
    }


    fun getSelectedSport(): List<SportsModeList.SportsMode> {
        return selectedSports
    }

    fun updateSelectedSport(data: SportsModeList.SportsMode) {
        val sport = SportsModeList.SportsMode(value = data.value)
        sport.name = data.name
        sport.text = data.text
        sport.index = data.index
        sport.type = data.type
        sport.value = true
        selectedSports.add(sport)
    }

    fun removeSelectedSport(data: SportsModeList.SportsMode) {
        selectedSports.forEachIndexed { index, sContact ->
            if (sContact.name?.lowercase() == data.name?.lowercase()) {
                selectedSports.removeAt(index)
                return
            }
        }
        selectedSports.remove(data)
    }

    fun addSelectedCount() {
        if (sportSelectedCount.value == null) {
            sportSelectedCount.value = 1
        } else {
            val totalCount = sportSelectedCount.value!! + 1
            sportSelectedCount.value = totalCount
        }
    }

    fun subSelectedCount() {
        if (sportSelectedCount.value == null || sportSelectedCount.value!! <= 0) {
            sportSelectedCount.value = 0
        } else {
            val totalCount = sportSelectedCount.value!! - 1
            sportSelectedCount.value = totalCount
        }
    }


    fun setSport(data: List<SportsModeList.SportsMode>) {
        selectedSports.addAll(data)
    }
}