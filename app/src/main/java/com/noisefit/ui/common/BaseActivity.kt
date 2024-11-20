package com.noisefit.ui.common

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.noisefit.NoiseFitApplicationMain
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR_TIMEOUT
import com.noisefit.data.remote.NetworkErrors.NETWORK_ERROR_UNKNOWN
import com.noisefit.data.remote.NetworkErrors.WRONG_CLIENT_TIME_ERROR
import com.noisefit.luna.R
import com.noisefit.luna.databinding.LayoutCustomWatchConnectedAlertBinding
import com.noisefit_commans.data.BinaryActionCallback
import com.noisefit_commans.data.DialogInputCaptureCallback
import com.noisefit_commans.data.ErrorResponse
import com.noisefit_commans.data.SingleActionCallback
import com.noisefit_commans.data.SnackbarUndoCallback
import com.noisefit_commans.data.TodoCallback
import com.noisefit_commans.data.TrinaryActionCallback
import com.noisefit_commans.data.UIComponentType
import com.noisefit_commans.databinding.DefaultLoaderBinding
import com.noisefit_commans.databinding.LayoutCustomAlertBinding
import com.noisefit_commans.ui.MessageDisplayMode
import com.noisefit_commans.ui.UIController
import com.noisefit_commans.ui.displayToast
import com.noisefit_commans.ui.gone
import com.noisefit_commans.ui.visible
import com.noisefit_commans.utils.DateFormats
import com.oreo.util.language.LocaleHelper


abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), UIController {

    lateinit var binding: VB
    private var dialogInView: AlertDialog? = null
    private var mProgressDialog: ProgressDialog? = null
    var progressBar: DefaultLoaderBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        progressBar = setLoadingView()
        LocaleHelper.setLocale(this, NoiseFitApplicationMain.appLanguage.languageCode)
        setContentView(binding.root)
        observeSubscriber()
        initListener()
    }


    abstract fun initListener()
    abstract fun observeSubscriber()

    abstract fun getViewBinding(): VB

    abstract fun setLoadingView()
            : DefaultLoaderBinding?

    override fun onDisplayError(message: String, messageMode: MessageDisplayMode) {
        when (messageMode) {
            MessageDisplayMode.SNACK_BAR -> displaySnackBarDefault(message)
            MessageDisplayMode.TOAST -> displayToast(message)
        }
    }

