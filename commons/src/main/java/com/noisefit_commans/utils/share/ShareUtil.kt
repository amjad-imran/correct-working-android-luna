package com.noisefit_commans.utils.share

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object ShareUtil {

    const val PACKAGE_TRACK = "com.crrepa.band.noise"
    const val PACKAGE_EVOLVE = "com.noisefit.peak.linkto.sports"
    const val PACKAGE_ASSIST = "com.ido.noise"
    const val PACKAGE_SYNC = "com.zjw.noisefitsync"
    const val PACKAGE_PRIME = "com.noisefit.prime"
    const val PACKAGE_ACE = "com.noise.fit.ace"
    const val PACKAGE_APEX = "com.yc.noisefit"

    fun shareOnFacebook(context: Context, imageUri: Uri?) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.setPackage("com.facebook.katana")
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        whatsappIntent.type = "image/jpeg"
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            // context.showShortToast("Facebook Not installed")
            openPlayStore(context, "com.facebook.katana")
        }
    }

    fun shareOnInsta(context: Context, imageUri: Uri?) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.setPackage("com.instagram.android")
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        whatsappIntent.type = "image/jpeg"
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            // context.showShortToast("Instagram Not installed")
            openPlayStore(context, "com.instagram.android")
        }
    }

    fun shareOnWhatsapp(context: Context, imageUri: Uri?) {
        val whatsappIntent = Intent(Intent.ACTION_SEND)
        whatsappIntent.setPackage("com.whatsapp")
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
        whatsappIntent.type = "image/jpeg"
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(whatsappIntent)
        } catch (ex: ActivityNotFoundException) {
            //   context.showShortToast("Whatsapp Not installed")
            openPlayStore(context, "com.whatsapp")
        }
    }

    fun shareOthers(context: Context, uri: Uri?) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/*"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(share, "Share Image"))
    }

    fun shareText(context: Context, msg: String) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_TEXT, msg)
        context.startActivity(Intent.createChooser(share, "Invite With"))
    }

    fun shareCrashLog(context: Context, content: String?) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_TEXT, content)
        context.startActivity(Intent.createChooser(share, "Share Using"))
    }

    fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    fun openAppPermissionSettings(context: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        context?.startActivity(intent)
    }
}