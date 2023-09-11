package com.noisefit.data.repository.implementation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.text.TextUtils
import com.google.gson.JsonObject
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.remote.abstraction.NetworkService
import com.noisefit.data.remote.base.Resource
import com.noisefit.data.remote.response.Watchface2
import com.noisefit.data.repository.LastSyncProvider
import com.noisefit.data.repository.abstraction.DeviceRepository
import com.noisefit.data.safeApiCallFlow
import com.noisefit.luna.BuildConfig
import com.noisefit_commans.data.model.Feedback
import com.noisefit_commans.data.model.FeedbackNew
import com.noisefit_commans.data.model.NotificationApp
import com.noisefit_commans.data.model.warranty.MarketPlace
import com.noisefit_commans.data.response.*
import com.noisefit_commans.enums.ApplicationType
import com.noisefit_commans.models.Contact
import com.noisefit_commans.ui.getRequestBody
import com.noisefit_commans.ui.numberWithSTDCode
import com.noisefit_commans.ui.onlyNumber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class DeviceRepositoryImpl(
    private val remoteDataSource: NetworkService,
    private val lastSyncProvider: LastSyncProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : DeviceRepository {


    override suspend fun getDeviceList(dType: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DeviceListResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getDeviceList(
                "${BuildConfig.BASE_URL_NEW}/user_detail/ring/devices/list",
                dType
            )
        }
    }

    override suspend fun getDeviceFeature(deviceId: Int): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<DeviceFeatureResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getDeviceFeatures(
                "${BuildConfig.BASE_URL_NEW}/user_detail/ring/device_features",
                deviceId,
                "android"
            )
        }
    }

    private val continuePackageName = arrayListOf(
        //自己
        // AppUtils.getAppPackageName(),
        //电话
        "com.oneplus.dialer",
        "com.android.incallui",
        "com.android.phone",
        "com.samsung.android.dialer",
        "com.google.android.dialer",
        //拨号
        "com.android.contacts",
        "cn.nubia.contacts",
        //短信
        "com.android.mms",
        "com.android.mms.service",
        "com.oneplus.mms",
        "com.samsung.android.messaging",
        "cn.nubia.mms",
        "com.google.android.apps.messaging"
    )

    override suspend fun getInstalledApps(
        selectedNotificationApp: List<NotificationApp>,
        supportedNotificationApp: List<NotificationApp>
    ): Flow<Pair<ArrayList<NotificationApp>, ArrayList<NotificationApp>>> {
        return flow {

            val installedAppList = getInstalledAppWithStatus(selectedNotificationApp)

            val sortedList = installedAppList.sortedWith(compareBy { it.appDisplayName })

            val recommendedNotificationList = ArrayList<NotificationApp>()
            val allNotificationList = ArrayList<NotificationApp>()
            sortedList.forEach { notificationApp ->
                allNotificationList.add(notificationApp)

                val pExist = supportedNotificationApp.filter {
                    it.appPackageName == notificationApp.appPackageName
                }

                if (pExist.isNotEmpty()) {
                    recommendedNotificationList.add(notificationApp)
                }
            }

            emit(Pair(recommendedNotificationList, allNotificationList))
        }
    }

    private fun getInstalledAppWithStatus(selectedNotificationApp: List<NotificationApp>): ArrayList<NotificationApp> {
        val installedAppList: ArrayList<NotificationApp> = ArrayList()
        val context = NoiseFitApplicationMain.context!!
        val pm = context.packageManager
        //获取电话包名加入过滤包名列表
        var infoIntent = Intent(Intent.ACTION_DIAL)
        pm.resolveActivity(infoIntent, 0)?.let {
//                LOGS.e("ACTION_CALL", it.activityInfo.packageName)
            if (it.activityInfo != null) {
                val pkName = it.activityInfo.packageName
                if (!TextUtils.isEmpty(pkName)) {
                    if (!continuePackageName.contains(pkName)) {
                        continuePackageName.add(it.activityInfo.packageName)
                    }
                }
            }
        }
        //获取短信包名加入过滤包名列表
        val uri: Uri = Uri.parse("smsto:10086")
        infoIntent = Intent(Intent.ACTION_SENDTO, uri)
        pm.resolveActivity(infoIntent, 0)?.let {
//                LOGS.e("ACTION_CALL ACTION_MMS", it.activityInfo.packageName)
            if (it.activityInfo != null) {
                val pkName = it.activityInfo.packageName
                if (!TextUtils.isEmpty(pkName)) {
                    if (!continuePackageName.contains(pkName)) {
                        continuePackageName.add(it.activityInfo.packageName)
                    }
                }
            }
        }
        //获取所有已安装应用
        val resolveIntent = Intent(Intent.ACTION_MAIN, null)
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val rList: List<ResolveInfo> = pm.queryIntentActivities(resolveIntent, 0)
        for (r in rList) {
            val packageName = r.activityInfo.packageName
//                LOGS.d("ACTION_CALL $packageName----" + r.loadLabel(pm).toString())
            if (r.activityInfo != null && packageName != null) {
                //过滤特殊包名
                if (continuePackageName.contains(packageName)) {
                    continue
                }


                val appName = r.loadLabel(pm).toString()
                //   val icon = r.icon
                val watchSupported = false
                var enabled = false

                val pExist = selectedNotificationApp.filter {
                    it.appPackageName == packageName
                }

                if (pExist.isNotEmpty()) {
                    enabled = true
                }


                val imageDrawable = packageName.let {
                    NoiseFitApplicationMain.context!!.packageManager
                        .getApplicationIcon(it)
                }

                val notificationApp = NotificationApp(
                    1,
                    ApplicationType.NONE,
                    appName,
                    r.activityInfo.packageName,
                    enabled,
                    watchSupported,
                )
                notificationApp.imageDrawable = imageDrawable
                installedAppList.add(
                    notificationApp
                )

            }
        }

        return installedAppList
    }

    override suspend fun enableInstalledAppsNotification(selectedNotificationApp: List<NotificationApp>): Flow<Triple<Boolean, Boolean, List<NotificationApp>>> {
        return flow {

            val willEnableNotificationsList = ArrayList<NotificationApp>()
            var isCallEnabled = false
            var isSmsEnabled = false

            val installedAppList = getInstalledAppWithStatus(selectedNotificationApp)
            //LOGS.d("enableInstalledAppsNotification ${Gson().toJson(selectedNotificationApp)}")

            val callPermissionFound = selectedNotificationApp.filter {
                it.isEnabled && it.appCode.type.equals(ApplicationType.CALL.type, true)
            }

            if (callPermissionFound.isNotEmpty()) {
                isCallEnabled = true
            }


            val smsPermissionFound = selectedNotificationApp.filter {
                it.isEnabled && it.appCode.type.equals(ApplicationType.SMS.type, true)
            }

            if (smsPermissionFound.isNotEmpty()) {
                isSmsEnabled = true
            }




            installedAppList.forEach { notificationApp ->
                val appFound = selectedNotificationApp.filter {
                    if (it.appDisplayName.equals(
                            "Whatsapp Business",
                            true
                        ) && notificationApp.appDisplayName.equals("WA Business", true)
                    ) {
                        true
                    } else {
                        it.appDisplayName.equals(notificationApp.appDisplayName, true)
                    }

                }


                if (appFound.isNotEmpty()) {
                    notificationApp.isEnabled = true
                    willEnableNotificationsList.add(notificationApp)
                }

            }
            //  LOGS.d("enableInstalledAppsNotification ${Gson().toJson(willEnableNotificationsList)}")
            //LOGS.d("enableInstalledAppsNotification $isCallEnabled $isSmsEnabled")
            emit(Triple(isCallEnabled, isSmsEnabled, willEnableNotificationsList))
        }
    }

    @SuppressLint("Range")
    override suspend fun getSaveContactInfo(
        selectedContactList: ArrayList<Contact>,
        supportedStpCode: Boolean
    ): Flow<List<Contact>> {
        return flow {

            val contactList = ArrayList<Contact>()

            val contactsNumberMap = HashMap<String, Contact>()
            val cursor: Cursor = NoiseFitApplicationMain.context?.contentResolver
                ?.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null,
                    null,
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )!!
            val contactsCount = cursor.count // get how many contacts you have in your contacts list
            if (contactsCount > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val contactName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt() > 0
                    ) {
                        //the below cursor will give you details for multiple contacts
                        val pCursor: Cursor =
                            NoiseFitApplicationMain.context?.contentResolver?.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(id),
                                null
                            )!!
                        // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
                        while (pCursor.moveToNext()) {
                            val photoUri =
                                pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
//                            //String isStarred        = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));


                            val phoneNo = if (supportedStpCode) {
                                try {
                                    pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        .replace(" ", "")
                                        .numberWithSTDCode()
                                } catch (exp: NullPointerException) {
                                    ""
                                }
                            } else {
                                try {
                                    pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        .replace(" ", "")
                                        .replace("+91", "")
                                        .onlyNumber()
                                } catch (exp: NullPointerException) {
                                    ""
                                }
                            }

//

                            //  LOGS.d("photoUri $phoneNo $photoUri")

                            if (phoneNo.isNotEmpty()) {
                                if (contactsNumberMap.containsKey(phoneNo)) {
                                    val mContact = contactsNumberMap[phoneNo]
                                    mContact?.number?.add(phoneNo)
                                    contactsNumberMap[phoneNo] = mContact!!
                                } else {
                                    val mContact = Contact()
                                    mContact.id = phoneNo
                                    mContact.photoUri = photoUri
                                    mContact.name = contactName
                                    mContact.number.add(phoneNo)
                                    contactsNumberMap[phoneNo] = mContact
                                }
                            }
                        }
                        pCursor.close()
                    }
                }
                cursor.close()
            }

            for ((_, contact) in contactsNumberMap) {
                val contactFound = selectedContactList.filter {
                    it.number[0] == contact.number[0]
                }
                if (!contactFound.isNullOrEmpty()) {
                    contact.selected = true
                }
                contactList.add(contact)
            }
            val sortedList = contactList.sortedWith(compareBy { it.name })
            emit(sortedList)
        }
    }

    @SuppressLint("Range")
    override suspend fun getOnlyNumber(
        selectedContactList: ArrayList<Contact>,
        supportedStpCode: Boolean
    ): Flow<HashSet<String>> {
        return flow {

            val contactList = HashSet<String>()

            //  val contactList1 = HashSet<String>()

            val phones: Cursor? = NoiseFitApplicationMain.context?.contentResolver?.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            )
            while (phones!!.moveToNext()) {
                val contactName =
                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//                val contactNumber =
//                    phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                val phoneNo = if (supportedStpCode) {
                    try {
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace(" ", "")
                            .numberWithSTDCode()
                    } catch (exp: NullPointerException) {
                        ""
                    }
                } else {
                    try {
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            .replace(" ", "")
                            .replace("+91", "")
                            .onlyNumber()
                    } catch (exp: NullPointerException) {
                        ""
                    }
                }
                contactList.add(phoneNo)
            }
            phones.close()

            emit(contactList)
        }
    }

    override suspend fun getRecentWatchFace(): Flow<Resource<BaseApiResponse<List<Watchface2>?>>> {
        return safeApiCallFlow(dispatcher) {
            val url = BuildConfig.BASE_URL_NEW + "/watch_faces/v2/random_list"
            remoteDataSource.getRecentWatchFace(url)
        }
    }

    override suspend fun checkForUpdates(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/core/ring/firmware_versions"
            remoteDataSource.checkForUpdates(url, requestObject)
        }
    }

    override suspend fun checkForUpdatesRecent(requestObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<UpdateResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.checkForUpdatesRecent(requestObject)
        }
    }

    override suspend fun getAgpsFileUrl(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AgpsFileResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getAgpsFileUrl()
        }
    }

    override suspend fun getVendorAgpsFileUrl(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<com.noisefit_commans.data.response.AgpsFileResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/master/devices/link"
            remoteDataSource.getVendorAgpsFileUrl(url)
        }
    }

    override suspend fun checkWarranty(number: String): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<WarrantyResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.checkWarranty(number)
        }
    }

    override suspend fun getMarketPlaces(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<MarketPlace>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NOISE_CONNECT}/complaint/channels"
            remoteDataSource.getMarketPlaces(url)
        }
    }

    override suspend fun checkWatchTokenExist(macAddress: String): Flow<Resource<BaseApiResponse<WatchTokenResponse>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/user_detail/ring/ring-token"
            val requestObject = JsonObject()
            requestObject.addProperty("address", macAddress)
            remoteDataSource.checkWatchTokenExist(url, requestObject)
        }
    }

    override suspend fun removeWatchTokenFromServer(
        macAddress: String
    ): Flow<Resource<BaseApiResponseData<Any>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NEW}/user_detail/ring/ring-token/remove"
            val requestObject = JsonObject()
            requestObject.addProperty("address", macAddress)
            remoteDataSource.removeWatchTokenFromServer(url, requestObject)
        }
    }

    override suspend fun getMarketPlacesOld(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<String>>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.getMarketPlacesOld()
        }
    }

    override suspend fun addWarrantyOld(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.addWarrantyOld(jsonObject)
        }
    }

    override suspend fun getWarrantyWatchList(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<List<WarrantyWatchesResponse>>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NOISE_CONNECT}/product/getproductBycategory"
            remoteDataSource.getWarrantyWatchList(url, "d8f021fd65e25982003ddce3cac2225e")
        }
    }

    override suspend fun addWarranty(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<String?>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NOISE_CONNECT}/warranty/registration"
            remoteDataSource.addWarranty(url, BuildConfig.NOISE_CONNECT_KEY, jsonObject)
        }
    }

    override suspend fun checkWarranty(jsonObject: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<Boolean>>> {
        return safeApiCallFlow(dispatcher) {
            val url = "${BuildConfig.BASE_URL_NOISE_CONNECT}/warranty/check-info"
            remoteDataSource.checkWarranty(url, BuildConfig.NOISE_CONNECT_KEY, jsonObject)
        }
    }

    override suspend fun getConfig(): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<ConfigResponse>>> {
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.config("${BuildConfig.BASE_URL_NEW}/master/app_version/config")
        }
    }


    override suspend fun submitFeedback(
        feedback: Feedback,
        deviceId: Int?
    ): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponse<MessageResponse>>> {
        val imageList = ArrayList<MultipartBody.Part>()
        feedback.screenShortList.forEach { uri ->
            val file = File(uri.path)
            //val requestFile :RequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val requestFile = MultipartBody.Part.createFormData(
                "images",
                file.name,
                file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            imageList.add(requestFile)
        }

        val logList = ArrayList<MultipartBody.Part>()
        if (feedback.file != null) {
            logList.add(
                MultipartBody.Part.createFormData(
                    "log",
                    "appLogs.txt"/*feedback.file!!.name*/,
                    feedback.file!!.asRequestBody("text/plain".toMediaTypeOrNull())
                )
            )
        }
        if (feedback.watchLogs != null) {

            var filename = feedback.watchLogs?.name
            if (filename.isNullOrEmpty()) {
                filename = "watchLogs.txt"
            }
            logList.add(
                MultipartBody.Part.createFormData(
                    "log",
                    filename/*feedback.watchLogs!!.name*/,
                    feedback.watchLogs!!.asRequestBody("text/plain".toMediaTypeOrNull())
                )
            )
        }


        return safeApiCallFlow(dispatcher) {
            remoteDataSource.submitFeedback(
                deviceId,
                feedback.platform.getRequestBody(),
                feedback.mobileDevice.getRequestBody(),
                feedback.osVersion.getRequestBody(),
                feedback.appVersion.getRequestBody(),
                feedback.watchName.getRequestBody(),
                feedback.watchFirmwareVersion.getRequestBody(),
                feedback.problemType.getRequestBody(),
                feedback.problemDesc.getRequestBody(),
                feedback.date.getRequestBody(),
                feedback.user_id.toString().getRequestBody(),
                imageList,
                logList
            )
        }

    }

    override suspend fun submitFeedbackNew(feedback: JsonObject): Flow<Resource<com.noisefit_commans.data.response.BaseApiResponseData<String>>> {
        val url =
            "${BuildConfig.BASE_URL_NEW}/core/ring/help_and_support/feedback"
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.submitFeedbackNew(url, feedback)
        }
    }

    override suspend fun submitFeedbackFile(feedback: FeedbackNew): Flow<Resource<BaseApiResponseData<String>>> {

        val logList = ArrayList<MultipartBody.Part>()
        if (feedback.file != null) {
            logList.add(
                MultipartBody.Part.createFormData(
                    "logs",
                    "appLogs.txt"/*feedback.file!!.name*/,
                    feedback.file!!.asRequestBody("text/plain".toMediaTypeOrNull())
                )
            )
        }
        if (feedback.watchLogs != null) {

            var filename = feedback.watchLogs?.name
            if (filename.isNullOrEmpty()) {
                filename = "watchLogs.txt"
            }
            logList.add(
                MultipartBody.Part.createFormData(
                    "logs",
                    filename/*feedback.watchLogs!!.name*/,
                    feedback.watchLogs!!.asRequestBody("text/plain".toMediaTypeOrNull())
                )
            )
        }


        val url =
            "${BuildConfig.BASE_URL_NEW}/core/ring/help_and_support/log_feedback"
        return safeApiCallFlow(dispatcher) {
            remoteDataSource.submitFeedbackFile(
                url,
                platform = feedback.platform.getRequestBody(),
                mobileDevice = feedback.mobileDevice.getRequestBody(),
                osVersion = feedback.osVersion.getRequestBody(),
                appVersion = feedback.appVersion.getRequestBody(),
                watchName = feedback.watchName.getRequestBody(),
                watchFirmwareVersion = feedback.watchFirmwareVersion.getRequestBody(),
                rating = feedback.rating.toString().getRequestBody(),
                problem_type = feedback.problemType.getRequestBody(),
                suggestion = feedback.suggestions.getRequestBody(),
                date = feedback.date.getRequestBody(),
                userId = feedback.user_id.toString().getRequestBody(),
                logList
            )
        }
    }

    companion object {
        val DEVICE_LIST_SUCCESS = "Device list success"
        val DEVICE_LIST_FAILED = "Something went wrong!!"

    }


}