//    fun connectionService(action: Actions) {
//        //if (localDataStore.getConnectedDevice() == null) return
//        //if (localDataStore.getServiceState() == ServiceState.STOPPED && action == Actions.STOP) return
//        Intent(this, ConnectionService::class.java).also {
//            it.action = action.name
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                LOGS.i("Starting the service in >=26 Mode")
//                ContextCompat.startForegroundService(this, it)
//                return
//            }
//            LOGS.i("Starting the service in < 26 Mode")
//            startService(it)
//        }
//    }


    override fun onApiErrorReceived(response: ErrorResponse) {
        when (response.uiComponentType) {
            is UIComponentType.AreYouSureDialog -> {
                displayAreYouSureDialog(
                    (response.uiComponentType as UIComponentType.AreYouSureDialog).title,
                    (response.uiComponentType as UIComponentType.AreYouSureDialog).message,
                    (response.uiComponentType as UIComponentType.AreYouSureDialog).doNotShow,
                    (response.uiComponentType as UIComponentType.AreYouSureDialog).ctaText,
                    (response.uiComponentType as UIComponentType.AreYouSureDialog).callback,
                )
            }

            is UIComponentType.Dialog -> {
            }

            is UIComponentType.None -> {
            }

            is UIComponentType.RetryApiDialog -> {
                var showWrongDialog = false
                var message = (response.uiComponentType as UIComponentType.RetryApiDialog).message
                if (message == null) {
                    message = getString(R.string.text_error_connecting_to_internet_retry)
                } else if (message.equals(NETWORK_ERROR, true) ||
                    message.equals(NETWORK_ERROR_TIMEOUT, true) ||
                    message.equals(NETWORK_ERROR_UNKNOWN, true)
                ) {
                    message = getString(R.string.text_error_connecting_to_internet_retry)
                } else if (message.equals(WRONG_CLIENT_TIME_ERROR, true)) {
                    message = getString(R.string.text_wrong_client_time)
                    showWrongDialog = true
                }

                if (showWrongDialog) {
                    showWrongTimeDialog()
                } else {
                    showRetryDialog(
                        getString(R.string.text_failed),
                        message,
                        (response.uiComponentType as UIComponentType.RetryApiDialog).callback
                    )
                }

            }

            is UIComponentType.SnackBar -> {
            }

            is UIComponentType.Toast -> {
            }

            is UIComponentType.CustomAlertDialog -> {
                customInfoDialog(
                    (response.uiComponentType as UIComponentType.CustomAlertDialog).title,
                    (response.uiComponentType as UIComponentType.CustomAlertDialog).message,
                    (response.uiComponentType as UIComponentType.CustomAlertDialog).doNotShow,
                    (response.uiComponentType as UIComponentType.CustomAlertDialog).ctaText,
                    (response.uiComponentType as UIComponentType.CustomAlertDialog).callback
                )
            }

            is UIComponentType.InfoAlertDialog -> {
                showInfoDialog(
                    (response.uiComponentType as UIComponentType.InfoAlertDialog).title,
                    (response.uiComponentType as UIComponentType.InfoAlertDialog).message,
                    (response.uiComponentType as UIComponentType.InfoAlertDialog).ctaText,
                    (response.uiComponentType as UIComponentType.InfoAlertDialog).callback
                )
            }

            is UIComponentType.InfoWatchConnectedAlertDialog -> {
                showWatchConnectedInfoDialog(
                    (response.uiComponentType as UIComponentType.InfoWatchConnectedAlertDialog).title,
                    (response.uiComponentType as UIComponentType.InfoWatchConnectedAlertDialog).message,
                    (response.uiComponentType as UIComponentType.InfoWatchConnectedAlertDialog).callback
                )
            }

            is UIComponentType.WrongTimeDialog -> {
                showInfoDialog(
                    (response.uiComponentType as UIComponentType.WrongTimeDialog).message ?: "",
                    "",
                    "",
                    null
                )
            }
        }


    }

    override fun onResume() {
        super.onResume()
        wrongTimeDialog?.dismiss()
    }


    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?
    ) {
//        val snackbar = Snackbar.make(
//            findViewById(R.id.container),
//            message,
//            Snackbar.LENGTH_LONG
//        )
//        snackbar.setAction(
//            getString(R.string.text_undo),
//            SnackbarUndoListener(snackbarUndoCallback)
//        )
//        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
//            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
//                onDismissCallback?.execute()
//            }
//        })
//        snackbar.show()
    }

    fun displaySnackBarDefault(
        message: String,
    ) {
//        val snackBar = Snackbar.make(
//            findViewById(R.id.container),
//            message,
//            Snackbar.LENGTH_LONG
//        )
//
//        val view = snackBar.view
//        val params = view.layoutParams as FrameLayout.LayoutParams
//        params.gravity = Gravity.BOTTOM
//        view.layoutParams = params
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            view.setBackgroundColor(getColor(R.color.errorRed))
//        }
//        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
//        snackBar.show()
    }

    private fun showLoading(title: String) {
        progressBar?.root?.visible()
    }


    private fun hideLoading() {
        progressBar?.root?.gone()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressBar?.root?.gone()
    }

    override fun displayProgressBar(isDisplayed: Boolean, title: String) {
        if (isDisplayed) {
            showLoading(title)
        } else {
            hideLoading()
        }
    }

    override fun hideSoftKeyboard() {
        window.decorView.windowToken
        val inputMethodManager = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager
            .hideSoftInputFromWindow(window.decorView.windowToken, 0)

    }

    override fun showSoftKeyboard(view: View?) {
        val inputMethodManager = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        inputMethodManager
            .toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback) {

    }

    override fun onPause() {
        super.onPause()
        if (dialogInView != null) {
            (dialogInView as AlertDialog).dismiss()
            dialogInView = null
        }
    }

    private fun displaySuccessDialog(
        message: String?
    ): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.text_success))
            .setCancelable(false)
            .setMessage(message)
            .setNegativeButton(resources.getString(R.string.text_dismiss)) { dialog, which ->
                dialog.dismiss()
                dialogInView = null
            }
            .setPositiveButton(resources.getString(R.string.text_ok)) { dialog, which ->
                dialog.dismiss()
                dialogInView = null
                // Respond to positive button press
            }
            .show()

    }


    private fun displayAreYouSureDialog(
        title: String,
        message: String?,
        doNotShow: Boolean,
        ctaText: String?,
        callback: BinaryActionCallback,

        ): AlertDialog {
        var alert: AlertDialog? = null
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )
        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            btnAllow.text = ctaText
            if (doNotShow) {
                btnDoNotShowAgain.visible()
            }

            btnAllow.setOnClickListener {
                alert?.dismiss()
                callback.yes()
            }
            btnCancel.setOnClickListener {
                alert?.dismiss()
                callback.no()
            }
        }
        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)
        alert = builder.create()
        alert.show()
        return alert

    }

    var wrongTimeDialog: AlertDialog? = null

    private fun showWrongTimeDialog() {
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)

        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )
        layoutCustomAlertBinding.apply {
            val desc =
                "Your phone date is inaccurate! Adjust your clock and try again \n\nYour phone date and time is: ${
                    DateFormats.getDate(DateFormats.dateTimeFormat())
                }"
            tvTitle.text = getString(R.string.text_wrong_time)
            tvDesc.text = desc
            btnAllow.text = getString(R.string.text_adjust_date)
            btnAllow.setOnClickListener {
                wrongTimeDialog?.dismiss()
                val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                startActivity(intent)
            }
            btnCancel.gone()

        }
        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)
        wrongTimeDialog = builder.create()

        if (!(this as Activity).isFinishing) {
            wrongTimeDialog?.show()
        }
    }

    private fun showRetryDialog(
        title: String,
        message: String?,
        callback: BinaryActionCallback?
    ): AlertDialog {
        var alert: AlertDialog? = null
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)

        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )
        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            btnAllow.text = getString(R.string.text_yes)
            btnAllow.setOnClickListener {
                alert?.dismiss()
                callback?.yes()
            }
            btnCancel.setOnClickListener {
                alert?.dismiss()
                callback?.no()
            }
        }
        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)
        alert = builder.create()

        if (!(this as Activity).isFinishing) {
            alert.show()
        }
        return alert

    }

    private fun showInfoDialog(
        title: String,
        message: String?,
        ctaText: String,
        callback: SingleActionCallback?
    ): AlertDialog {
        var alert: AlertDialog? = null
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )
        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            btnAllow.text = ctaText
            btnCancel.visibility = View.GONE
            btnAllow.setOnClickListener {
                alert?.dismiss()
                callback?.onClicked()
                dialogInView = null
            }

        }
        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)

        alert = builder.create()
        alert.show()
        return alert
    }

    private fun showWatchConnectedInfoDialog(
        title: String,
        message: String?,
        callback: SingleActionCallback?
    ): AlertDialog {
        var alert: AlertDialog? = null
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomWatchConnectedAlertBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(this),
                R.layout.layout_custom_watch_connected_alert, null, false
            )
        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            imgClose.setOnClickListener {
                alert?.dismiss()
                callback?.onClicked()
                dialogInView = null
            }

        }
        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)

        alert = builder.create()
        alert.show()
        return alert
    }


    private fun customInfoDialog(
        title: String,
        message: String?,
        doNotShow: Boolean,
        ctaText: String,
        callback: TrinaryActionCallback?
    ): AlertDialog {
        var alert: AlertDialog? = null
        val builder = MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_rounded)
        val layoutCustomAlertBinding: LayoutCustomAlertBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this),
            com.noisefit_commans.R.layout.layout_custom_alert, null, false
        )

        layoutCustomAlertBinding.apply {
            tvTitle.text = title
            tvDesc.text = message
            btnAllow.text = ctaText
            if (doNotShow) {
                btnDoNotShowAgain.visible()
            }

            btnDoNotShowAgain.setOnClickListener {
                alert?.dismiss()
                callback?.maybe()
                dialogInView = null
            }

            btnAllow.setOnClickListener {
                alert?.dismiss()
                callback?.yes()
                dialogInView = null
            }
            btnCancel.setOnClickListener {
                alert?.dismiss()
                callback?.no()
                dialogInView = null
            }
        }


        builder.setView(layoutCustomAlertBinding.root)
        builder.setCancelable(false)

        alert = builder.create()
        alert.show()
        return alert
    }

    fun hasPermissions(permissionList: List<String>): Boolean {
        permissionList.forEach {
            if (ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun attachBaseContext(base: Context?) {
        base?.let {
            super.attachBaseContext(LocaleHelper.onAttach(it))
        }
    }

}

