package com.noisefit.ui.dashboard.feature.sportSelection.zh

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.models.Widget
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ZhSportSelectionViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface
) : BaseViewModel() {
    var maxCount = 10


    private val _selectedSports = MutableLiveData<ArrayList<Widget>>()
    val selectedSports: LiveData<ArrayList<Widget>>
        get() = _selectedSports


    var fetchData = false
    private val _noiseFitSearchQuery = MutableLiveData<String>()
    val noiseFitSearchQuery: LiveData<String>
        get() = _noiseFitSearchQuery


    private val _allSportModeList = MutableLiveData<ArrayList<Widget>>()
    val allSportModeList: LiveData<ArrayList<Widget>>
        get() = _allSportModeList


    var sportSelectedCount = MutableLiveData(0)
    private val _editMode = MutableLiveData<Boolean>()

    val editMode: LiveData<Boolean>
        get() = _editMode


    init {
        //maxCount = allSportModeList.size
    }


    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }


    fun setAllSports(sportList: ArrayList<Widget>) {
        _allSportModeList.value?.clear()
        _selectedSports.value?.clear()
        if (_allSportModeList.value.isNullOrEmpty()) {
            _allSportModeList.value = ArrayList()
        }
        if (_selectedSports.value.isNullOrEmpty()) {
            _selectedSports.value = ArrayList()
        }

        val enableWidgetList = ArrayList<Widget>()
        _allSportModeList.value!!.addAll(sportList)
        sportList.forEach { widget ->
            if (widget.isEnable) {
                enableWidgetList.add(widget)

            }
        }
        sportSelectedCount.value = enableWidgetList.size
        _selectedSports.value = enableWidgetList
    }


    fun getSyncSportList(): List<Widget> {
        val sportList = ArrayList<Widget>()
        getSelectedSport().forEach { widget ->
            sportList.add(widget)
        }

        val remainingActivities =
            getRemainingActivities(sportList, getAllSportList())
        sportList.addAll(remainingActivities)
        return sportList
    }

    private fun getRemainingActivities(
        selectedSportsMode: List<Widget>,
        totalSportsList: List<Widget>
    ): List<Widget> {

        val remainingList = ArrayList<Widget>()
        totalSportsList.toMutableList().forEach { sportsMode ->
            val sportFound = selectedSportsMode.filter {
                it.name.lowercase() == sportsMode.name.lowercase()
            }
            if (sportFound.isEmpty()) {
                sportsMode.isEnable = false
                remainingList.add(sportsMode)
            }
        }
        return remainingList
    }


    fun getAllSportList(): List<Widget> {
        return _allSportModeList.value ?: ArrayList()
    }

    fun getSelectedSport(): List<Widget> {
        return _selectedSports.value ?: ArrayList()
    }


    fun updateSelectedSport(data: Widget) {
        val widget = Widget(
            data.functionId,
            data.name,
            data.haveHide,
            data.isEnable,
            data.order,
            data.sortable
        )
        _selectedSports.value!!.add(widget)
    }

    fun removeSelectedSport(data: Widget) {
        _selectedSports.value?.forEachIndexed { index, sContact ->
            if (sContact.name.lowercase() == data.name.lowercase()) {
                _selectedSports.value!!.removeAt(index)
                return
            }
        }
        _selectedSports.value!!.remove(data)
    }

    fun removeSelectedFromAllSport(data: Widget) {
        _allSportModeList.value?.forEach { sport ->
            if (sport.functionId == data.functionId) {
                sport.isEnable = false
                LOGS.d("dasdas $sport")
                return@forEach
            }
        }
        _allSportModeList.value = _allSportModeList.value

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


}