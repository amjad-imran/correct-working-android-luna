package com.noisefit.data.base

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import com.noisefit.NoiseFitApplicationMain
import com.noisefit_commans.utils.LOGS
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ResourcesProvider
@Inject
constructor(
    @ApplicationContext val context: Context
) {
    fun getString(@StringRes stringResId: Int): String {
        val res = getResourcesBasedOnLanguage()
        return res.getString(stringResId)
    }

    fun getString(@StringRes stringResId: Int, formatArgs: Any): String {
        val res = getResourcesBasedOnLanguage()
        val string = res.getString(stringResId)
        val localizedString = String.format(
            res.getString(stringResId),
            formatArgs
        )
        return localizedString
        //return res.getString(stringResId, formatArgs)
    }

    fun getResourcesBasedOnLanguage(): Resources {
        val language = NoiseFitApplicationMain.appLanguage.languageCode
        var conf = context.resources.configuration
        conf = Configuration(conf)
        conf.setLocale(Locale(language))
        val localizedContext = context.createConfigurationContext(conf)
        return localizedContext.resources
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(stringResId: Int): Drawable? {
        return context.getDrawable(stringResId)
    }
}