package com.noisefit.receiver.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Matcher
import java.util.regex.Pattern


class OtpBroadcastReceiver : BroadcastReceiver() {
    private var otpReceiveListener: OTPReceiveListener? = null


    fun setListener(otpReceiveListener: OTPReceiveListener?) {
        this.otpReceiveListener = otpReceiveListener
    }


    override fun onReceive(context: Context?, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras: Bundle? = intent.extras
            if (extras != null) {
                val status: Status? = extras.get(SmsRetriever.EXTRA_STATUS) as? Status
                if (status != null) when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as? String
                        if (message != null) {
                            val pattern: Pattern = Pattern.compile("(\\d{4})")
                            val matcher: Matcher = pattern.matcher(message)
                            val otp = if (matcher.find()) {
                                matcher.group(0)
                            } else {
                                null
                            }
                            otpReceiveListener?.onOTPReceived(otp)
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> otpReceiveListener?.onOTPTimeOut()
                }
            }
        }
    }
}

interface OTPReceiveListener {
    fun onOTPReceived(otp: String?)
    fun onOTPTimeOut()
}