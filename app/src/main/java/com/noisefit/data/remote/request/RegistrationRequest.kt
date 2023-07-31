package com.noisefit.data.remote.request

import android.os.Parcelable
import com.noisefit_commans.utils.StringUtils.isValidEmail
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegistrationRequest(
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val country_code: String? = null,
    val password: String? = null,
    val login_type: String? = null,
    val image_url: String? = null,
    val push_token: String? = null,
    val fb_uuid: String? = null,
    val fb_access_token: String? = null,
    val google_uid: String? = null,
    val google_token: String? = null
) : Parcelable {

    fun validateEmailData(): String? {
        if (this.first_name.isNullOrEmpty() || this.first_name == "null") {
            return "First name is required"
        }

        if (this.last_name.isNullOrEmpty() || this.last_name == "null") {
            return "Last name is required"
        }

        if (this.email.isNullOrEmpty() || this.email == "null") {
            return "Email is required"
        }

        if (!this.email.isValidEmail()) {
            return "Please enter a valid email address"
        }

        if (this.mobile.isNullOrEmpty() || this.mobile == "null" || this.mobile.contains("null")) {
            return "Phone number is required"
        }

        if (this.password.isNullOrEmpty() || this.password == "null") {
            return "Password is required"
        }

        if (this.password.length < 6) {
            return "Password must be atleast 6 characters"
        }

        return null
    }

    fun validateGmailData(): String? {
        if (this.first_name.isNullOrEmpty() || this.first_name == "null") {
            return "First name is required"
        }

        if (this.last_name.isNullOrEmpty() || this.last_name == "null") {
            return "Last name is required"
        }

        if (this.email.isNullOrEmpty() || this.email == "null") {
            return "Email is required"
        }

        if (!this.email.isValidEmail()) {
            return "Please enter a valid email address"
        }

        if (this.mobile.isNullOrEmpty() || this.mobile == "null") {
            return "Phone number is required"
        }


        return null
    }
}