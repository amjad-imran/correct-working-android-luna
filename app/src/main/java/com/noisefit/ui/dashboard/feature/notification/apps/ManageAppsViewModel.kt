package com.noisefit.ui.dashboard.feature.notification.apps

import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.R
import com.noisefit.data.local.AppStaticData
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.session.SessionManager
import com.noisefit_commans.ui.BaseViewModel
import com.noisefit_commans.ui.showShortToast
import com.noisefit.watch.SDKWatchType
import com.noisefit.watch.WatchesSDK
import com.noisefit_commans.data.local.abstraction.DataStoredInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ManageAppsViewModel
@Inject
constructor(
    private val localDataStore: DataStoredInterface,
    private val deviceRepository: DeviceRepository,
    private val watchesSDK: WatchesSDK,
    val sessionManager: SessionManager
) : BaseViewModel() {
    var notificationListType = 0

    private val _showTabView = MutableLiveData<Boolean>(false)
    val showTabView: LiveData<Boolean>
        get() = _showTabView

    private val _manageNotificationList = MutableLiveData<ArrayList<NotificationApp>>()
    val manageNotificationList: LiveData<ArrayList<NotificationApp>>
        get() = _manageNotificationList

    private val _selectedNotificationList = ArrayList<NotificationApp>()

    private val _allInstalledNotificationList = MutableLiveData<ArrayList<NotificationApp>>()
    val allInstalledNotificationList: LiveData<ArrayList<NotificationApp>>
        get() = _allInstalledNotificationList

    private val _allRecommendedNotificationList = MutableLiveData<ArrayList<NotificationApp>>()
    val allRecommendedNotificationList: LiveData<ArrayList<NotificationApp>>
        get() = _allRecommendedNotificationList

    init {
        if (watchesSDK.getWatchType() != SDKWatchType.SDK_CF_PRO) {
            _showTabView.value = true
        }

    }

    fun isSmartNotificationOn():Boolean{
        return localDataStore.getExperimentalSettings().smartNotification
    }

    fun getSelectedNotificationList(): ArrayList<NotificationApp> {
        return _selectedNotificationList
    }

    fun updateNotification(notificationApp: NotificationApp) {
        _selectedNotificationList.add(notificationApp)
    }


    fun removeSelectedNotification(notificationApp: NotificationApp) {


        _allRecommendedNotificationList.value?.forEachIndexed { index, notification ->
            if (notification.appPackageName == notificationApp.appPackageName) {
                notification.isEnabled = false

                return@forEachIndexed
            }
        }


        _allInstalledNotificationList.value?.forEachIndexed { index, notification ->
            if (notification.appPackageName == notificationApp.appPackageName) {
                notification.isEnabled = false

                return@forEachIndexed
            }
        }
        _selectedNotificationList.forEachIndexed { index, notification ->
            if (notification.appPackageName == notificationApp.appPackageName) {
                _selectedNotificationList.removeAt(index)
                return
            }
        }

    }




    fun removeNotification(notificationApp: NotificationApp) {
        _selectedNotificationList.forEachIndexed { index, notification ->
            if (notification.appPackageName == notificationApp.appPackageName) {
                _selectedNotificationList.removeAt(index)
                return
            }
        }

        _selectedNotificationList.remove(notificationApp)
    }

    fun getLocalDataStore(): DataStoredInterface {
        return localDataStore
    }

    fun feedManageNotification() {
        setLoading(true)
        val notificationList = localDataStore.getNotificationEnabledAppList()
        if (_manageNotificationList.value == null) {
            _manageNotificationList.value = ArrayList()
        }
        _manageNotificationList.value?.clear()
        if (!notificationList.isNullOrEmpty()) {
            setManageNotificationList(ArrayList(notificationList))
        }
        setLoading(false)
    }

    fun addDefaultRecommended() {
        if (_manageNotificationList.value.isNullOrEmpty() && !localDataStore.isDefaultNotificationAdded()) {
            getInstalledApps(ArrayList(), true)
            NoiseFitApplicationMain.context?.apply {
                showShortToast(this.getString(R.string.text_please_wait_looking_for_recommended_notifications))
            }
        }

    }

    private fun setManageNotificationList(notificationList: ArrayList<NotificationApp>) {
        val manageNotList = ArrayList<NotificationApp>()
        notificationList.forEach { notificationApp ->

            if (!notificationApp.appPackageName.isNullOrEmpty()) {

                val isAppInstalled: Boolean = appInstalledOrNot(notificationApp.appPackageName!!)
                if (isAppInstalled) {
                    val imageDrawable = notificationApp.appPackageName?.let {
                        NoiseFitApplicationMain.context!!.packageManager
                            .getApplicationIcon(it)
                    }
                    notificationApp.imageDrawable = imageDrawable

                    manageNotList.add(notificationApp)
                }
            }
        }

        _manageNotificationList.value = manageNotList

    }

    private fun appInstalledOrNot(uri: String): Boolean {

        val pm: PackageManager = NoiseFitApplicationMain.context!!.packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_META_DATA)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    private fun saveNotificationList(notificationList: ArrayList<NotificationApp>) {
        notificationList.forEach {
            it.isEnabled = true
        }
        localDataStore.setDefaultNotificationAdded(true)
        localDataStore.saveNotificationAppList(notificationList)
    }

    fun getInstalledApps(selectedNotificationList: List<NotificationApp>, addDefault: Boolean) {

        _selectedNotificationList.clear()
        _selectedNotificationList.addAll(selectedNotificationList)
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            val supportedNotificationAppList =
                AppStaticData.getNotificationApps(localDataStore.getDeviceFeatures()?.availableNotificationTypes)
            deviceRepository.getInstalledApps(
                selectedNotificationList,
                supportedNotificationAppList
            ).collect { data ->


                if (addDefault) {
                    saveNotificationList(data.first)
                    //TODO: Need to think a better way to optimize below code. All apps icon getting null if we are trying to save in datastore
                    data.first.forEach { notificationApp ->
                        if (!notificationApp.appPackageName.isNullOrEmpty()) {
                            val isAppInstalled: Boolean =
                                appInstalledOrNot(notificationApp.appPackageName!!)
                            if (isAppInstalled) {
                                val imageDrawable = notificationApp.appPackageName.let {
                                    NoiseFitApplicationMain.context!!.packageManager
                                        .getApplicationIcon(it?:"")
                                }
                                notificationApp.imageDrawable = imageDrawable
                                notificationApp.isEnabled = true
                            }
                        }

                    }


                    withContext(Dispatchers.Main) {
                        if (data.first.isNotEmpty()) {
                            NoiseFitApplicationMain.context?.apply {
                                showShortToast(this.getString(R.string.text_recommended_notifications_added_successful))
                            }
                        }
                        _manageNotificationList.value = data.first
                        setLoading(false)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _allInstalledNotificationList.postValue(data.second)
                        _allRecommendedNotificationList.postValue(data.first)
                        setLoading(false)
                    }

                }


            }
        }


    }

    fun setManageAppList(notificationList: ArrayList<NotificationApp>) {

        if (_manageNotificationList.value == null) {
            _manageNotificationList.value = ArrayList()
        }
        _manageNotificationList.value?.clear()
        viewModelScope.launch {
            delay(100)
            setManageNotificationList(ArrayList(notificationList))

        }

        //_manageNotificationList.postValue(notificationList)
        //setManageNotificationList(notificationList)
    }


    fun getManageAppList(): List<NotificationApp> {
        return _manageNotificationList.value ?: ArrayList()
    }

}