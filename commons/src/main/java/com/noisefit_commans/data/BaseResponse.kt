package com.noisefit_commans.data

import android.view.View


data class ErrorResponse(
    val uiComponentType: UIComponentType,
)

sealed class UIComponentType {

    class Toast(val message: String?) : UIComponentType()

    class Dialog : UIComponentType()

    class RetryApiDialog(val message: String?) :
        UIComponentType() {
        var callback: BinaryActionCallback? = null
    }

    class InfoAlertDialog(val title: String, val message: String?, val ctaText: String) :
        UIComponentType() {
        var callback: SingleActionCallback? = null
    }
    class InfoWatchConnectedAlertDialog(val title: String, val message: String?, val ctaText: String) :
        UIComponentType() {
        var callback: SingleActionCallback? = null
    }

    class CustomAlertDialog(
        val title: String,
        val message: String?,
        val doNotShow: Boolean,
        val ctaText: String,
        var callback: TrinaryActionCallback
    ) : UIComponentType()

    class AreYouSureDialog(
        val title: String,
        val message: String?,
        val doNotShow: Boolean,
        val ctaText: String?,
        val callback: BinaryActionCallback,
    ) : UIComponentType()

    class SnackBar(
        val undoCallback: SnackbarUndoCallback? = null,
        val onDismissCallback: TodoCallback? = null
    ) : UIComponentType()

    class None : UIComponentType()
}


interface BinaryActionCallback {

    fun yes()

    fun no()
}

interface TrinaryActionCallback {

    fun yes()

    fun maybe()

    fun no()
}

interface SingleActionCallback {
    fun onClicked()
}

interface SnackbarUndoCallback {

    fun undo()
}

class SnackbarUndoListener
constructor(
    private val snackbarUndoCallback: SnackbarUndoCallback?
) : View.OnClickListener {

    override fun onClick(v: View?) {
        snackbarUndoCallback?.undo()
    }

}


interface DialogInputCaptureCallback {

    fun onTextCaptured(text: String)
}

interface TodoCallback {

    fun execute()
}
/*
response?.response?.messageType?.let {
                when (it) {

                    is MessageType.Error -> {
                        LOGS.d("Error")
                    }
                    is MessageType.Success -> {
                        LOGS.d("Success")
                    }
                }

            }
 */