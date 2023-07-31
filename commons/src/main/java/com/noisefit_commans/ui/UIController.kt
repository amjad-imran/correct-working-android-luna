package com.noisefit_commans.ui

import android.view.View
import com.noisefit_commans.data.DialogInputCaptureCallback
import com.noisefit_commans.data.ErrorResponse


interface UIController {

    fun displayProgressBar(isDisplayed: Boolean, title: String)

    fun logAppEvent(eventName : String,data : HashMap<String,Any??>)

    fun hideSoftKeyboard()

    fun showSoftKeyboard(view : View?)

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onApiErrorReceived(response: ErrorResponse)

    fun onDisplayError(message: String, messageMode: MessageDisplayMode = MessageDisplayMode.TOAST)

}
enum class MessageDisplayMode {
    SNACK_BAR, TOAST
}