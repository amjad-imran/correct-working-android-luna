package com.noisefit.ui.dashboard.feature.appList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.noisefit.session.SessionManager
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import com.noisefit_commans.models.Widget
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class AppListSelectionViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val localDataStore: DataStoredInterface,
    val watchesSDK: WatchesSDK
) : BaseViewModel() {
    var fetchData = false
    private val _editMode = MutableLiveData<Boolean>()

    val editMode: LiveData<Boolean>
        get() = _editMode

    private val _disableEditMode = MutableLiveData<Boolean>()

    val disableEditMode: LiveData<Boolean>
        get() = _disableEditMode

    var maxCount = 10

    private val _widgetSortList = MutableLiveData<ArrayList<Widget>>()
    val widgetSortList: LiveData<ArrayList<Widget>>
        get() = _widgetSortList


    private val _noiseFitSearchQuery = MutableLiveData<String>()
    val noiseFitSearchQuery: LiveData<String>
        get() = _noiseFitSearchQuery


    private val _allWidgetSortList = MutableLiveData<ArrayList<Widget>>()
    val allWidgetSortList: LiveData<ArrayList<Widget>>
        get() = _allWidgetSortList


    var count = MutableLiveData(0)


    init {
        _disableEditMode.value = watchesSDK.getWatchType( sessionManager.connectedDevice.value) == SDKWatchType.SDK_RYEEX
        //maxCount = allWidgetSortList.size
    }


    fun setEditMode(mode: Boolean) {
        _editMode.value = mode
    }


    fun setWidgetList(sportList: ArrayList<Widget>) {
        _allWidgetSortList.value?.clear()
        _widgetSortList.value?.clear()
        if (_allWidgetSortList.value.isNullOrEmpty()) {
            _allWidgetSortList.value = ArrayList()
        }
        if (_widgetSortList.value.isNullOrEmpty()) {
            _widgetSortList.value = ArrayList()
        }

        val enableWidgetList = ArrayList<Widget>()
        _allWidgetSortList.value!!.addAll(sportList)
        sportList.forEach { widget ->
            if (widget.isEnable) {
                enableWidgetList.add(widget)

            }
        }
        count.value = enableWidgetList.size
        _widgetSortList.value = enableWidgetList
    }


    fun getSyncWidgetList(): List<Widget> {
        val sportList = ArrayList<Widget>()
        getSelectedWidgetSort().forEach { widget ->
            sportList.add(widget)
        }

        val remainingActivities =
            getRemainingActivities(sportList, getAllWidgetSortList())
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


    fun getAllWidgetSortList(): List<Widget> {
        return _allWidgetSortList.value ?: ArrayList()
    }

    fun getSelectedWidgetSort(): List<Widget> {
        return _widgetSortList.value ?: ArrayList()
    }


    fun updateSelectedWidgetSort(data: Widget) {
        val widget = Widget(
            data.functionId,
            data.name,
            data.haveHide,
            data.isEnable,
            data.order,
            data.sortable
        )
        _widgetSortList.value!!.add(widget)
    }

    fun removeSelectedWidgetSort(data: Widget) {
        _widgetSortList.value?.forEachIndexed { index, sContact ->
            if (sContact.name.lowercase() == data.name.lowercase()) {
                _widgetSortList.value!!.removeAt(index)
                return
            }
        }
        _widgetSortList.value!!.remove(data)
    }

    fun removeSelectedFromAllWidgetSort(data: Widget) {
        _allWidgetSortList.value?.forEach { sport ->
            if (sport.functionId == data.functionId) {
                sport.isEnable = false
                LOGS.d("dasdas $sport")
                return@forEach
            }
        }
        _allWidgetSortList.value = _allWidgetSortList.value

    }

    fun addSelectedCount() {
        if (count.value == null) {
            count.value = 1
        } else {
            val totalCount = count.value!! + 1
            count.value = totalCount
        }
    }

    fun subSelectedCount() {
        if (count.value == null || count.value!! <= 0) {
            count.value = 0
        } else {
            val totalCount = count.value!! - 1
            count.value = totalCount
        }
    }


}