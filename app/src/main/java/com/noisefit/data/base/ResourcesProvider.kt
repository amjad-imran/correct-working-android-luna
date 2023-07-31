package com.noisefit.data.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesProvider
@Inject
constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(@StringRes stringResId: Int): String {
        return context.getString(stringResId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(stringResId: Int): Drawable? {
        return context.getDrawable(stringResId)
    }
}