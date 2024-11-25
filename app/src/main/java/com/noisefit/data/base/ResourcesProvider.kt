package com.noisefit.data.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import com.noisefit.NoiseFitApplicationMain
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
        val localizedString = String.format(
            res.getString(stringResId),
            formatArgs
        )
        return localizedString
    }

    //think of some other way
    fun getString(@StringRes stringResId: Int, args1: Any, args2: Any, args3: Any): String {
        val res = getResourcesBasedOnLanguage()
        val localizedString = String.format(
            res.getString(stringResId),
            args1,
            args2,
            args3
        )
        return localizedString
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