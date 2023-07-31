package com.noisefit_commans.utils.autostart

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import javax.inject.Inject
import com.google.android.material.dialog.MaterialAlertDialogBuilder



class AutoStartUtil
@Inject
constructor(){

    private val AUTOSTART_INTENTS = arrayOf(
        Intent().setComponent(
            ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        ),  //MIUI
        Intent().setComponent(
            ComponentName(
                "com.letv.android.letvsafe",
                "com.letv.android.letvsafe.AutobootManageActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.optimize.process.ProtectActivity"
            )
        ),
        //oppo
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.privacypermissionsentry.PermissionTopActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        ), Intent().setComponent(
            ComponentName(
                "com.oppo.safe",
                "com.oppo.safe.permission.startup.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.iqoo.secure",
                "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        ),
        Intent().setComponent(
            ComponentName(
                "com.asus.mobilemanager",
                "com.asus.mobilemanager.entry.FunctionActivity"
            )
        ).setData(Uri.parse("mobilemanager://function/entry/AutoStart")),
        Intent().setComponent(
            ComponentName(
                "com.samsung.android.lool",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        )
    )


    fun startAutostartSettings(context: Context) {
        try {
            for (intent in AUTOSTART_INTENTS) {
                if (context.packageManager.resolveActivity(
                        intent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    ) != null
                ) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    break
                }
            }
        }catch (exp : Exception){
            exp.printStackTrace()
        }
    }

    fun isSupportedAndroid(context: Context): Boolean {
        try {
            for (intent in AUTOSTART_INTENTS) {
                if (context.packageManager
                        .resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null
                ) {
                    return true
                }
            }
        } catch (exp: Exception) {
            return false
        }
        return false
    }


    fun showAlert(context: Context, onClickListener: DialogInterface.OnClickListener) {
        MaterialAlertDialogBuilder(context).setTitle("Allow AutoStart")
            .setMessage("To run the app in background, please enable Auto Start for NoiseFit.")
            .setPositiveButton("ENABLE AUTOSTART", onClickListener).show().setCancelable(false)
    }
}